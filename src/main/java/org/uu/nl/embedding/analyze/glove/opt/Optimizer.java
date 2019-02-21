package org.uu.nl.embedding.analyze.glove.opt;

public interface Optimizer {
	public Optimum optimize();
	public String getName();
}
