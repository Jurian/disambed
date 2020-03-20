package org.uu.nl.embedding.opt;

/**
 * @author Jurian Baas
 */
public interface IOptimizer {
	Optimum optimize();
	String getName();
	OptimizeJob createJob(int id, int iteration);
	double[] extractResult();
}
