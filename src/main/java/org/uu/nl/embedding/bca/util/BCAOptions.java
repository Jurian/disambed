package org.uu.nl.embedding.bca.util;

import java.util.Map;

/**
 * @author Jurian Baas
 */
public class BCAOptions {

	public Map<String, Double> getWeights() {
		return this.weights;
	}

	public enum BCAType {
		DIRECTED_UNWEIGHTED,
		DIRECTED_WEIGHTED,
		DIRECTED_WEIGHTED_LITERAL,
		UNDIRECTED_WEIGHTED
	}
	
	private final BCAType type;
	private final int negativeSamples;
	private final boolean reverse, predicates;
	private final double alpha;
	private final double epsilon;
	private final Map<String, Double> weights;
	
	public BCAOptions(Map<String, Double> weights, BCAType type, boolean reverse, boolean predicates, double alpha, double epsilon, int negativeSamples) {
		this.weights = weights;
		this.predicates = predicates;
		this.reverse = reverse;
		this.alpha = alpha;
		this.epsilon = epsilon;
		this.type = type;
		this.negativeSamples = negativeSamples;
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

	public boolean includePredicates() { return predicates; }

	public int getNegativeSamples() { return negativeSamples; }
}
