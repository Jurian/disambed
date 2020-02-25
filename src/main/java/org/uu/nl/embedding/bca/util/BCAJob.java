package org.uu.nl.embedding.bca.util;

import grph.Grph;
import org.uu.nl.embedding.convert.util.NodeInfo;

import java.util.concurrent.Callable;

/**
 * One BCAJob represents performing the bookmark coloring algorithm for a single node
 * @author Jurian Baas
 *
 */
public abstract class BCAJob implements Callable<BCV> {

	protected final int bookmark;
	protected final boolean reverse, predicates;
	protected final double alpha, epsilon;
	private final Grph graph;

	protected BCAJob(
            int bookmark, boolean reverse, boolean predicates,
            double alpha, double epsilon,
            Grph graph) {

		this.reverse = reverse;
		this.predicates = predicates;
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

	protected boolean isLiteral(int n) {
		return graph.getVertexColorProperty().getValue(n) ==  NodeInfo.LITERAL.id;
	}

	protected String nodeLabel(int n) {
		return graph.getVertexLabelProperty().getValueAsString(n);
	}

	protected String edgeLabel(int e) {
		return graph.getEdgeLabelProperty().getValueAsString(e);
	}

	protected int getEdgeType(int e) {
		return graph.getEdgeColorProperty().getValueAsInt(e);
	}
}
