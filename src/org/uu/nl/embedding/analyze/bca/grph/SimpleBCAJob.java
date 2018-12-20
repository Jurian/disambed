package org.uu.nl.embedding.analyze.bca.grph;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.uu.nl.embedding.analyze.bca.grph.util.BCAJob;
import org.uu.nl.embedding.analyze.bca.util.BCV;
import org.uu.nl.embedding.analyze.bca.util.PaintRegistry;

import grph.Grph;

public class SimpleBCAJob extends BCAJob {

	private final Map<Integer, BCV> computedBCV;
	private final int[][] out;
	
	public SimpleBCAJob(Map<Integer, BCV> computedBCV, int bookmark, boolean reverse, boolean normalize, double alpha, double epsilon, Grph graph, int[][] out) {
		super(bookmark, reverse, normalize, alpha, epsilon, graph);
		this.computedBCV = computedBCV;
		this.out = out;
	}

	public String nodeLabel(int n) {
		return graph.getVertexLabelProperty().getValueAsString(n);
	}

	@Override
	protected BCV doWork(Grph graph, boolean reverse) {
		
		final Queue<Integer> nodeQueue = new LinkedList<>();
		final PaintRegistry<Integer> wetPaintRegister = new PaintRegistry<>();
		final BCV bcv = new BCV(bookmark);
		
		nodeQueue.add(bookmark);
		wetPaintRegister.put(bookmark, 1d);
		
		int[] neighbors, edges;
		int focusNode, neighbor, neighborCount, predicate;
		double weight, partialWetPaint;
		BCV precomputed;
		
		while (!nodeQueue.isEmpty()) {

			focusNode = nodeQueue.poll();
			final double wetPaint = wetPaintRegister.get(focusNode);

			// System.out.println("Focus: " + nodeLabel(focusNode));
			
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

				neighbors = out[focusNode];
				edges = graph.getOutEdges(focusNode).toIntArray();
				neighborCount = neighbors.length;
				
				// System.out.println("Neighbors:");
				for (int i = 0; i < neighbors.length; i++) {
					
					neighbor = neighbors[i];
					
					// System.out.println(nodeLabel(neighbor));

					weight = 1 / (double) neighborCount;
					partialWetPaint = (1 - alpha) * wetPaint * weight;
					
					predicate = edges[i];
					bcv.add(predicate, partialWetPaint);
					
					if (nodeQueue.contains(neighbor)) {
						wetPaintRegister.add(neighbor, partialWetPaint);
					} else {
						nodeQueue.add(neighbor);
						wetPaintRegister.put(neighbor, partialWetPaint);
					}
				}
				// System.out.println();
			}
		}
		return bcv;
	}
}
