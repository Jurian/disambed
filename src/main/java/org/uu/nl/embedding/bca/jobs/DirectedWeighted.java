package org.uu.nl.embedding.bca.jobs;

import org.uu.nl.embedding.bca.util.BCAJob;
import org.uu.nl.embedding.util.InMemoryRdfGraph;

/**
 * @author Jurian Baas
 */
public class DirectedWeighted extends BCAJob {

	public DirectedWeighted(
            InMemoryRdfGraph graph, int bookmark,
            double alpha, double epsilon,
			int[][] vertexIn, int[][] vertexOut, int[][] edgeIn, int[][] edgeOut) {
		super(bookmark, true, alpha, epsilon, graph, vertexOut, vertexIn, edgeOut, edgeIn);
	}

    @Override
    protected int[] getIndexes(boolean reverse, int focusNode, int[][] indexIn, int[][] indexOut) {
        return reverse ? indexIn[focusNode] : indexOut[focusNode];
    }
}
