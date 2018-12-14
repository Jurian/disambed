package org.uu.nl.analyze.bca.util;

import java.util.HashMap;
import java.util.Map;


/**
 * This class represents a bookmark coloring vector
 * @author Jurian Baas
 *
 */
public class BCV extends HashMap<Integer, Double> {

	private static final long serialVersionUID = 1L;
	
	public final int rootNode;
	
	public BCV(int rootNode) {
		this.rootNode = rootNode;
	}
	
	/**
	 * Add the value to a key, or create a new 
	 * record if the key was not present before
	 * @param key key with which the specified value is to be associated
	 * @param value value to be associated with the specified key
	 * @return the previous value associated with key, or null if there was no mapping for key.
		(A null return can also indicate that the map previously associated null with key.)
	 */
	public Double add(Integer key, Double value) {
		return super.put(key, getOrDefault(key, 0d) + value);
	}

	/**
	 * Removes the diagonal value and changes the other values to sum to 1
	 */
	public void normalize() {
		remove(rootNode);
		double sum = sum();
		for(Entry<Integer, Double> entry : entrySet())
			entry.setValue(entry.getValue() / sum);
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
	public double sum() {
		return values().stream().mapToDouble(d->d).sum();
	}
	
	/**
	 * Merge this BCV with another BCV (usually using the same root-node
	 * but in reverse order)
	 * @param other
	 */
	public void merge(BCV other) {
		for(Entry<Integer, Double> entry : other.entrySet())
			add(entry.getKey(), entry.getValue());
	}
	
	/**
	 * Used for gathering all the BCV's into a single map for use in GloVe
	 * @param matrix The final map that contains all BCV's
	 */
	public void addTo(Map<OrderedIntegerPair, Double> matrix) {
		forEach((key, value) -> matrix.put(new OrderedIntegerPair(rootNode, key), value));
	}
	
}