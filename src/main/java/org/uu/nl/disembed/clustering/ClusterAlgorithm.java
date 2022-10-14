package org.uu.nl.disembed.clustering;

import org.uu.nl.disembed.clustering.rules.RuleChecker;

import java.util.concurrent.Callable;

public abstract  class ClusterAlgorithm implements Callable<ClusterAlgorithm.ClusterResult> {

    protected final int threads;
    protected final int index;
    protected final int[] component;
    protected final RuleChecker ruleChecker;
    protected final float[][] vectors;
    protected final float theta, epsilon;

    public ClusterAlgorithm(int index, int[] component, RuleChecker ruleChecker, float[][] vectors, float theta, float epsilon, int threads) {
        int n = component.length;
        this.threads = threads;
        this.index = index;
        this.component = component;
        this.ruleChecker = ruleChecker;
        this.vectors = vectors;
        this.theta = theta;
        this.epsilon = epsilon;
    }

    protected boolean usingRules() {
        return ruleChecker != null;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public ClusterResult call() {
        return cluster();
    }

    protected ClusterResult skip(int n, float[] penalties) {

        if(n == 1) return new ClusterResult(index, component);
        if(n == 2) {
            float penalty = penalties == null ? 0f : penalties[0];
            if(Util.weight(component[0], component[1], vectors, theta, epsilon, penalty) > 0)
                return new ClusterResult(index, new int[] {component[0],component[0]});
            else return new ClusterResult(index, component);
        }
        throw new IllegalArgumentException("n >= 3");
    }

    public abstract ClusterResult cluster();

    public record ClusterResult(int index, int[] cluster) { }
}
