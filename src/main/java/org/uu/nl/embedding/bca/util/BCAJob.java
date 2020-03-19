package org.uu.nl.embedding.bca.util;

import org.uu.nl.embedding.util.InMemoryRdfGraph;

import java.util.concurrent.Callable;

/**
 * One BCAJob represents performing the bookmark coloring algorithm for a single node
 * @author Jurian Baas
 *
 */
public abstract class BCAJob implements Callable<BCV> {

	protected final int bookmark;
	protected final boolean reverse;
	protected final double alpha, epsilon;
	private final InMemoryRdfGraph graph;

	protected BCAJob(
            int bookmark, boolean reverse,
            double alpha, double epsilon,
			InMemoryRdfGraph graph) {

		this.reverse = reverse;
		this.bookmark = bookmark;
		this.alpha = alpha;
		this.epsilon = epsilon;
		this.graph = graph;
	}

	@Override
	public BCV call() {
		final BCV bcv = doWork(graph, false);
		if (this.reverse) bcv.merge(doWork(graph, true));
		return bcv;
	}

	protected abstract BCV doWork(final InMemoryRdfGraph graph, final boolean reverse);
}
