package org.uu.nl.embedding.bca.jobs;

import org.uu.nl.embedding.bca.util.BCAJobNoBacksies;
import org.uu.nl.embedding.util.InMemoryRdfGraph;

/**
 * @author Jurian Baas
 */
public class UndirectedWeighted extends BCAJobNoBacksies {

	public UndirectedWeighted(
			InMemoryRdfGraph graph, int bookmark,
			double alpha, double epsilon,
			int[][] vertexNeighborhood, int[][] edgeNeighborhood) {
		super(bookmark, alpha, epsilon, graph, vertexNeighborhood, edgeNeighborhood);
	}
}
