package org.uu.nl.analyze.glove.opt;

import java.util.concurrent.Callable;

public abstract class GloveJob implements Callable<Double> {
	
	protected final int id;
	
	public GloveJob(int id) {
		this.id = id;
	}
}
