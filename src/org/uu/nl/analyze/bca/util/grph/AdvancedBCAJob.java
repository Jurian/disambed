package org.uu.nl.analyze.bca.util.grph;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.uu.nl.analyze.bca.util.BCV;
import org.uu.nl.analyze.bca.util.PaintRegistry;
import org.uu.nl.convert.Rdf2GrphConverter;

import grph.Grph;
import grph.properties.NumericalProperty;

public class AdvancedBCAJob extends BCAJob {

	private final Map<Integer, BCV> computedBCV;
	private final int[][] in, out;
	
	public AdvancedBCAJob(Map<Integer, BCV> computedBCV, 
			int bookmark, boolean reverse, boolean normalize, double alpha, double epsilon, 
			Grph graph, int[][] in, int[][] out) {
		super(bookmark, reverse, normalize, alpha, epsilon, graph);
		this.computedBCV = computedBCV;
		this.in = in;
		this.out = out;
	}

	@Override
	protected BCV doWork(Grph graph, boolean reverse) {
		
		final int bookmarkIndex = this.bookmark;
		final LinkedList<Integer> nodeQueue = new LinkedList<>();
		final PaintRegistry<Integer> wetPaintRegister = new PaintRegistry<>();
		final BCV bcv = new BCV(bookmarkIndex);
		
		final Map<Integer, Integer> usePredicate = new HashMap<>();
		final Map<Integer, Integer> ignoreNeighbour = new HashMap<>();
		
		final NumericalProperty types = graph.getEdgeColorProperty();
		
		nodeQueue.add(bookmark);
		wetPaintRegister.put(bookmark, 1d);
		
		while (!nodeQueue.isEmpty()) {

			final int focusNode = nodeQueue.poll();
			final double wetPaint = wetPaintRegister.get(focusNode);

			// Keep part of the available paint on this node, distribute the rest
			//final int focusNodeIndex = keys.get(focusNode);
			System.out.println("Focus: " + focusNode);
			
			final BCV precomputed = computedBCV.get(focusNode);
			if(precomputed != null) {
				precomputed.forEach((index, precomputedPaint) -> {
					final double scaled = precomputedPaint * wetPaint;
					if(scaled > (epsilon * alpha)) bcv.add(index, scaled);
					if(scaled > (epsilon * alpha)) System.out.println("Precomputed: " + index + ": " + scaled);
				});
			} else {
				bcv.add(focusNode, (alpha * wetPaint));

				// If there is not enough paint we stop and don't distribute among the neighbors
				if (wetPaint < epsilon)
					continue;

				//The set of vertices adjacent to vertex v are found in the array in[v], out[v]
				final int ignoredIndex = ignoreNeighbour.getOrDefault(focusNode, -1);
				final int[] neighbors;
				final int[] edges;
				final boolean[] ignored;
				int neighborCount;
	
				// We do something different in case of a literal node
				if(types.getValue(focusNode) == Rdf2GrphConverter.LITERAL) {
					
					final int predicate = usePredicate.get(focusNode);
					
					neighbors = in[focusNode];
					neighborCount = neighbors.length;
					ignored = new boolean[neighbors.length];
					edges = graph.getInEdges(focusNode).toIntArray();
					
					for(int i = 0; i < neighbors.length; i++) {
						if(neighbors[i] == ignoredIndex || edges[i] == predicate) {
							ignored[i] = true;
							neighborCount--;
						}
					}

					// Remove this node from the map in case we reach it again from a different predicate
					System.out.println("Came in from " + usePredicate.get(focusNode) + " and " + ignoreNeighbour.get(focusNode));
					usePredicate.remove(focusNode);
				} else {
					
					neighbors = out[focusNode];
					neighborCount = neighbors.length;
					ignored = new boolean[neighbors.length];
					edges = graph.getOutEdges(focusNode).toIntArray();
					
					for(int i = 0; i < neighbors.length; i++) {
						if(neighbors[i] == ignoredIndex) {
							ignored[i] = true;
							neighborCount--;
						}
					}
				}
				
				
				System.out.println("Neighbours:");
				for (int i = 0; i < neighbors.length; i++) {
					
					if(ignored[i]) continue;
					
					final int neighbor = neighbors[i];
					
					System.out.println(i);
					
					final int predicate = edges[i];
					final double weight = 1 / (double) neighborCount;
					final double partialWetPaint = (1 - alpha) * wetPaint * weight;

					bcv.add(predicate, partialWetPaint);
					
					if (nodeQueue.contains(neighbor)) {
						wetPaintRegister.add(neighbor, partialWetPaint);
					} else {
						nodeQueue.add(neighbor);
						wetPaintRegister.put(neighbor, partialWetPaint);
					}
					
					// When we consider this literal neighbor later we have to remember with predicate we used
					// This way we don't visit nodes that have a different relationship with the literal
					if(types.getValue(neighbor) == Rdf2GrphConverter.LITERAL || types.getValue(focusNode) == Rdf2GrphConverter.LITERAL) ignoreNeighbour.put(neighbor, focusNode);
					if(types.getValue(neighbor) == Rdf2GrphConverter.LITERAL) usePredicate.put(neighbor, predicate);
				}
				System.out.println();
			}
		}
		return bcv;
	}
}
