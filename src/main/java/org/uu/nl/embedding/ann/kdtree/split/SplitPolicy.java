package org.uu.nl.embedding.ann.kdtree.split;


/**
 * This version has been modified to make use of primitive arrays
 * 
 * @author Jurian Baas & Thomas Jungblut (original)
 * @see <a href="https://github.com/thomasjungblut/JRPT/blob/master/src/de/jungblut/jrpt/rules/SplitPolicy.java">Original</a>
 * @param <T> the payload type
 */
public interface SplitPolicy<T> {

  /**
   * Choose a split dimension for the given vector and tree level.
   * 
   * @param v the vector.
   * @param level the tree level.
   * @return an index of the dimension between 0 and v.getDimension()
   *         (exclusive).
   */
  int splitDimension(double[] v, int level);

  int splitDimension(int dimension, int level);
  
}