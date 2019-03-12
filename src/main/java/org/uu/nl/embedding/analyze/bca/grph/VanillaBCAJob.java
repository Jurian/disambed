package org.uu.nl.embedding.analyze.bca.grph;

import grph.Grph;
import org.uu.nl.embedding.analyze.bca.grph.util.BCAJob;
import org.uu.nl.embedding.analyze.bca.util.BCV;
import org.uu.nl.embedding.analyze.bca.util.PaintRegistry;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class VanillaBCAJob extends BCAJob {

	private final Map<Integer, BCV> computedBCV;
	private final int[][] out, in;
	
	public VanillaBCAJob(Grph graph, Map<Integer, BCV> computedBCV, 
			int bookmark, boolean reverse, boolean normalize, double alpha, double epsilon,
			int[][] in, int[][] out) {
		super(bookmark, reverse, normalize, alpha, epsilon, graph);
		this.computedBCV = computedBCV;
		this.out = out;
		this.in = in;
	}

	public String nodeLabel(int n) {
		return graph.getVertexLabelProperty().getValueAsString(n);
	}
	
	public int getEdgeType(int e) {
		return graph.getEdgeColorProperty().getValueAsInt(e);
	}
	
	@Override
	protected BCV doWork(Grph graph, boolean reverse) {
		
		final Queue<Integer> nodeQueue = new LinkedList<>();
		final PaintRegistry<Integer> wetPaintRegister = new PaintRegistry<>();
		final BCV bcv = new BCV(bookmark);
		
		nodeQueue.add(bookmark);
		wetPaintRegister.put(bookmark, 1d);
		
		int[] neighbors, edgeCache;
		int focusNode, neighbor, neighborCount, predicate;
		double weight, partialWetPaint;
		BCV precomputed;
		
		while (!nodeQueue.isEmpty()) {

			focusNode = nodeQueue.poll();
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
				if (wetPaint < epsilon)
					continue;

				if(reverse) neighbors = in[focusNode];
				else neighbors = out[focusNode];

				neighborCount = neighbors.length;
				
				if(reverse) edgeCache = graph.getInOnlyEdges(focusNode).toIntArray();
				else edgeCache = graph.getOutOnlyEdges(focusNode).toIntArray();
				
				for (int i = 0; i < neighbors.length; i++) {
					
					neighbor = neighbors[i];
					weight = 1 / (double) neighborCount;
					partialWetPaint = (1 - alpha) * wetPaint * weight;
					
					if(reverse) predicate = getEdge(neighbor, focusNode, graph.getOutOnlyEdges(neighbor).toIntArray(), edgeCache);
					else predicate = getEdge(focusNode, neighbor, edgeCache, graph.getInOnlyEdges(neighbor).toIntArray());

					bcv.add(getEdgeType(predicate), partialWetPaint);
					
					if (nodeQueue.contains(neighbor)) {
						wetPaintRegister.add(neighbor, partialWetPaint);
					} else {
						nodeQueue.add(neighbor);
						wetPaintRegister.put(neighbor, partialWetPaint);
					}
				}
			}
		}
		return bcv;
	}
	
	private int getEdge(int src, int dest, int[] out, int[] in) {
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
