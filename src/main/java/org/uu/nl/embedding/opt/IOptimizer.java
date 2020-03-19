package org.uu.nl.embedding.opt;

/**
 * @author Jurian Baas
 */
public interface IOptimizer {
	Optimum optimize();
	String getName();
}
