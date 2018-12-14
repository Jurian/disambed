package org.uu.nl.analyze.bca.util;

public class Options {
	protected final boolean reverse, normalize, literals;
	protected final double alpha, epsilon;
	protected final int nThreads;
	
	public Options(boolean reverse, boolean normalize, boolean literals, double alpha, double epsilon, int nThreads) {
		this.reverse = reverse;
		this.normalize = normalize;
		this.literals = literals;
		this.alpha = alpha;
		this.epsilon = epsilon;
		this.nThreads = nThreads;
	}

	public boolean isReverse() {
		return reverse;
	}

	public boolean isNormalize() {
		return normalize;
	}

	public boolean isLiterals() {
		return literals;
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
