package org.uu.nl.embedding.bca.jobs;

import grph.properties.NumericalProperty;

import org.uu.nl.embedding.bca.util.BCAJob;
import org.uu.nl.embedding.bca.util.BCV;
import org.uu.nl.embedding.bca.util.PaintedNode;
import org.uu.nl.embedding.logic.DateCompareLogic;
import org.uu.nl.embedding.logic.LogicRule;
import org.uu.nl.embedding.logic.util.SimpleDate;
import org.uu.nl.embedding.util.InMemoryRdfGraph;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.stream.IntStream;


/**
 * @author Jurian Baas
 */
public class UndirectedWeighted extends BCAJob {

	private final int[][] vertexOut, vertexIn, edgeOut, edgeIn;

	public UndirectedWeighted(InMemoryRdfGraph graph, int bookmark,
							  double alpha, double epsilon,
							  int[][] vertexIn, int[][] vertexOut, int[][] edgeIn, int[][] edgeOut) {
		super(bookmark, false, alpha, epsilon, graph);
		this.vertexOut = vertexOut;
		this.vertexIn = vertexIn;
		this.edgeOut = edgeOut;
		this.edgeIn = edgeIn;
	}

	@Override
	protected BCV doWork(final InMemoryRdfGraph graph, final boolean reverse) {

		if(reverse) throw new UnsupportedOperationException("No reverse mode in undirected version");

		final NumericalProperty edgeWeights = graph.getEdgeWeightProperty();
		final NumericalProperty edgeTypes = graph.getEdgeTypeProperty();
		final TreeMap<Integer, PaintedNode> nodeTree = new TreeMap<>();
		final BCV bcv = new BCV(bookmark);

		nodeTree.put(bookmark, new PaintedNode(bookmark, 1));

		int focusNode;
		double wetPaint, partialWetPaint;
		PaintedNode node;

		while (!nodeTree.isEmpty()) {

			node = nodeTree.pollFirstEntry().getValue();
			focusNode = node.nodeID;
			wetPaint = node.getPaint();

            // Keep part of the available paint on this node, distribute the rest
            bcv.add(focusNode, (alpha * wetPaint));

            // If there is not enough paint we stop and don't distribute among the neighbors
            if (wetPaint < epsilon)
                continue;

			double totalWeight = 0;

			for (int i = 0; i < vertexOut[focusNode].length; i++) {
				// Skip any edges we don't want to follow
				//if(neighbors[i] == node.prevNodeID) continue;
				totalWeight += edgeWeights.getValueAsFloat(edgeOut[focusNode][i]);
			}

			for (int i = 0; i < vertexIn[focusNode].length; i++) {
				// Skip any edges we don't want to follow
				//if(neighbors[i] == node.prevNodeID) continue;
				totalWeight += edgeWeights.getValueAsFloat(edgeIn[focusNode][i]);
			}

			for (int i = 0; i < vertexOut[focusNode].length; i++) {

				float weight = edgeWeights.getValueAsFloat(edgeOut[focusNode][i]);
				partialWetPaint = (1 - alpha) * wetPaint * (weight / totalWeight);

				// We can already tell that the neighbor will not have enough paint to continue
				if(partialWetPaint < epsilon)
					continue;

				// Log(n) time lookup
				if (nodeTree.containsKey(vertexOut[focusNode][i])) {
					nodeTree.get(vertexOut[focusNode][i]).addPaint(partialWetPaint);
				} else {

					// Remember which node we came from so we don't go back
					// Remember which predicate we used to get here
					nodeTree.put(vertexOut[focusNode][i], new PaintedNode(vertexOut[focusNode][i], partialWetPaint));
				}
			}

			for (int i = 0; i < vertexIn[focusNode].length; i++) {

				float weight = edgeWeights.getValueAsFloat(edgeIn[focusNode][i]);
				partialWetPaint = (1 - alpha) * wetPaint * (weight / totalWeight);

				// We can already tell that the neighbor will not have enough paint to continue
				if(partialWetPaint < epsilon)
					continue;

				// Log(n) time lookup
				if (nodeTree.containsKey(vertexIn[focusNode][i])) {
					nodeTree.get(vertexIn[focusNode][i]).addPaint(partialWetPaint);
				} else {

					// Remember which node we came from so we don't go back
					// Remember which predicate we used to get here
					nodeTree.put(vertexIn[focusNode][i], new PaintedNode(vertexIn[focusNode][i], partialWetPaint));
				}
			}
		}
		return bcv;
	}

