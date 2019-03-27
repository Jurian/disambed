package org.uu.nl.embedding.glove.opt;

public interface Optimizer {
	Optimum optimize();
	String getName();
}
