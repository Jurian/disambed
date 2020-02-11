package org.uu.nl.embedding.bca.util;

import org.uu.nl.embedding.Settings;
import org.uu.nl.embedding.util.rnd.ExtendedRandom;

import java.util.HashMap;


/**
 * This class represents a bookmark coloring vector
 * @author Jurian Baas
 *
 */
public class BCV extends HashMap<Integer, Double> {

	protected static final Settings settings = Settings.getInstance();
	private static final ExtendedRandom random = settings.getThreadLocalRandom();
	private static final long serialVersionUID = 1L;
	
	private final int rootNode;

	public int getRootNode() {
		return this.rootNode;
	}

	public BCV(int rootNode) {
		this.rootNode = rootNode;
	}
	
	/**
	 * Add the value to a key, or create a new 
	 * record if the key was not present before
	 * @param key key with which the specified value is to be associated
	 * @param value value to be associated with the specified key
	 */
	public void add(Integer key, Double value) {
		super.put(key, getOrDefault(key, 0d) + value);
	}

	/**
	 * Removes the diagonal value and changes the other values to sum to 1
	 */
	public void normalize() {
		double sum = sum();
		for(Entry<Integer, Double> entry : entrySet())
			entry.setValue(entry.getValue() / sum);
		remove(rootNode);
	}
	
	/**
	 * @return The maximum value for this BCV
	 */
	public double max() {
		return values().stream().mapToDouble(d->d).max().orElse(0d);
	}
	
	/**
	 * @return The total sum of all values in this BCV
	 */
    private double sum() {
		return values().stream().mapToDouble(d->d).sum();
	}
	
	/**
	 * Merge this BCV with another BCV (usually using the same root-node
	 * but in reverse order)
	 * @param other The other BCV
	 */
	public void merge(BCV other) {
		for(Entry<Integer, Double> entry : other.entrySet())
			add(entry.getKey(), entry.getValue());
	}

	public void negativeSampling(int vertices, int samples) {
		for(int i = 0; i < samples; i++) {
			int v;
			do {
				v = random.uniform(vertices);
			} while (containsKey(v));

			put(v, Double.MIN_VALUE);
		}
	}
	
}