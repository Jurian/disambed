package org.uu.nl.embedding.bca.util;

public class BCAOptions {
	
	public enum BCAType {
		VANILLA, SEMANTIC
	}
	
	private final BCAType type;
	private final boolean reverse;
	private final double alpha;
	private final double epsilon;
	
	public BCAOptions(BCAType type, boolean reverse, double alpha, double epsilon) {
		this.reverse = reverse;
		this.alpha = alpha;
		this.epsilon = epsilon;
		this.type = type;
	}

	public BCAType getType() {
		return type;
	}

	public boolean isReverse() {
		return reverse;
	}

	public double getAlpha() {
		return alpha;
	}

	public double getEpsilon() {
		return epsilon;
	}

}
