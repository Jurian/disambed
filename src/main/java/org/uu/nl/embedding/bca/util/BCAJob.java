package org.uu.nl.embedding.bca.util;

import grph.properties.NumericalProperty;
import org.uu.nl.embedding.util.InMemoryRdfGraph;

import java.util.TreeMap;
import java.util.concurrent.Callable;

/**
 * One BCAJob represents performing the bookmark coloring algorithm for a single node
 * @author Jurian Baas
 *
 */
public abstract class BCAJob implements Callable<BCV> {

	protected final int bookmark;
	protected final double alpha, epsilon;
	protected final InMemoryRdfGraph graph;
	protected final int[][] vertexNeighborhood, edgeNeighborhood;

	protected BCAJob(
			int bookmark,
			double alpha, double epsilon,
			InMemoryRdfGraph graph, int[][] vertexNeighborhood, int[][] edgeNeighborhood) {

		this.bookmark = bookmark;
		this.alpha = alpha;
		this.epsilon = epsilon;
		this.graph = graph;
		this.vertexNeighborhood = vertexNeighborhood;
		this.edgeNeighborhood = edgeNeighborhood;
	}

	@Override
	public BCV call() {
		return doWork();
	}

	protected BCV doWork() {
		final NumericalProperty edgeWeights = graph.getEdgeWeightProperty();

		final TreeMap<Integer, PaintedNode> nodeTree = new TreeMap<>();
		final BCV bcv = new BCV(bookmark);

		nodeTree.put(bookmark, new PaintedNode(bookmark, 1));

		int[] neighbors, edges;
		int focusNode;
		double wetPaint, partialWetPaint, totalWeight;
		PaintedNode node;

		while (!nodeTree.isEmpty()) {

			node = nodeTree.pollFirstEntry().getValue();
			focusNode = node.nodeID;
			wetPaint = node.getPaint();

			// Keep part of the available paint on this node, distribute the rest
			bcv.add(focusNode, (alpha * wetPaint));

			// If there is not enough paint we stop and don't distribute among the neighbors
			if (wetPaint < epsilon) continue;

			neighbors = vertexNeighborhood[focusNode];
			edges = edgeNeighborhood[focusNode];

			totalWeight = getTotalWeight(neighbors, edges, -1);

			for (int i = 0; i < neighbors.length; i++) {

				float weight = edgeWeights.getValueAsFloat(edges[i]);
				partialWetPaint = (1 - alpha) * wetPaint * (weight / totalWeight);

				// Log(n) time lookup
				if (nodeTree.containsKey(neighbors[i])) {
					nodeTree.get(neighbors[i]).addPaint(partialWetPaint);
				} else {
					nodeTree.put(neighbors[i], new PaintedNode(neighbors[i], partialWetPaint));
				}

			}
		}
		return bcv;
	}

	protected double getTotalWeight(int[] neighbors, int[] edges, int ignore) {
		double totalWeight = 0f;
		for (int i = 0; i < neighbors.length; i++) {
			if(neighbors[i] == ignore) continue;
			totalWeight += graph.getEdgeWeightProperty().getValueAsFloat(edges[i]);
		}
		return totalWeight;
	}

}
