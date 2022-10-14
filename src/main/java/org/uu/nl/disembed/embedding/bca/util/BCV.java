package org.uu.nl.disembed.embedding.bca.util;

import com.carrotsearch.hppc.IntFloatHashMap;
import com.carrotsearch.hppc.cursors.IntFloatCursor;
import org.apache.commons.math.util.FastMath;
import org.uu.nl.disembed.util.config.Configuration;
import org.uu.nl.disembed.util.rnd.ExtendedRandom;


/**
 * This class represents a bookmark coloring vector
 * @author Jurian Baas
 *
 */
public class BCV extends IntFloatHashMap {

	private static final ExtendedRandom random = Configuration.getThreadLocalRandom();
	private static final long serialVersionUID = 1L;

	private final int rootNode;

	public int getRootNode() {
		return this.rootNode;
	}

	public BCV(int rootNode) {
		this.rootNode = rootNode;
	}

	public BCV centerAndScale() {
		remove(rootNode);
		final float max = max();
		final float min = min();

		for(IntFloatCursor c : this) {
			values[c.index] = (0.95f-0.05f)/(max-min)*(c.value-max)+0.95f;
		}

		return this;
	}

	/**
	 * Changes the values to sum to 1
	 */
	public BCV scale() {
		remove(rootNode);

		if(size() == 1) {
			for(IntFloatCursor c : this) {
				values[c.index] = 1 - 1e-3f;
			}
			return this;
		}

		final float sum = sum();

		for(IntFloatCursor c : this) {
			values[c.index] = c.value / sum;
		}

		return this;
	}

	/**
	 * @return The minimum value for this BCV
	 */
	private float min() {
		float min = 0;
		for(IntFloatCursor c : this) {
			min = FastMath.min(min, c.value);
		}
		return min;
	}

	/**
	 * @return The maximum value for this BCV
	 */
	public float max() {
		float max = 0;
		for(IntFloatCursor c : this) {
			max = FastMath.max(max, c.value);
		}
		return max;
	}

	/**
	 * Used to scale the probabilities to some positive range
	 */
	private float scale(float a, float aMax, float aMin) {
		return (a / ((aMax - aMin) / ((float) 1000 - (float) 1))) + (float) 1;
	}

	/**
	 * @return The total sum of all values in this BCV
	 */
    private float sum() {
		float sum = 0;
		for(IntFloatCursor c : this) {
			sum += c.value;
		}
    	return sum;
	}
	
	/**
	 * Merge this BCV with another BCV (usually using the same root-node
	 * but in reverse order)
	 * @param other The other BCV
	 */
	public void merge(BCV other) {

		for(IntFloatCursor c : other) {
			this.putOrAdd(c.key, c.value, c.value);
		}
	}
	
}