package org.uu.nl.disembed.embedding.opt;

/**
 * @author Jurian Baas
 */
public interface IOptimizer {
	Embedding optimize() throws OptimizationFailedException;
	String getName();
	OptimizeJob createJob(int id, int iteration);
	double calculateMemoryMegaBytes();
}
