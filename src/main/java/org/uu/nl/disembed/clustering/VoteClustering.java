package org.uu.nl.disembed.clustering;

import com.carrotsearch.hppc.IntArrayList;
import org.uu.nl.disembed.clustering.rules.RuleChecker;

public class VoteClustering extends ClusterAlgorithm {

    public VoteClustering(int index, int[] component, RuleChecker ruleChecker, float[][] vectors, float theta, float epsilon, int threads) {
        super(index, component, ruleChecker, vectors, theta, epsilon, threads);
    }

    @Override
    public ClusterResult cluster() {

        final int n = component.length;
        final int edges = Util.nEdges(n); // number of edges

        final float[] penalties = usingRules() ? ruleChecker.checkComponent(component) : null;

        if(n < 3) return skip(n, penalties);

        int nClust = 0;
        IntArrayList clusterIndex = new IntArrayList();
        int[] clusters = new int[n];

        clusters[0] = component[0];
        clusterIndex.add(nClust++);

        for(int i = 1; i < n; i++) {

            float[] sums = new float[nClust];

            double bestSum = 0;
            int bestClst = 0;
            int bestIndex = 0;

            for(int j = 0; j < i; j++) {

                int k = Util.combinationToIndex(i, j, n);
                sums[clusterIndex.get(j)] += Util.weight(component[i], component[j], vectors, theta, epsilon, penalties != null ? penalties[k] : 0);

                if(sums[clusterIndex.get(j)] > bestSum) {
                    bestSum = sums[clusterIndex.get(j)];
                    bestClst = clusterIndex.get(j);
                    bestIndex = clusters[j];
                }
            }

            if(sums[bestClst] > 0) {
                clusters[i] = bestIndex;
                clusterIndex.add(bestClst);
            } else {
                clusters[i] = component[i];
                clusterIndex.add(nClust++);
            }
        }

        return new ClusterResult(index, clusters);
    }
}
