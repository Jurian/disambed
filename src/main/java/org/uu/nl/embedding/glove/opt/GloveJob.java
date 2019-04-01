package org.uu.nl.embedding.glove.opt;

import java.util.concurrent.Callable;

/**
 * @author Jurian Baas
 */
public abstract class GloveJob implements Callable<Double> {
	
	final int id;
	
	public GloveJob(int id) {
		this.id = id;
	}
}
