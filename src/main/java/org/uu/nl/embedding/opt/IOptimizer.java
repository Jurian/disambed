package org.uu.nl.embedding.opt;

/**
 * @author Jurian Baas
 */
public interface IOptimizer {
	Optimum optimize() throws OptimizationFailedException;
	String getName();
	OptimizeJob createJob(int id, int iteration);
}
