package org.uu.nl.disembed.clustering;

import com.carrotsearch.hppc.IntArrayList;
import org.uu.nl.disembed.clustering.rules.RuleChecker;

import java.util.HashMap;
import java.util.Map;
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

        if(n == 1) return new ClusterResult(index, new int[][] {component});
        if(n == 2) {
            float penalty = penalties == null ? 0f : penalties[0];
            if(Util.weight(component[0], component[1], vectors, theta, epsilon, penalty) > 0)
                return new ClusterResult(index, new int[][] {{component[0],component[0]}});
            else return new ClusterResult(index, new int[][] {component});
        }
        throw new IllegalArgumentException("n >= 3");
    }

    protected int[][] toClusterArrays(final int[] clustering) {
        final int n = component.length;

        final Map<Integer, IntArrayList> clusterMap = new HashMap<>();
        boolean[] clustered = new boolean[n];

        for (int j = 0; j < n; j++) {

            if (clustered[j]) continue;

            IntArrayList cluster = new IntArrayList();
            cluster.add(component[j]);
            clusterMap.put(component[j], cluster);

            clustered[j] = true;

            for (int k = j + 1; k < n; k++) {

                if (clustered[k]) continue;

                if (clustering[j] == clustering[k]) {

                    cluster = clusterMap.get(component[j]);
                    cluster.add(component[k]);
                    clusterMap.put(component[j], cluster);
                    clustered[k] = true;
                }
            }
        }

        return clusterMap.values().stream().map(IntArrayList::toArray).toArray(int[][]::new);
    }

    public abstract ClusterResult cluster();

    public record ClusterResult(int index, int[][] cluster) { }
}
