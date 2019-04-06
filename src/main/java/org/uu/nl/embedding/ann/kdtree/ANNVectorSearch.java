package org.uu.nl.embedding.ann.kdtree;

import org.uu.nl.embedding.ann.kdtree.util.VectorDistanceTuple;

import java.util.List;

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
interface ANNVectorSearch<T> {

  /**
   * @return the k nearest neighbors to the given vector.
   */
  List<VectorDistanceTuple<T>> getNearestNeighbours(double[] vec, int k);

  /**
   * @return nearest neighbors to the given vector within the given radius.
   */
  List<VectorDistanceTuple<T>> getNearestNeighbours(double[] vec, double radius);

  /**
   * @return the k nearest neighbors to the given vector within the given
   *         radius.
   */
  List<VectorDistanceTuple<T>> getNearestNeighbours(double[] vec, int k, double radius);

  /**
   * @return the vectors and payload within the range of the lower and upper
   *         bounded vectors.
   */
  List<VectorDistanceTuple<T>> rangeQuery(double[] lower, double[] upper);
  
}