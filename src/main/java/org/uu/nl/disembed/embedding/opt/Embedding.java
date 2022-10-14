package org.uu.nl.disembed.embedding.opt;

import java.util.Iterator;

/**
 * @author Jurian Baas
 */
public class Embedding implements Iterable<Optimizer.EmbeddedEntity>{

	private final int size;
	private final int dimension;
	private final String[] keys;
	private final float[][] vectors;

	public Embedding(int dimension, int size) {
		this.dimension = dimension;
		this.size = size;
		this.keys = new String[size];
		this.vectors = new float[size][dimension];
	}

	public Embedding(String[] keys, float[][] vectors) {
		this.dimension = vectors[0].length;
		this.size = vectors.length;
		this.keys = keys;
		this.vectors = vectors;
	}

	public void setKey(int i, String key) {
		this.keys[i] = key;
	}

	public void setVector(int i, float[] vector){
		this.vectors[i] = vector;
	}

	public String[] getKeys() {
		return keys;
	}

	public float[][] getVectors() {
		return vectors;
	}

	@Override
	public Iterator<Optimizer.EmbeddedEntity> iterator() {
		return new Iterator<>() {
			int i = 0;

			@Override
			public boolean hasNext() {
				return i < size;
			}

			@Override
			public Optimizer.EmbeddedEntity next() {
				final Optimizer.EmbeddedEntity entity = new Optimizer.EmbeddedEntity(
						i,
						keys[i],
						vectors[i]
				);

				i++;
				return entity;
			}
		};
	}

	public int getSize() {
		return size;
	}

	public int getDimension() {
		return dimension;
	}
}
