package org.uu.nl.embedding.bca.util;

import it.unimi.dsi.fastutil.ints.Int2FloatRBTreeMap;
import org.uu.nl.embedding.util.InMemoryRdfGraph;

/**
 * One BCAJob represents performing the bookmark coloring algorithm for a single node. This version does early stopping,
 * thereby preventing any paint less than alpha * epsilon to be distributed. This improves stability in GloVe later on.
 * @author Jurian Baas
 *
 */
public abstract class BCAJobStable extends BCAJob {

	protected BCAJobStable(int bookmark, float alpha, float epsilon, InMemoryRdfGraph graph, int[][] vertexNeighborhood, int[][] edgeNeighborhood) {
		super(bookmark, alpha, epsilon, graph, vertexNeighborhood, edgeNeighborhood);
	}

	protected BCV doWork() {

		final Int2FloatRBTreeMap nodeTree = new Int2FloatRBTreeMap();
		final BCV bcv = new BCV(bookmark);

		nodeTree.put(bookmark, 1f);

		int[] neighbors, edges;
		int focusNode;
		float wetPaint, partialWetPaint, totalWeight;


		while (!nodeTree.isEmpty()) {

			focusNode = nodeTree.firstIntKey();
			wetPaint = nodeTree.get(focusNode);
			nodeTree.remove(focusNode);

			// Keep part of the available paint on this node, distribute the rest
			bcv.addTo(focusNode, (alpha * wetPaint));

			neighbors = vertexNeighborhood[focusNode];
			edges = edgeNeighborhood[focusNode];

			totalWeight = getTotalWeight(neighbors, edges, -1);

			for (int i = 0; i < neighbors.length; i++) {

				float weight = graph.getEdgeWeightProperty().getValueAsFloat(edges[i]);
				partialWetPaint = (1 - alpha) * wetPaint * (weight / totalWeight);

				// Stopping early here increases stability in GloVe
				if(partialWetPaint < epsilon) continue;

				// Log(n) time lookup
				nodeTree.addTo(neighbors[i], partialWetPaint);
			}
		}

		return bcv;
	}
}
