package org.uu.nl.embedding.ann.kdtree;

/**
 * Construction interface for ANN (approximate nearest neighbours).
 * 
 * This version has been modified to make use of primitive arrays
 * 
 * @author Jurian Baas & Thomas Jungblut (original)
 * @see <a href="https://github.com/thomasjungblut/JRPT/blob/master/src/de/jungblut/jrpt/ANNConstruction.java">Original</a>
 * @param <T> the value type of the payload
 */
public interface ANNConstruction<T> {

  /**
   * Adds a new vector to the tree with the given payload. The payload may be
   * null, the vector is not allowed to be non-null.
   * 
   * @param v the non-null vector.
   * @param payload the maybe null payload for this vector.
   */
  public void add(double[] v, T payload);

}