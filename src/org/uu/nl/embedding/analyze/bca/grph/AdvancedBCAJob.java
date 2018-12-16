package org.uu.nl.embedding.analyze.bca.grph;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.uu.nl.embedding.analyze.bca.grph.util.BCAJob;
import org.uu.nl.embedding.analyze.bca.util.BCV;
import org.uu.nl.embedding.analyze.bca.util.PaintRegistry;
import org.uu.nl.embedding.convert.util.NodeInfo;

import grph.Grph;

public class AdvancedBCAJob extends BCAJob {

	private static final int NO_VALUE = -1;
	private final Map<Integer, BCV> computedBCV;
	private final int[][] in, out;
	
	public AdvancedBCAJob(Map<Integer, BCV> computedBCV, int bookmark, boolean reverse, boolean normalize, double alpha, double epsilon, Grph graph, int[][] in, int[][] out) {
		super(bookmark, reverse, normalize, alpha, epsilon, graph);
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
	
	private String edgeLabel(int e) {
		return graph.getEdgeLabelProperty().getValueAsString(e);
	}
	
	private int getEdgeUniqueId(int e) {
		return graph.getEdgeColorProperty().getValueAsInt(e);
	}

	@Override
	protected BCV doWork(Grph graph, boolean reverse) {
		
		final Queue<Integer> nodeQueue = new LinkedList<>();
		final PaintRegistry<Integer> wetPaintRegister = new PaintRegistry<>();
		final BCV bcv = new BCV(bookmark);
		
		final Map<Integer, Integer> filterPredicate = new HashMap<>();
		final Map<Integer, Integer> ignoreMap = new HashMap<>();
		
		nodeQueue.add(bookmark);
		wetPaintRegister.put(bookmark, 1d);
		
		int[] neighbors, edges;
		int focusNode, neighbor, predicate, neighborCount, ignoredNeighbor, ignoredEdge;
		double weight, partialWetPaint;
		BCV precomputed;
		
		while (!nodeQueue.isEmpty()) {

			focusNode = nodeQueue.poll();
			final double wetPaint = wetPaintRegister.get(focusNode);

			// System.out.println("Focus: " + nodeLabel(focusNode) + " " + focusNode);
			
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
				if (wetPaint < epsilon)
					continue;

				ignoredNeighbor = ignoreMap.getOrDefault(focusNode, NO_VALUE);
				ignoredEdge = 0;
				
				// We do something different in case of a literal node
				if(isLiteral(focusNode)) {
					neighbors = in[focusNode];
					edges = new int[neighbors.length];
					predicate = getEdgeUniqueId(filterPredicate.get(focusNode));
					
					for(int i = 0; i < neighbors.length; i++) {
						edges[i] = graph.getSomeEdgeConnecting(neighbors[i], focusNode);
						if(getEdgeUniqueId(edges[i]) != predicate) {
							edges[i] = NO_VALUE;
							ignoredEdge++;
						}	
					}
					
					// Remove this node from the map in case we reach it again from a different predicate
					// System.out.println("Came in from " + edgeLabel(filterPredicate.get(focusNode)) + " and " + nodeLabel(ignoredNeighbor) + " " + ignoredNeighbor);
					filterPredicate.remove(focusNode);
				} else {
					neighbors = out[focusNode];
					edges = new int[neighbors.length];
					for(int i = 0; i < neighbors.length; i++) {
						edges[i] = graph.getSomeEdgeConnecting(focusNode, neighbors[i]);
					}
				}
				
				neighborCount = ignoredNeighbor == NO_VALUE ? neighbors.length : neighbors.length - 1;
				neighborCount -= ignoredEdge;
				
				// System.out.println("Neighbors:");
				for (int i = 0; i < neighbors.length; i++) {
					
					neighbor = neighbors[i];
					predicate = edges[i];
					
					if(neighbor == ignoredNeighbor || predicate == NO_VALUE) continue;
					
					// System.out.println(nodeLabel(focusNode) + " (" + focusNode + ") -- " + edgeLabel(edges[i]) + " (" + edges[i] + ") -> " + nodeLabel(neighbor) + " (" + neighbor + ")");

					weight = 1 / (double) neighborCount;
					partialWetPaint = (1 - alpha) * wetPaint * weight;
					
					bcv.add(getEdgeUniqueId(predicate), partialWetPaint);
					
					if (nodeQueue.contains(neighbor)) {
						wetPaintRegister.add(neighbor, partialWetPaint);
					} else {
						nodeQueue.add(neighbor);
						wetPaintRegister.put(neighbor, partialWetPaint);
					}
					
					// When we consider this literal neighbor later we have to remember with predicate we used
					// This way we don't visit nodes that have a different relationship with the literal
					if(isLiteral(neighbor) || isLiteral(focusNode)) ignoreMap.put(neighbor, focusNode);
					if(isLiteral(neighbor)) filterPredicate.put(neighbor, predicate);
				}
				// System.out.println();
			}
		}
		// System.out.println("-------------------------------------------------------------------");
		return bcv;
	}
}
