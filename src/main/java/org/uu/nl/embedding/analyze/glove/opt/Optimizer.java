package org.uu.nl.embedding.analyze.glove.opt;

public interface Optimizer {
	Optimum optimize();
	String getName();
}
