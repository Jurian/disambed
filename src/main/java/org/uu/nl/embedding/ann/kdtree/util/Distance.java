package org.uu.nl.embedding.ann.kdtree.util;

import org.apache.commons.math.util.FastMath;

public class Distance {
	
	/**
	 * Compute the Euclidean (L2) distance between two vectors. This version makes direct use
	 * of the 1-dimensional array representation of the vectors so that only read 
	 * operations have to be done.
	 * @param v The 1-dimensional array containing the vectors
	 * @param dimension The number of dimensions each vector has
	 * @param a Index of the first vector
	 * @param b Index of the second vector
	 * @return Euclidean distance between a and b
	 */
	public static double euclidean(double[] v, int dimension, int a, int b) {
		double distance = 0;
		double temp = 0;
		for(int d = 0; d < dimension; d++) {
			temp = v[a * dimension + d] - v[b * dimension + d];
			distance += temp * temp;
		}
		return FastMath.sqrt(distance);
	}
	
	/**
	 * Compute the Euclidean (L2) distance between two vectors. This version makes direct use
	 * of the 1-dimensional array representation of the vectors so that only read 
	 * operations have to be done.
	 * @param v The 1-dimensional array containing the vectors
	 * @param dimension The number of dimensions each vector has
	 * @param a Index of the first vector
	 * @param b The second vector
	 * @return Euclidean distance between a and b
	 */
	public static double euclidean(double[] v, int dimension, int a, double[] b) {
		double distance = 0;
		double temp = 0;
		for(int d = 0; d < dimension; d++) {
			temp = v[a * dimension + d] - b[d];
			distance += temp * temp;
		}
		return FastMath.sqrt(distance);
	}
	
	/**
	 * Compute the Euclidean (L2) distance between two vectors. 
	 * Note that no checks are made for size difference between a and b!
	 * @param a The first vector
	 * @param b The second vector
	 * @return Euclidean distance between a and b
	 */
	public static double euclidean(double[] a, double[] b) {
		double distance = 0;
		double temp = 0;
		for(int i = 0; i < a.length; i++) {
			temp = (a[i] - b[i]);
			distance += temp * temp;
		}
		return FastMath.sqrt(distance);
	}
	
	/**
	 * Compute the Manhattan (L1) distance between two vectors. This version makes direct use
	 * of the 1-dimensional array representation of the vectors so that only read 
	 * operations have to be done.
	 * @param v The 1-dimensional array containing the vectors
	 * @param dimension The number of dimensions each vector has
	 * @param a Index of the first vector
	 * @param b Index of the second vector
	 * @return Manhattan distance between a and b
	 */
	public static double manhattan(double[] v, int dimension, int a, int b) {
		double distance = 0;
		for(int d = 0; d < dimension; d++) 
			distance += FastMath.abs(v[a * dimension + d] - v[b * dimension + d]);
		return FastMath.sqrt(distance);
	}
	
	/**
	 * Compute the Manhattan (L1) distance between two vectors. 
	 * Note that no checks are made for size difference between a and b!
	 * @param a The first vector
	 * @param b The second vector
	 * @return Manhattan distance between a and b
	 */
	public static double manhattan(double[] a, double[] b) {
		double distance = 0;
		for(int i = 0; i < a.length; i++) 
			distance += FastMath.abs(a[i] - b[i]);
		return distance;
	}
}
