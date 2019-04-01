package org.uu.nl.embedding.bca;

import grph.Grph;
import org.uu.nl.embedding.bca.util.BCAJob;
import org.uu.nl.embedding.bca.util.BCV;
import org.uu.nl.embedding.bca.util.PaintRegistry;
import org.uu.nl.embedding.convert.util.NodeInfo;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class SemanticBCAJob extends BCAJob {

	private static final int SKIP = -1;
	private final Map<Integer, BCV> computedBCV;
	private final int[][] in, out;
	public SemanticBCAJob(
			Grph graph, Map<Integer, BCV> computedBCV, int bookmark, 
			boolean reverse, double alpha, double epsilon,
			int[][] in, int[][] out) {
		super(bookmark, reverse, alpha, epsilon, graph);
		this.computedBCV = computedBCV;
		this.in = in;
		this.out = out;
	}
	
	private boolean isLiteral(int n) {
		return graph.getVertexColorProperty().getValue(n) ==  NodeInfo.LITERAL;
	}
	
	private String nodeLabel(int n) {
		return graph.getVertexLabelProperty().getValueAsString(n);
	}
	
	public String edgeLabel(int e) {
		return graph.getEdgeLabelProperty().getValueAsString(e);
	}
	
	private int getEdgeType(int e) {
		return graph.getEdgeColorProperty().getValueAsInt(e);
	}
	
	private class SemanticNode {

		private final int nodeID;
		private final int prevNodeID;
		private final int predicateID;

		SemanticNode(int nodeID, int predicateID, int prevNodeID) {
			this.nodeID = nodeID;
			this.prevNodeID = prevNodeID;
			this.predicateID = predicateID;
		}

		@Override
		public boolean equals(Object obj) {
			if(obj instanceof SemanticNode) {
				return this.nodeID == ((SemanticNode) obj).nodeID;
			} else return false;
		}
	}

	@Override
	protected BCV doWork(Grph graph, boolean reverse) {
		
		final Queue<SemanticNode> nodeQueue = new LinkedList<>();
		final PaintRegistry<Integer> wetPaintRegister = new PaintRegistry<>();
		final BCV bcv = new BCV(bookmark);

		int[] neighbors, edges, edgeCache;
		int focusNode, neighbor, predicate, edge, neighborCount, ignoredNeighborCount;
		double partialWetPaint;
		SemanticNode node;
		BCV precomputed;

		nodeQueue.add(new SemanticNode(bookmark, SKIP, SKIP));
		wetPaintRegister.put(bookmark, 1d);

		while (!nodeQueue.isEmpty()) {

			node = nodeQueue.poll();
			focusNode = node.nodeID;
			final double wetPaint = wetPaintRegister.get(focusNode);

			//precomputed = computedBCV.get(focusNode);
			if(false){//precomputed != null) {
				precomputed.forEach((index, precomputedPaint) -> {
					final double scaled = precomputedPaint * wetPaint;
					if(scaled > (epsilon * alpha)) bcv.add(index, scaled);
				});
			} else {
				// Keep part of the available paint on this node, distribute the rest
				bcv.add(focusNode, (alpha * wetPaint));

				// If there is not enough paint we stop and don't distribute among the neighbors
				if (wetPaint < epsilon) continue;

				ignoredNeighborCount = 0;

				if(isLiteral(focusNode)) {
					neighbors = in[focusNode];
					edges = new int[neighbors.length];
					predicate = node.predicateID == SKIP ? SKIP : getEdgeType(node.predicateID);
					// Cache edges and reuse in loop
					edgeCache = graph.getInOnlyEdges(focusNode).toIntArray();

					// Check which edges we need to follow and which to ignore
					for(int i = 0; i < neighbors.length; i++) {

						edges[i] = getEdge(neighbors[i], focusNode, graph.getOutEdges(neighbors[i]).toIntArray(), edgeCache);

						if(predicate != SKIP && getEdgeType(edges[i]) != predicate) {
							edges[i] = SKIP;
							ignoredNeighborCount++;
						}
					}
				} else {
					if(reverse) {
						neighbors = new int[in[focusNode].length + out[focusNode].length];
						// Use all incoming neighbors
						System.arraycopy(in[focusNode], 0, neighbors, 0, in[focusNode].length);
						edges = new int[neighbors.length];

						edgeCache = graph.getInOnlyEdges(focusNode).toIntArray();
						for(int i = 0; i < in[focusNode].length; i++)
							edges[i] = getEdge(neighbors[i], focusNode, graph.getInEdges(neighbors[i]).toIntArray(), edgeCache);

						edgeCache = graph.getOutOnlyEdges(focusNode).toIntArray();
						for(int i = 0; i < out[focusNode].length; i++)
							edges[i + in[focusNode].length] = getEdge(focusNode, neighbors[i], edgeCache, graph.getInEdges(neighbors[i]).toIntArray());

						// Also use outgoing neighbors to a literal node
						for(int i = 0 ; i < out[focusNode].length; i++) {
							if(isLiteral(out[focusNode][i]))
								neighbors[i + in[focusNode].length] = out[focusNode][i];
							else {
								edges[i + in[focusNode].length] = SKIP;
								ignoredNeighborCount++;
							}
						}
					} else {
						// Simply follow outgoing nodes
						neighbors = out[focusNode];
						edgeCache = graph.getOutOnlyEdges(focusNode).toIntArray();
						edges = new int[neighbors.length];

						for(int i = 0; i < neighbors.length; i++)
							edges[i] = getEdge(focusNode, neighbors[i], edgeCache, graph.getInEdges(neighbors[i]).toIntArray());
					}
				}


				neighborCount = neighbors.length - ignoredNeighborCount;
				if(neighborCount == 0)
					continue;

				partialWetPaint = (1 - alpha) * wetPaint / neighborCount;
				// We can already tell that the neighbors will not have enough paint to continue
				if(partialWetPaint < epsilon)
					continue;

				/*
				System.out.println(
						nodeLabel(bookmark) +
								" Focus node: " + nodeLabel(focusNode) +
								" Queue size:" + nodeQueue.size() +
								" neighbor count:" + neighborCount +
								" wet paint:" + wetPaint);
				*/

				for (int i = 0; i < neighbors.length; i++) {
					
					neighbor = neighbors[i];
					edge = edges[i];
					
					if(neighbor == node.prevNodeID || edge == SKIP) continue;

					bcv.add(graph.getVertices().size() + getEdgeType(edge), partialWetPaint);

					// Remember which node we came from so we don't go back
					// Remember which predicate we used to get here
					// This way we don't visit nodes that have a different relationship with the literal
					SemanticNode neighborNode = new SemanticNode(neighbor, edge, focusNode);

					if (nodeQueue.contains(neighborNode)) {
						wetPaintRegister.add(neighbor, partialWetPaint);
					} else {
						nodeQueue.add(neighborNode);
						wetPaintRegister.put(neighbor, partialWetPaint);
					}
				}
			}
		}
		return bcv;
	}
}
