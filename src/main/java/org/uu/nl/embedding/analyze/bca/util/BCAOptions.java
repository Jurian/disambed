package org.uu.nl.embedding.analyze.bca.util;

public class BCAOptions {
	
	public enum BCAType {
		VANILLA, SEMANTIC
	}
	
	private final BCAType type;
	private final boolean reverse;
	private final double alpha;
	private final double epsilon;
	private final int nThreads;
	
	public BCAOptions(BCAType type, boolean reverse, double alpha, double epsilon, int nThreads) {
		this.reverse = reverse;
		this.alpha = alpha;
		this.epsilon = epsilon;
		this.nThreads = nThreads;
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

	public int getnThreads() {
		return nThreads;
	}
	
	
}
