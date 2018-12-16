package org.uu.nl.embedding.ann.kdtree.util;

/**
 * Higher dimensional rectangle, modified to use primitive arrays
 * 
 * @author Jurian Baas & Thomas Jungblut (original)
 * @see <a href=
 *      "https://github.com/thomasjungblut/JRPT/blob/master/src/de/jungblut/jrpt/HyperRectangle.java">Original</a>
 */
public final class HyperRectangle {

	public final double[] min;
	public final double[] max;

	public HyperRectangle(double[] min, double[] max) {
		this.min = min;
		this.max = max;
	}

	public double[] closestPoint(double[] t) {
		double[] p = new double[t.length];
		for (int i = 0; i < t.length; ++i) {
			if (t[i] <= min[i]) {
				p[i] = min[i];
			} else if (t[i] >= max[i]) {
				p[i] = max[i];
			} else {
				p[i] = t[i];
			}
		}
		return p;
	}
	
	public HyperRectangle copy() {
		final double[] min = new double[this.min.length], max = new double[this.max.length];
		System.arraycopy(this.min, 0, min, 0, min.length);
		System.arraycopy(this.max, 0, max, 0, max.length);
		return new HyperRectangle(min, max);
	}

	public static HyperRectangle infiniteHyperRectangle(int dimension) {
		double[] min = new double[dimension];
		double[] max = new double[dimension];
		for (int i = 0; i < dimension; ++i) {
			min[i] = Double.NEGATIVE_INFINITY;
			max[i] = Double.POSITIVE_INFINITY;
		}

		return new HyperRectangle(min, max);
	}

	@Override
	public String toString() {
		return "min: " + min + " ; max: " + max;
	}
}