package org.uu.nl.embedding.bca.jobs;

import grph.Grph;
import org.uu.nl.embedding.bca.util.BCAJob;
import org.uu.nl.embedding.bca.util.BCV;
import org.uu.nl.embedding.bca.util.PaintedNode;

import java.util.TreeMap;


/**
 * @author Jurian Baas
 */
public class UndirectedWeighted extends BCAJob {

	private final int[][] vertexOut, vertexIn, edgeOut, edgeIn;
	private final double[] weights;

	public UndirectedWeighted(Grph graph,
							  int bookmark, double[] weights, boolean predicates, double alpha, double epsilon,
							  int[][] vertexIn, int[][] vertexOut, int[][] edgeIn, int[][] edgeOut) {
		super(bookmark, false, predicates, alpha, epsilon, graph);
		this.weights = weights;
		this.vertexOut = vertexOut;
		this.vertexIn = vertexIn;
		this.edgeOut = edgeOut;
		this.edgeIn = edgeIn;
	}

	@Override
	protected BCV doWork(Grph graph, boolean reverse) {

		if(reverse) throw new UnsupportedOperationException("No reverse mode in undirected version");

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
					totalWeight += weights[getEdgeType(edgeIn[focusNode][i])];
				} else {
					// Skip any edges we don't want to follow
					if(vertexOut[focusNode][i - inDegree] == node.prevNodeID) continue;
					totalWeight += weights[getEdgeType(edgeOut[focusNode][i - inDegree])];
				}

			}
			// We ended up skipping all neighbors
			if(totalWeight == 0) continue;

            for (int i = 0; i < degree; i++) {

				if(i < vertexIn[focusNode].length) {
					// Skip any edges we don't want to follow
					if(vertexIn[focusNode][i] == node.prevNodeID) continue;

					neighbor = vertexIn[focusNode][i];
					edgeType = getEdgeType(edgeIn[focusNode][i]);
				} else {
					// Skip any edges we don't want to follow
					if(vertexOut[focusNode][i - inDegree] == node.prevNodeID) continue;

					neighbor = vertexOut[focusNode][i - inDegree];
					edgeType = getEdgeType(edgeOut[focusNode][i - inDegree]);
				}

				partialWetPaint = (1 - alpha) * wetPaint * (weights[edgeType] / totalWeight);

				// We can already tell that the neighbor will not have enough paint to continue
				if(partialWetPaint < epsilon)
					continue;

				if(predicates) {
					// Add the predicate to the context
					int edgeIndex = graph.getVertices().size() + edgeType;
					bcv.add(edgeIndex, partialWetPaint);
				}

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
