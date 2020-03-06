package org.uu.nl.embedding.bca.jobs;

import grph.Grph;
import grph.properties.NumericalProperty;
import org.uu.nl.embedding.bca.util.BCAJob;
import org.uu.nl.embedding.bca.util.BCV;
import org.uu.nl.embedding.bca.util.PaintedNode;
import org.uu.nl.embedding.util.InMemoryRdfGraph;

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

	@Override
	protected BCV doWork(InMemoryRdfGraph graph, boolean reverse) {

		if(reverse) throw new UnsupportedOperationException("No reverse mode in undirected version");

		final NumericalProperty edgeWeights = graph.getEdgeWeightProperty();
		final NumericalProperty edgeTypes = graph.getEdgeTypeProperty();
		final TreeMap<Integer, PaintedNode> nodeTree = new TreeMap<>();
		final BCV bcv = new BCV(bookmark);

		nodeTree.put(bookmark, new PaintedNode(bookmark, PaintedNode.SKIP, PaintedNode.SKIP, 1));

		int focusNode, edgeType, neighbor;
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
			int inDegree = vertexIn[focusNode].length;
			int outDegree = vertexOut[focusNode].length;
			int degree = inDegree + outDegree;

			for (int i = 0; i < degree; i++) {


				if(i < vertexIn[focusNode].length) {
					// Skip any edges we don't want to follow
					if(vertexIn[focusNode][i] == node.prevNodeID) continue;
					totalWeight += edgeWeights.getValueAsFloat(edgeIn[focusNode][i]);
					//totalWeight += weights[getEdgeType(edgeIn[focusNode][i])];
				} else {
					// Skip any edges we don't want to follow
					if(vertexOut[focusNode][i - inDegree] == node.prevNodeID) continue;
					totalWeight += edgeWeights.getValueAsFloat(edgeOut[focusNode][i - inDegree]);
					//totalWeight += weights[getEdgeType(edgeOut[focusNode][i - inDegree])];
				}

			}
			// We ended up skipping all neighbors
			if(totalWeight == 0) continue;

            for (int i = 0; i < degree; i++) {

            	float weight;
				if(i < vertexIn[focusNode].length) {
					// Skip any edges we don't want to follow
					if(vertexIn[focusNode][i] == node.prevNodeID) continue;

					neighbor = vertexIn[focusNode][i];
					weight = edgeWeights.getValueAsFloat(edgeIn[focusNode][i]);
					edgeType = edgeTypes.getValueAsInt(edgeIn[focusNode][i]);
				} else {
					// Skip any edges we don't want to follow
					if(vertexOut[focusNode][i - inDegree] == node.prevNodeID) continue;

					neighbor = vertexOut[focusNode][i - inDegree];
					weight = edgeWeights.getValueAsFloat(edgeOut[focusNode][i - inDegree]);
					edgeType = edgeTypes.getValueAsInt(edgeOut[focusNode][i - inDegree]);
				}

				partialWetPaint = (1 - alpha) * wetPaint * (weight / totalWeight);

				// We can already tell that the neighbor will not have enough paint to continue
				if(partialWetPaint < epsilon)
					continue;

				// Log(n) time lookup
				if (nodeTree.containsKey(neighbor)) {
					nodeTree.get(neighbor).addPaint(partialWetPaint);
				} else {

					// Remember which node we came from so we don't go back
					// Remember which predicate we used to get here
					nodeTree.put(neighbor, new PaintedNode(neighbor, edgeType, focusNode, partialWetPaint));
				}

            }
		}
		return bcv;
	}
}
