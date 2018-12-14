package org.uu.nl.analyze.bca.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.shared.Lock;
import org.apache.jena.util.iterator.ExtendedIterator;

/**
 * One BCAJob represents performing the bookmark coloring algorithm for a single node
 * @author Jurian Baas
 *
 */
public abstract class BCAJob implements Callable<BCV> {

	protected final Node bookmark;
	protected final boolean reverse, normalize;
	protected final double alpha, epsilon;
	protected final Map<Node, Integer> keys;
	protected final Model model;

	public BCAJob(
			Node bookmark, boolean reverse, boolean normalize,
			double alpha, double epsilon, 
			Map<Node, Integer> keys, 
			Model model) {

		this.reverse = reverse;
		this.normalize = normalize;
		this.bookmark = bookmark;
		this.model = model;
		this.alpha = alpha;
		this.epsilon = epsilon;
		this.keys = keys;
	}
	
	@Override
	public BCV call() {

		final Graph graph = model.getGraph();
		model.enterCriticalSection(Lock.READ);
		try {
			final BCV bcv = doWork(graph, false);
			if(this.reverse)
				bcv.merge(doWork(graph, true));
			return bcv;
		} finally {
			model.leaveCriticalSection();
		}
	}
	
	/**
	 * Since Jena only provides iterators and we would like to know the number
	 * of neighbours beforehand, we collect them here
	 * @param graph Graph to query
	 * @param s Subject pattern
	 * @param p Predicate pattern
	 * @param o Object pattern
	 * @return A list of all triples that matched the pattern
	 */
	protected List<Triple> collect(Graph graph, Node s, Node p, Node o) {
		final ExtendedIterator<Triple> nodes = graph.find(s, p, o);
		final List<Triple> collection = new ArrayList<>();
		try {
			while(nodes.hasNext()) collection.add(nodes.next());
		} finally {
			nodes.close();
		}
		return collection;
	}
	
	/**
	 * Since Jena only provides iterators and we would like to know the number
	 * of neighbours beforehand, we collect them here. This method is used for
	 * literal nodes only, where we would like to find other nodes that have the 
	 * same relationship with a given literal node.
	 * @param graph Graph to query
	 * @param predicate The predicate that we used when we arrived at this literal
	 * @param literal The literal we arrived at
	 * @return A list of all triples that matched the pattern
	 */
	protected List<Triple> collectLiteralIncoming(Graph graph, Node predicate, Node literal) {
		return this.collect(graph, Node.ANY, predicate, literal);
	}
	
	/**
	 * Convenience method for collecting all nodes that have an edge coming from the given node
	 * @param graph Graph to query
	 * @param node The current node
	 * @return A list of all triples that matched the pattern
	 */
	protected List<Triple> collectOutgoing(Graph graph, Node node) {
		return this.collect(graph, node, Node.ANY, Node.ANY);
	}
	
	/**
	 * Convenience method for collecting all nodes that have an edge coming towards the given node
	 * @param graph Graph to query
	 * @param node The current node
	 * @return A list of all triples that matched the pattern
	 */
	protected List<Triple> collectIncoming(Graph graph, Node node) {
		return this.collect(graph, Node.ANY, Node.ANY, node);
	}
	
	protected abstract BCV doWork(Graph graph, boolean reverse);

}