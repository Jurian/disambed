package org.uu.nl.embedding.analyze.bca.grph;

import grph.Grph;
import org.uu.nl.embedding.analyze.bca.grph.util.BCAJob;
import org.uu.nl.embedding.analyze.bca.util.BCV;
import org.uu.nl.embedding.analyze.bca.util.PaintRegistry;
import org.uu.nl.embedding.convert.util.NodeInfo;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class SemanticBCAJob extends BCAJob {

	private boolean debug = false;
	private static final int NO_VALUE = -1;
	private final Map<Integer, BCV> computedBCV;
	private final int[][] in, out, edgeOut, edgeIn;
	public SemanticBCAJob(
			Grph graph, Map<Integer, BCV> computedBCV, int bookmark, 
			boolean reverse, boolean normalize, double alpha, double epsilon,
			int[][] in, int[][] out, int[][] edgeIn, int[][] edgeOut) {
		super(bookmark, reverse, normalize, alpha, epsilon, graph);
		this.computedBCV = computedBCV;
		this.in = in;
		this.out = out;
		this.edgeIn = edgeIn;
		this.edgeOut = edgeOut;
	}
	
	public boolean isLiteral(int n) {
		return graph.getVertexColorProperty().getValue(n) ==  NodeInfo.LITERAL;
	}
	
	public String nodeLabel(int n) {
		return graph.getVertexLabelProperty().getValueAsString(n);
	}
	
	public String edgeLabel(int e) {
		return graph.getEdgeLabelProperty().getValueAsString(e);
	}
	
	public int getEdgeType(int e) {
		return graph.getEdgeColorProperty().getValueAsInt(e);
	}
	
	private class SemanticNode {

		private int nodeID;
		private int prevNodeID;
		private int predicatID;
		
		public SemanticNode(int nodeID) {
			this(nodeID, NO_VALUE);
		}
		
		public SemanticNode(int nodeID, int predicateID) {
			this(nodeID, predicateID, NO_VALUE);
		}
		
		public SemanticNode(int nodeID, int predicateID, int prevNodeID) {
			this.nodeID = nodeID;
			this.prevNodeID = prevNodeID;
			this.predicatID = predicateID;
		}

		@Override
		public boolean equals(Object obj) {
			if(obj instanceof SemanticNode) {
				
				SemanticNode other = (SemanticNode) obj;
				
				return 	this.nodeID == other.nodeID;// && 
						//this.prevNodeID == other.prevNodeID && 
						//this.predicatID == other.predicatID;
				
			} else return false;
		}
	}

	@Override
	protected BCV doWork(Grph graph, boolean reverse) {
		
		final Queue<SemanticNode> nodeQueue = new LinkedList<>();
		final PaintRegistry<Integer> wetPaintRegister = new PaintRegistry<>();
		final BCV bcv = new BCV(bookmark);

		int[] neighbors, edges, edgeCache;
		int focusNode, neighbor, predicate, neighborCount, ignoredNeighbor, ignoredEdgeCount;
		double weight, partialWetPaint, semanticWeight = 0.01;
		SemanticNode node;
		BCV precomputed;

		nodeQueue.add(new SemanticNode(bookmark));
		wetPaintRegister.put(bookmark, 1d);

		while (!nodeQueue.isEmpty()) {

			node = nodeQueue.poll();
			focusNode = node.nodeID;
			final double wetPaint = wetPaintRegister.get(focusNode);

			precomputed = computedBCV.get(focusNode);
			if(precomputed != null) {
				precomputed.forEach((index, precomputedPaint) -> {
					final double scaled = precomputedPaint * wetPaint;
					if(scaled > (epsilon * alpha)) bcv.add(index, scaled);
				});
			} else {
				// Keep part of the available paint on this node, distribute the rest
				bcv.add(focusNode, (alpha * wetPaint));

				// If there is not enough paint we stop and don't distribute among the neighbors
				if (wetPaint < epsilon) continue;

				ignoredNeighbor = node.prevNodeID;
				ignoredEdgeCount = 0;

				// We do something different in case of a literal node
				if(isLiteral(focusNode)) {
					neighbors = in[focusNode];
					edges = edgeIn[focusNode];
					//edges = new int[neighbors.length];
					predicate = getEdgeType(node.predicatID);
					// Cache edges and reuse in loop
					//edgeCache = graph.getInOnlyEdges(focusNode).toIntArray();
					
					// Check which edges we need to follow and which to ignore
					for(int i = 0; i < neighbors.length; i++) {
						//edges[i] = getSomeEdgeConnecting(
						//		neighbors[i], focusNode,
						//		graph.getOutEdges(neighbors[i]).toIntArray(), edgeCache);
						if(getEdgeType(edges[i]) != predicate) {
							edges[i] = NO_VALUE;
							ignoredEdgeCount++;
						}
					}
				} else {
					neighbors = out[focusNode];
					// Cache edges and reuse in loop
					//edgeCache = graph.getOutOnlyEdges(focusNode).toIntArray();
					edges = edgeOut[focusNode];

					//edges = new int[neighbors.length];
					//for(int i = 0; i < neighbors.length; i++) {
					//	edges[i] = getSomeEdgeConnecting(
					//			focusNode, neighbors[i],
					//			edgeCache, graph.getInEdges(neighbors[i]).toIntArray());
					//}
				}

				neighborCount = neighbors.length - ignoredEdgeCount;

				//if(ignoredNeighbor != NO_VALUE && (isLiteral(ignoredNeighbor) || isLiteral(focusNode))) neighborCount--;

				//if(neighborCount > 250) continue;

				if(debug)
				System.out.println(
						nodeLabel(bookmark) +
						" Focus node: " + nodeLabel(focusNode) +
						" Queue size:" + nodeQueue.size() +
						" neighbor count:" + neighborCount +
						" wet paint:" + wetPaint);

				partialWetPaint = (1 - alpha) * wetPaint * (1 / (double) neighborCount);
				if(partialWetPaint < epsilon) continue;

				for (int i = 0; i < neighbors.length; i++) {
					
					neighbor = neighbors[i];
					predicate = edges[i];
					
					if(neighbor == ignoredNeighbor || predicate == NO_VALUE) continue;

					assert neighborCount > 0;

					//weight = 1 / (double) neighborCount;
					//partialWetPaint = (1 - alpha) * wetPaint * weight;

					//if(Double.isInfinite(partialWetPaint)) continue;

					bcv.add(getEdgeType(predicate), partialWetPaint);
					
					SemanticNode neighborNode = new SemanticNode(neighbor);
					// When we consider this literal neighbor later we have to remember with predicate we used
					// This way we don't visit nodes that have a different relationship with the literal
					neighborNode.prevNodeID = focusNode;
					neighborNode.predicatID = predicate;

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
	
	private int getSomeEdgeConnecting(int src, int dest, int[] out, int[] in) {
		if (out.length == 0 || in.length == 0) {
			return -1;
		} else {
			if (out.length < in.length) {
				for(int e : out) 
					if (graph.getDirectedSimpleEdgeHead(e) == dest) return e;
			} else {
				for(int e : in) 
					if (graph.getDirectedSimpleEdgeTail(e) == src) return e;
			}
			return -1;
		}
	}
}
