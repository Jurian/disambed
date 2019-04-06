package org.uu.nl.embedding.bca.util;

import java.util.HashMap;

/**
 * Convenience class for keeping track of the wet paint for
 * the bookmark coloring algorithm
 * @author Jurian Baas
 *
 */
public class PaintRegistry<T> extends HashMap<T, Double> {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Add the value to a key, or create a new 
	 * record if the key was not present before
	 * @param key key with which the specified value is to be associated
	 * @param value value to be associated with the specified key
	 */
	public void add(T key, Double value) {
		super.put(key, getOrDefault(key, 0d) + value);
	}
}