package org.uu.nl.embedding.bca.jobs;

import grph.properties.NumericalProperty;

import org.uu.nl.embedding.bca.util.BCAJob;
import org.uu.nl.embedding.bca.util.BCV;
import org.uu.nl.embedding.bca.util.PaintedNode;
import org.uu.nl.embedding.logic.DateCompareLogic;
import org.uu.nl.embedding.logic.LogicRule;
import org.uu.nl.embedding.util.InMemoryRdfGraph;

import java.util.Map;
import java.util.Random;
import java.util.TreeMap;


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

	/**
	 * Nog niet af!!!
	 * 
	 * @author Euan Westenbroek
	 * @version 0.1
	 * @since 14-05-2020
	 */
	protected BCV addDateAwareWindow(final BCV bcv, final DateCompareLogic dateRule, final InMemoryRdfGraph graph) {

		final NumericalProperty edgeWeights = graph.getEdgeWeightProperty();
		final NumericalProperty edgeTypes = graph.getEdgeTypeProperty();
		final TreeMap<Integer, PaintedNode> nodeTree = new TreeMap<>();
		final BCV awareBcv = new BCV(bcv.getRootNode());

		nodeTree.put(bcv.getRootNode(), new PaintedNode(bcv.getRootNode(), 1));

		int focusNode;
		double wetPaint, partialWetPaint;
		PaintedNode node;
		

		
		// wat zit er in een bcv? en hoe kunnen we die langs gaan om buren te checken op waarden en paint aanpassen?
		for(Map.Entry<Integer, Float> entry : bcv.entrySet()) {
			focusNode = entry.getKey();
		}
		
		while (!nodeTree.isEmpty()) {

			node = nodeTree.pollFirstEntry().getValue();
			focusNode = node.nodeID;
			wetPaint = node.getPaint();

            // Keep part of the available paint on this node, distribute the rest
			awareBcv.add(focusNode, (alpha * wetPaint));
            
			for (int i = 0; i < vertexOut[focusNode].length; i++) {
				
			}
		}
		
		return awareBcv;
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
}
