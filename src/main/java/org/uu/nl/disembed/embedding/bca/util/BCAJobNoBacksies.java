package org.uu.nl.disembed.embedding.bca.util;

import org.uu.nl.disembed.embedding.convert.InMemoryRdfGraph;

import java.util.TreeMap;

/**
 * One BCAJob represents performing the bookmark coloring algorithm for a single node. This version does early stopping,
 * thereby preventing any paint less than alpha * epsilon to be distributed. This improves stability in GloVe later on.
 * @author Jurian Baas
 *
 */
public class BCAJobNoBacksies extends BCAJobStable {

	static class PaintedNodeWithMemory extends PaintedNode {

		public static final int NO_PREVIOUS_NODE = -1;

		public final int previousNodeId;

		public PaintedNodeWithMemory(int nodeID, float startPaint) {
			super(nodeID, startPaint);
			this.previousNodeId = NO_PREVIOUS_NODE;
		}

		public PaintedNodeWithMemory(int nodeID, float startPaint, int previousNodeId) {
			super(nodeID, startPaint);
			this.previousNodeId = previousNodeId;
		}
	}

	public BCAJobNoBacksies(int bookmark, float alpha, float epsilon, InMemoryRdfGraph graph, int[][] vertexNeighborhood, int[][] edgeNeighborhood) {
		super(bookmark, alpha, epsilon, graph, vertexNeighborhood, edgeNeighborhood);
	}

	@Override
	protected BCV doWork() {

		final TreeMap<Integer, PaintedNodeWithMemory> nodeTree = new TreeMap<>();
		final BCV bcv = new BCV(bookmark);

		nodeTree.put(bookmark, new PaintedNodeWithMemory(bookmark, 1));

		int[] neighbors, edges;
		int focusNode;
		float wetPaint, partialWetPaint, totalWeight;
		PaintedNodeWithMemory node;
		String bookmarkLabel = graph.getVertexLabelProperty().getValueAsString(bookmark);

		while (!nodeTree.isEmpty()) {

			node = nodeTree.pollFirstEntry().getValue();
			focusNode = node.nodeID;
			wetPaint = node.getPaint();


			// Keep part of the available paint on this node, distribute the rest
			bcv.addTo(focusNode, (alpha * wetPaint));

			neighbors = vertexNeighborhood[focusNode];
			edges = edgeNeighborhood[focusNode];

			totalWeight = getTotalWeight(neighbors, edges, node.previousNodeId);


			for (int i = 0; i < neighbors.length; i++) {

				if(neighbors[i] == node.previousNodeId) continue;

				float weight = graph.getEdgeWeightProperty().getValueAsFloat(edges[i]);
				partialWetPaint = (1 - alpha) * wetPaint * (weight / totalWeight);

				// Stopping early here increases stability in GloVe
				if(partialWetPaint < epsilon) continue;

				// Log(n) time lookup
				if (nodeTree.containsKey(neighbors[i])) {
					nodeTree.get(neighbors[i]).addPaint(partialWetPaint);
				} else {
					nodeTree.put(neighbors[i], new PaintedNodeWithMemory(neighbors[i], partialWetPaint, focusNode));
				}

			}
		}
		return bcv;
	}
}
