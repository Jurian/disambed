package org.uu.nl.ann.kdtree;

import java.util.List;

import org.uu.nl.ann.kdtree.util.VectorDistanceTuple;

/**
 * Interface for searching a ANN (approx. nearest neighbour) datastructure
 * structure.
 * 
 * This version has been modified to make use of primitive arrays
 * 
 * @author Jurian Baas & Thomas Jungblut (original)
 * @see <a href="https://github.com/thomasjungblut/JRPT/blob/master/src/de/jungblut/jrpt/ANNSearch.java">Original</a>
 * @param <T> the value type of the payload
 */
public interface ANNVectorSearch<T> {

  /**
   * @return the k nearest neighbors to the given vector.
   */
  public List<VectorDistanceTuple<T>> getNearestNeighbours(double[] vec, int k);

  /**
   * @return nearest neighbors to the given vector within the given radius.
   */
  public List<VectorDistanceTuple<T>> getNearestNeighbours(double[] vec, double radius);

  /**
   * @return the k nearest neighbors to the given vector within the given
   *         radius.
   */
  public List<VectorDistanceTuple<T>> getNearestNeighbours(double[] vec, int k, double radius);

  /**
   * @return the vectors and payload within the range of the lower and upper
   *         bounded vectors.
   */
  public List<VectorDistanceTuple<T>> rangeQuery(double[] lower, double[] upper);
  
}