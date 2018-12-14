package org.uu.nl.ann.kdtree.util;

public class ValueDistanceTuple<T> implements Comparable<ValueDistanceTuple<T>> {

	  private final T value;
	  private final double dist;

	  public ValueDistanceTuple( T value, double dist) {
	    this.value = value;
	    this.dist = dist;
	  }

	  public double getDistance() {
	    return dist;
	  }

	  public T getValue() {
	    return value;
	  }

	  @Override
	  public int compareTo(ValueDistanceTuple<T> o) {
	    return Double.compare(o.dist, dist);
	  }

	  @Override
	  public String toString() {
	    return value + " -> " + dist;
	  }

}
