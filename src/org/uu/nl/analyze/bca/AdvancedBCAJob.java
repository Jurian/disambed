package org.uu.nl.analyze.bca;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.uu.nl.analyze.bca.util.BCAJob;
import org.uu.nl.analyze.bca.util.BCV;
import org.uu.nl.analyze.bca.util.PaintRegistry;

public class AdvancedBCAJob extends BCAJob {

	private final Map<Integer, BCV> computedBCV;
	
	public AdvancedBCAJob(Map<Integer, BCV> computedBCV, Node bookmark, boolean reverse, boolean normalize, double alpha, double epsilon, Map<Node, Integer> keys, Model model) {
		super(bookmark, reverse, normalize, alpha, epsilon, keys, model);
		this.computedBCV = computedBCV;
	}

	@Override
	protected BCV doWork(Graph graph, boolean reverse) {
		final int bookmarkIndex = keys.get(bookmark);
		final LinkedList<Node> nodeQueue = new LinkedList<>();
		final PaintRegistry wetPaintRegister = new PaintRegistry();
		final BCV bcv = new BCV(bookmarkIndex);
		
		final Map<Node, Node> usePredicate = new HashMap<>();
		final Map<Node, Node> ignoreNeighbour = new HashMap<>();
		
		nodeQueue.add(bookmark);
		wetPaintRegister.put(bookmark, 1d);
		
		while (!nodeQueue.isEmpty()) {

			final Node focusNode = nodeQueue.poll();
			final double wetPaint = wetPaintRegister.get(focusNode);

			// Keep part of the available paint on this node, distribute the rest
			final int focusNodeIndex = keys.get(focusNode);
			System.out.println("Focus: " + focusNode.toString() + " (" + focusNodeIndex + ")");
			
			final BCV precomputed = computedBCV.get(focusNodeIndex);
			if(precomputed != null) {
				precomputed.forEach((index, precomputedPaint) -> {
					final double scaled = precomputedPaint * wetPaint;
					if(scaled > (epsilon * alpha)) bcv.add(index, scaled);
					if(scaled > (epsilon * alpha)) System.out.println("Precomputed: " + index + ": " + scaled);
				});
			} else {
				bcv.add(focusNodeIndex, (alpha * wetPaint));

				// If there is not enough paint we stop and don't distribute among the neighbors
				if (wetPaint < epsilon)
					continue;

				final List<Triple> neighbours; 
				// We do something different in case of a literal node
				if(focusNode.isLiteral()) {
					// No real difference in this case with reverse mode, literal nodes can only have  incoming edges anyway
					neighbours = collectLiteralIncoming(graph, usePredicate.get(focusNode), focusNode);
					// Remove this node from the map in case we reach it again from a different predicate
					System.out.println("Came in from " + usePredicate.get(focusNode) + " and " + ignoreNeighbour.get(focusNode));
					usePredicate.remove(focusNode);
				} else {
					if(reverse) neighbours = collectIncoming(graph, focusNode);
					else neighbours = collectOutgoing(graph, focusNode);
				}
				
				final Node ignored = ignoreNeighbour.get(focusNode);
				// Remove the ignored node (if present) from the list
				// We do this first so the total neighbor count is accurate
				if(ignored != null) {
					for(int i = 0; i < neighbours.size(); i++) {
						final Triple t = neighbours.get(i);
						
						// In reverse mode or when the focus node is a literal, we need the subject instead of the object
						final Node neighbour = reverse || focusNode.isLiteral() ? t.getSubject() : t.getObject();
						if(neighbour.equals(ignored)) {
							neighbours.remove(i);
							ignoreNeighbour.remove(focusNode);
							break;
						}
					}
				}
				final long neighbourCount = neighbours.size();
				System.out.println("Neighbours:");
				for (Triple t : neighbours) {
					System.out.println(t.toString());
					// In reverse mode or when the focus node is a literal, we need the subject instead of the object
					final Node neighbour = reverse || focusNode.isLiteral() ? t.getSubject() : t.getObject();
					
					final Node predicate = t.getPredicate();
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
