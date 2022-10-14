package org.uu.nl.embedding.cluster;

import com.carrotsearch.hppc.IntArrayList;

public class VoteClustering extends ClusterAlgorithm {

    public VoteClustering(int index, int[] component, float[] penalties, float[][] vectors, float theta, float epsilon) {
        super(index, component, penalties, vectors, theta, epsilon);
    }

    @Override
    public ClusterResult cluster() {

        final int n = component.length;

        if(n < 3) return skip(n);

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

                int k = Util.combinationToIndex(i+1, j+1, n) - 1;
                sums[clusterIndex.get(j)] += Util.weight(component[i], component[j], vectors, theta, epsilon, penalties[k]);

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
