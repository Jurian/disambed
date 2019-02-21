package org.uu.nl.embedding.ann.kdtree.util;

/**
 * Tuple for holding element information from an ANN. Comparable is implemented
 * on the distance, enabling a descending sort mainly designed for use in a
 * {@link LimitedPriorityQueue}.
 * 
 * Modified to use primitive arrays.
 * 
 * @author Jurian Baas & Thomas Jungblut (original)
 * @see <a href="https://github.com/thomasjungblut/JRPT/blob/master/src/de/jungblut/jrpt/VectorDistanceTuple.java">Original</a>
 * @param <T> the payload value type
 */
public final class VectorDistanceTuple<T> implements
    Comparable<VectorDistanceTuple<T>> {

  private final double[] keyVector;
  private final T value;
  private final double dist;

  public VectorDistanceTuple(double[] keyVector, T value, double dist) {
    this.keyVector = keyVector;
    this.value = value;
    this.dist = dist;
  }

  public double getDistance() {
    return dist;
  }

  public double[] getVector() {
    return keyVector;
  }

  public T getValue() {
    return value;
  }

  @Override
  public int compareTo(VectorDistanceTuple<T> o) {
    return Double.compare(o.dist, dist);
  }

  @Override
  public String toString() {
    return keyVector + " - " + value + " -> " + dist;
  }
}