package org.uu.nl.embedding.ann.kdtree;

import java.util.List;

import org.uu.nl.embedding.ann.kdtree.util.ValueDistanceTuple;

public interface ANNValueSearch<T> {
	  /**
	   * @return the k nearest neighbors to the given vector.
	   */
	  public List<ValueDistanceTuple<T>> getValueNearestNeighbours(double[] vec, int k);

	  /**
	   * @return nearest neighbors to the given vector within the given radius.
	   */
	  public List<ValueDistanceTuple<T>> getValueNearestNeighbours(double[] vec, double radius);

	  /**
	   * @return the k nearest neighbors to the given vector within the given
	   *         radius.
	   */
	  public List<ValueDistanceTuple<T>> getValueNearestNeighbours(double[] vec, int k, double radius);

	  /**
	   * @return the payloads within the range of the lower and upper
	   *         bounded vectors.
	   */
	  public List<ValueDistanceTuple<T>> rangeValueQuery(double[] lower, double[] upper);
	  
}
