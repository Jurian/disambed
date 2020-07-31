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
	protected final boolean reverse;
	protected final double alpha, epsilon;
	protected final InMemoryRdfGraph graph;
	protected final int[][] vertexOut, vertexIn, edgeOut, edgeIn;
	protected String callType;

	protected BCAJob(
			int bookmark, boolean reverse,
			double alpha, double epsilon,
			InMemoryRdfGraph graph, String callType, int[][] vertexOut, int[][] vertexIn, int[][] edgeOut, int[][] edgeIn) {

		this.reverse = reverse;
		this.bookmark = bookmark;
		this.alpha = alpha;
		this.epsilon = epsilon;
		this.graph = graph;
		this.callType = callType;
		this.vertexOut = vertexOut;
		this.vertexIn = vertexIn;
		this.edgeOut = edgeOut;
		this.edgeIn = edgeIn;
	}

	@Override
	public BCV call() {
		if (this.callType == "kaleundirectedweighted") {
//######---------WERKT DIT???????????????????????????????????????????????
			final BCV bcv = doWorkInclEdges(false);
			return bcv;
			
		} else if (this.callType == "undirectedweighted") {
			final BCV bcv = doWork(false);
			if (this.reverse) bcv.merge(doWork(true));
			return bcv;
			
		} else {
			final BCV bcv = doWork(false);
			if (this.reverse) bcv.merge(doWork(true));
			return bcv;
		}
	}

	protected BCV doWork(final boolean reverse) {
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

			neighbors = getNeighbors(reverse, focusNode);
			edges = getEdges(reverse, focusNode);

			totalWeight = getTotalWeight(neighbors, edges);

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
	
	protected BCV doWorkInclEdges(final boolean reverse) {
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

			neighbors = getNeighbors(reverse, focusNode);
			edges = getEdges(reverse, focusNode);

			totalWeight = getTotalWeight(neighbors, edges);

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

	protected double getTotalWeight(int[] neighbors, int[] edges) {
		double totalWeight = 0f;
		for (int i = 0; i < neighbors.length; i++)
			totalWeight += graph.getEdgeWeightProperty().getValueAsFloat(edges[i]);
		return totalWeight;
	}

	protected int[] getNeighbors(final boolean reverse, final int focusNode) {
		return getIndexes(reverse, focusNode, vertexIn, vertexOut);
	}

	protected int[] getEdges(final boolean reverse, final int focusNode) {
		return getIndexes(reverse, focusNode, edgeIn, edgeOut);
	}

	protected abstract int[] getIndexes(boolean reverse, int focusNode, int[][] indexIn, int[][] indexOut);
}
