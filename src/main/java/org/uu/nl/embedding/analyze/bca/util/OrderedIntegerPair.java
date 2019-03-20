package org.uu.nl.embedding.analyze.bca.util;

/**
 *
* @author Thanos Papaoikonou
 */
public class OrderedIntegerPair {
    
    private final int index1;
    private final int index2;
    private boolean diagonal;

    public OrderedIntegerPair(int index1, int index2) {
        this.index1 = index1;
        this.index2 = index2;
        this.setDiagonal(index1 == index2);
    }
    
    public int getIndex1() {
        return index1;
    }


    public int getIndex2() {
        return index2;
    }


    @Override
    public int hashCode() {
    	return (index1 << 16) + index2;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false; 
        if (!(obj instanceof OrderedIntegerPair)) return false;
        
        final OrderedIntegerPair other = (OrderedIntegerPair) obj;
        return index1 == other.index1 && index2 == other.index2;
    }

    @Override
    public String toString() {
        return "<" + index1 + ", " + index2 + '>';
    }


	public boolean isDiagonal() {
		return diagonal;
	}


	private void setDiagonal(boolean diagonal) {
		this.diagonal = diagonal;
	}
 
}