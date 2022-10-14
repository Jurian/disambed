package org.uu.nl.embedding.cluster;

import java.util.concurrent.Callable;

public abstract  class ClusterAlgorithm implements Callable<ClusterAlgorithm.ClusterResult> {

    protected final int index;
    protected final int[] component;
    protected final float[] penalties;
    protected final float[][] vectors;
    protected final float theta, epsilon;

    public ClusterAlgorithm(int index, int[] component, float[] penalties, float[][] vectors, float theta, float epsilon) {
        this.index = index;
        this.component = component;
        this.penalties = penalties;
        this.vectors = vectors;
        this.theta = theta;
        this.epsilon = epsilon;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public ClusterResult call() {
        return cluster();
    }

    protected ClusterResult skip(int n) {

        if(n == 1) return new ClusterResult(index, component);
        if(n == 2) {
            if(Util.weight(component[0], component[1], vectors, theta, epsilon, penalties[0]) > 0)
                return new ClusterResult(index, new int[] {component[0],component[0]});
            else return new ClusterResult(index, component);
        }
        throw new IllegalArgumentException("n >= 3");
    }

    public abstract ClusterResult cluster();

    public record ClusterResult(int index, int[] cluster) { }
}
