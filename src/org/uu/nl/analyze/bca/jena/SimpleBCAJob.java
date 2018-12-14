package org.uu.nl.analyze.bca.jena;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.uu.nl.analyze.bca.util.BCV;
import org.uu.nl.analyze.bca.util.PaintRegistry;
import org.uu.nl.analyze.bca.util.jena.BCAJob;

public class SimpleBCAJob extends BCAJob {

	public SimpleBCAJob(Node bookmark, boolean reverse, boolean normalize, double alpha, double epsilon,
			Map<Node, Integer> keys, Model model) {
		super(bookmark, reverse, normalize, alpha, epsilon, keys, model);
	}

	@Override
	protected BCV doWork(Graph graph, boolean reverse) {
		final int bookmarkIndex = keys.get(bookmark);
		final LinkedList<Node> nodeQueue = new LinkedList<>();
		final PaintRegistry<Node> wetPaintRegister = new PaintRegistry<>();
		final BCV bcv = new BCV(bookmarkIndex);
		
		nodeQueue.add(bookmark);
		wetPaintRegister.put(bookmark, 1d);
		
		while (!nodeQueue.isEmpty()) {

			final Node focusNode = nodeQueue.poll();
			final double wetPaint = wetPaintRegister.get(focusNode);

			// Keep part of the available paint on this node, distribute the rest
			final int focusNodeIndex = keys.get(focusNode);

			bcv.add(focusNodeIndex, (alpha * wetPaint));

			// If there is not enough paint we stop and don't distribute among the neighbors
			if (wetPaint < epsilon)
				continue;

			final List<Triple> neighbours; 
			if(reverse) neighbours = collect(graph, Node.ANY, Node.ANY, focusNode);
			else neighbours = collect(graph, focusNode, Node.ANY, Node.ANY);

			final long neighbourCount = neighbours.size();
			
			for (Triple t : neighbours) {
				// In reverse mode we need the subject instead of the object
				final Node neighbour = reverse ? t.getSubject() : t.getObject();
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
			}

		}
		return bcv;
	}

}
