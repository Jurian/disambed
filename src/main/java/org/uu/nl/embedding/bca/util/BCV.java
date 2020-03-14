package org.uu.nl.embedding.bca.util;

import org.apache.commons.math.util.FastMath;
import org.uu.nl.embedding.util.config.Configuration;
import org.uu.nl.embedding.util.rnd.ExtendedRandom;

import java.util.HashMap;


/**
 * This class represents a bookmark coloring vector
 * @author Jurian Baas
 *
 */
public class BCV extends HashMap<Integer, Float> {

	private static final ExtendedRandom random = Configuration.getThreadLocalRandom();
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
	public void add(int key, float value) {
		super.put(key, getOrDefault(key, 0f) + value);
	}

	/**
	 * Add the value to a key, or create a new
	 * record if the key was not present before
	 * @param key key with which the specified value is to be associated
	 * @param value value to be associated with the specified key
	 */
	public void add(int key, double value) {
		this.add(key, (float)value);
	}

	/**
	 * Removes the diagonal value and changes the other values to sum to 1
	 */
	public void normalize() {
		remove(rootNode);
		final int size = size();
		if(size == 1) {
			// Prevent Xij == 1
			for(Entry<Integer, Float> entry : entrySet())
				entry.setValue(1 - 1e-7f);
		} else if( size > 1){
			final float sum = sum();
			for(Entry<Integer, Float> entry : entrySet())
				entry.setValue(entry.getValue() / sum);
		}
	}
	
	/**
	 * @return The maximum value for this BCV
	 */
	public float max() {
		float max = 0f;
		for(Float value : values())
			max = FastMath.max(max,value);
		return max;
	}
	
	/**
	 * @return The total sum of all values in this BCV
	 */
    private float sum() {
    	float sum = 0f;
		for(Float value : values())
			sum += value;
		return sum;
	}
	
	/**
	 * Merge this BCV with another BCV (usually using the same root-node
	 * but in reverse order)
	 * @param other The other BCV
	 */
	public void merge(BCV other) {
		other.forEach((key, value2) -> merge(key, value2, (v1, v2) -> 2 * v1 * v2 / (v1 + v2)));
	}
	
}