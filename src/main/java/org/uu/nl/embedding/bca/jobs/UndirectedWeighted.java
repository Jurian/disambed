package org.uu.nl.embedding.bca.jobs;

import org.uu.nl.embedding.bca.util.BCAJob;
import org.uu.nl.embedding.util.InMemoryRdfGraph;

/**
 * @author Jurian Baas
 */
public class UndirectedWeighted extends BCAJob {

	public UndirectedWeighted(
			InMemoryRdfGraph graph, int bookmark,
			double alpha, double epsilon,
			int[][] vertexIn, int[][] vertexOut, int[][] edgeIn, int[][] edgeOut) {
		super(bookmark, true, alpha, epsilon, graph, vertexOut, vertexIn, edgeOut, edgeIn);
	}

	@Override
	protected int[] getIndexes(boolean reverse, int focusNode, int[][] indexIn, int[][] indexOut) {

		int[] index = new int[indexIn[focusNode].length + indexOut[focusNode].length];
		System.arraycopy(indexIn[focusNode], 0, index, 0, indexIn[focusNode].length);
		System.arraycopy(indexOut[focusNode], 0, index, indexIn[focusNode].length, indexOut[focusNode].length);

		return index;
	}
}