	public BCV doWorkWithIgnore(final InMemoryRdfGraph graph, final boolean reverse, 
									final BCV origBcv, final int[] orderedNodes, final List<Map<Integer, int[]>> removedInOutMaps) {

		if(reverse) throw new UnsupportedOperationException("No reverse mode in undirected version");

		final NumericalProperty edgeWeights = graph.getEdgeWeightProperty();
		final TreeMap<Integer, PaintedNode> nodeTree = new TreeMap<>();
		final BCV bcv = new BCV(bookmark);
		int[][][] inOutVerts = new int[][][] {vertexIn, vertexOut};
		int[][][] inOutEdges = new int[][][] {edgeIn, edgeOut};

		nodeTree.put(bookmark, new PaintedNode(bookmark, 1));

		int focusNode;
		double wetPaint, partialWetPaint;
		PaintedNode node;

		while (!nodeTree.isEmpty()) {

			node = nodeTree.pollFirstEntry().getValue();
			focusNode = node.nodeID;
			wetPaint = node.getPaint();

            // Keep part of the available paint on this node, distribute the rest
            bcv.add(focusNode, (alpha * wetPaint));

            // If there is not enough paint we stop and don't distribute among the neighbors
            if (wetPaint < epsilon)
                continue;

			double totalWeight = 0;
			
			for(int direction = 0; direction < removedInOutMaps.size(); direction++) {
				// get array of removed neighbors (== neighbor of focusNode)
				int[] removedNeighbors = removedInOutMaps.get(direction).get(focusNode);
				
				/*
				 * Calculate totalWeight using edgeWeights
				 */
				for (int i = 0; i < inOutVerts[direction][focusNode].length; i++) {
					// Check if there are any removed neighbors of the focusNode
					if(removedNeighbors.length == 0) {
						
							totalWeight += edgeWeights.getValueAsFloat(inOutEdges[direction][focusNode][i]);
							
					} else if(removedNeighbors.length > 0) { // if array not empty then ignore each removed vertex

						for(int removedEdge : removedNeighbors) {
							if(inOutEdges[direction][focusNode][i] == removedEdge) {
								// Keep totalWeight the same
								continue;
							} else {
								totalWeight += edgeWeights.getValueAsFloat(inOutEdges[direction][focusNode][i]);
							}
						} 
					} else { // removedVerts < 0, throw Exception
						String sDir;
						if(direction == 0) {
							sDir = "INCOMING";
						} else if(direction == 1) {
							sDir = "OUTGOING";
						} else {
							sDir = "(BUG WARNING: direction == " + Integer.toString(direction) + ")";
						}
			        	throw new IllegalArgumentException("Length of removed " + sDir + "verts array of " + 
			        			Integer.toString(focusNode) + " is smaller than 0, namely: " + Integer.toString(removedNeighbors.length));
					}
				}
			}
			
			
			/*
			 * Calculate new paint values
			 * and put neighboring vertices in TreeMap
			 */
			// Loop over directions as integer
			for(int direction = 0; direction < removedInOutMaps.size(); direction++) {
				// get array of removed neighbors (== neighbor of focusNode)
				int[] removedNeighbors = removedInOutMaps.get(direction).get(focusNode);

				// Check if there are any removed neighbors of the focusNode
				for (int i = 0; i < inOutVerts[direction][focusNode].length; i++) {
					if(removedNeighbors.length == 0) {
								
						float weight = edgeWeights.getValueAsFloat(inOutEdges[direction][focusNode][i]);
						partialWetPaint = (1 - alpha) * wetPaint * (weight / totalWeight);
		
						// We can already tell that the neighbor will not have enough paint to continue
						if(partialWetPaint < epsilon)
							continue;
		
						// Log(n) time lookup
						if (nodeTree.containsKey(inOutVerts[direction][focusNode][i])) {
							nodeTree.get(inOutVerts[direction][focusNode][i]).addPaint(partialWetPaint);
						} else {
		
							// Remember which node we came from so we don't go back
							// Remember which predicate we used to get here
							nodeTree.put(inOutVerts[direction][focusNode][i], new PaintedNode(inOutVerts[direction][focusNode][i], partialWetPaint));
						}
							
					} // if array not empty then ignore each removed vertex
					else if(removedNeighbors.length > 0) {
							// Act accordingly by reducing number of neighbors and ignore the removed vertex (== neighbor of focusNode)
	
						for(int removedEdge : removedNeighbors) {
							if(inOutEdges[direction][focusNode][i] == removedEdge) {
								// Don't update values, and ignore this edge
								continue;
								
							} else {
								float weight = edgeWeights.getValueAsFloat(inOutEdges[direction][focusNode][i]);
								partialWetPaint = (1 - alpha) * wetPaint * (weight / totalWeight);
				
								// We can already tell that the neighbor will not have enough paint to continue
								if(partialWetPaint < epsilon)
									continue;
				
								// Log(n) time lookup
								if (nodeTree.containsKey(inOutVerts[direction][focusNode][i])) {
									nodeTree.get(inOutVerts[direction][focusNode][i]).addPaint(partialWetPaint);
								} else {
				
									// Remember which node we came from so we don't go back
									// Remember which predicate we used to get here
									nodeTree.put(inOutVerts[direction][focusNode][i], new PaintedNode(inOutVerts[direction][focusNode][i], partialWetPaint));
								}
							}
						}
					} //END else-if length > 0
					else { // removedNeighbors < 0, throw Exception
						String sDir;
						if(direction == 0) {
							sDir = "INCOMING";
						} else if(direction == 1) {
							sDir = "OUTGOING";
						} else {
							sDir = "(BUG WARNING: direction == " + Integer.toString(direction) + ")";
						}
			        	throw new IllegalArgumentException("Length of removed " + sDir + "verts array of " + 
			        			Integer.toString(focusNode) + " is smaller than 0, namely: " + Integer.toString(removedNeighbors.length));
					}
				}
			}
		}
		return bcv;
	}
}
