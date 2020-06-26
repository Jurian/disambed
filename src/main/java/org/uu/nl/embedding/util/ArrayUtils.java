	package org.uu.nl.embedding.util;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class ArrayUtils {
	
	/**
	 * Code copied from:
	 * https://stackoverflow.com/questions/80476/how-can-i-concatenate-two-arrays-in-java
	 * @param <T>
	 * @param a
	 * @param b
	 * @return
	 */
	static public <T> T[] concatenate(final T[] array1, final T[] array2) {
	    int aLen = array1.length;
	    int bLen = array2.length;

	    @SuppressWarnings("unchecked")
	    T[] resArray = (T[]) Array.newInstance(array1.getClass().getComponentType(), aLen + bLen);
	    System.arraycopy(array1, 0, resArray, 0, aLen);
	    System.arraycopy(array2, 0, resArray, aLen, bLen);

	    return resArray;
	}
	
	static public boolean[] concatenate(final boolean[] array1, final boolean[] array2) {
	    int aLen = array1.length;
	    int bLen = array2.length;

	    @SuppressWarnings("unchecked")
	    boolean[] resArray = (boolean[]) Array.newInstance(array1.getClass().getComponentType(), aLen + bLen);
	    System.arraycopy(array1, 0, resArray, 0, aLen);
	    System.arraycopy(array2, 0, resArray, aLen, bLen);

	    return resArray;
	}
	

	
	static public boolean[] toArray(final ArrayList<Boolean> array) {
	    int len = array.size();

	    @SuppressWarnings("unchecked")
	    boolean[] resArray = (boolean[]) Array.newInstance(array.getClass().getComponentType(), len);
	    System.arraycopy(array, 0, resArray, 0, len);

	    return resArray;
	}
	

}
