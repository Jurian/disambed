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
	private final boolean reverse;
	protected final double alpha, epsilon;
	protected final Grph graph;

	protected BCAJob(
            int bookmark, boolean reverse,
            double alpha, double epsilon,
            Grph graph) {

		this.reverse = reverse;
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

	protected int getEdge(int source, int destination, int[] out, int[] in) {
		if (out.length == 0 || in.length == 0) {
			return -1;
		} else {
			if (out.length < in.length) {
				for(int e : out)
					if (graph.getDirectedSimpleEdgeHead(e) == destination) return e;
			} else {
				for(int e : in)
					if (graph.getDirectedSimpleEdgeTail(e) == source) return e;
			}
			return -1;
		}
	}

	protected boolean isLiteral(int n) {
		return graph.getVertexColorProperty().getValue(n) ==  NodeInfo.LITERAL;
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
