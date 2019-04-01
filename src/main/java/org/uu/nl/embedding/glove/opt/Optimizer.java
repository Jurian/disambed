package org.uu.nl.embedding.glove.opt;

/**
 * @author Jurian Baas
 */
public interface Optimizer {
	Optimum optimize();
	String getName();
}
