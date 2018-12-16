package org.uu.nl.embedding.analyze.bca.grph.util;

import java.util.concurrent.Callable;

import org.uu.nl.embedding.analyze.bca.util.BCV;

import grph.Grph;

/**
 * One BCAJob represents performing the bookmark coloring algorithm for a single node
 * @author Jurian Baas
 *
 */
public abstract class BCAJob implements Callable<BCV> {

	protected final int bookmark;
	protected final boolean reverse, normalize;
	protected final double alpha, epsilon;
	protected final Grph graph;

	public BCAJob(
			int bookmark, boolean reverse, boolean normalize,
			double alpha, double epsilon, 
			Grph graph) {

		this.reverse = reverse;
		this.normalize = normalize;
		this.bookmark = bookmark;
		this.alpha = alpha;
		this.epsilon = epsilon;
		this.graph = graph;
	}

	@Override
	public BCV call() {

		BCV bcv = doWork(graph, false);
		if (this.reverse)
			bcv.merge(doWork(graph, true));
		return bcv;
	}

	protected abstract BCV doWork(Grph graph, boolean reverse);
}
