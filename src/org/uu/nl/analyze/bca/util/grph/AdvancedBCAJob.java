package org.uu.nl.analyze.bca.util.grph;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.uu.nl.analyze.bca.util.BCV;
import org.uu.nl.analyze.bca.util.PaintRegistry;
import org.uu.nl.convert.Rdf2GrphConverter;

import grph.Grph;
import toools.collections.primitive.LucIntSet;

public class AdvancedBCAJob extends BCAJob {

	private final Map<Integer, BCV> computedBCV;
	
	public AdvancedBCAJob(Map<Integer, BCV> computedBCV, int bookmark, boolean reverse, boolean normalize, double alpha, double epsilon, Grph graph) {
		super(bookmark, reverse, normalize, alpha, epsilon, graph);
		this.computedBCV = computedBCV;
	}

	@Override
	protected BCV doWork(Grph graph, boolean reverse) {
		
		final int bookmarkIndex = this.bookmark;
		final LinkedList<Integer> nodeQueue = new LinkedList<>();
		final PaintRegistry<Integer> wetPaintRegister = new PaintRegistry<>();
		final BCV bcv = new BCV(bookmarkIndex);
		
		final Map<Integer, Integer> usePredicate = new HashMap<>();
		final Map<Integer, Integer> ignoreNeighbour = new HashMap<>();
		
		nodeQueue.add(bookmark);
		wetPaintRegister.put(bookmark, 1d);
		
		while (!nodeQueue.isEmpty()) {

			final Integer focusNode = nodeQueue.poll();
			final double wetPaint = wetPaintRegister.get(focusNode);

			// Keep part of the available paint on this node, distribute the rest
			//final int focusNodeIndex = keys.get(focusNode);
			System.out.println("Focus: " + focusNode.toString() );
			
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

				
				final LucIntSet neighbours, predicates;
				final int ignored = ignoreNeighbour.getOrDefault(focusNode, -1);
				
				// We do something different in case of a literal node
				if(graph.getEdgeColorProperty().getValue(focusNode) == Rdf2GrphConverter.LITERAL) {
					
					int[] edges = graph.getInEdges(focusNode).toIntArray();
					int predicate = usePredicate.get(focusNode);
					neighbours = graph.getInNeighbors(focusNode);
					neighbours.removeIf(i -> edges[i] != predicate || i == ignored);
					// Remove this node from the map in case we reach it again from a different predicate
					System.out.println("Came in from " + usePredicate.get(focusNode) + " and " + ignoreNeighbour.get(focusNode));
					usePredicate.remove(focusNode);
				} else {
					neighbours = graph.getOutNeighbors(focusNode);
					neighbours.removeIf(i -> i == ignored);
				}
				
				final long neighbourCount = neighbours.size();
				System.out.println("Neighbours:");
				for (int i : neighbours) {
					System.out.println(i);
					
					final int predicate = t.getPredicate();
					final int predicateIndex = keys.get(predicate);
					final double weight = 1 / (double) neighbourCount;
					final double partialWetPaint = (1 - alpha) * wetPaint * weight;

					bcv.add(predicateIndex, partialWetPaint);
					
					if (nodeQueue.contains(neighbour)) {
						wetPaintRegister.add(neighbour, partialWetPaint);
					} else {
						nodeQueue.add(neighbour);
						wetPaintRegister.put(neighbour, partialWetPaint);
					}
					
					// When we consider this literal neighbor later we have to remember with predicate we used
					// This way we don't visit nodes that have a different relationship with the literal
					if(neighbour.isLiteral() || focusNode.isLiteral()) ignoreNeighbour.put(neighbour, focusNode);
					if(neighbour.isLiteral()) usePredicate.put(neighbour, predicate);
				}
				System.out.println();
			}
		}
		return bcv;
	}
}
