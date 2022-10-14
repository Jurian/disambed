package org.uu.nl.embedding.cluster;

import com.github.jelmerk.knn.DistanceFunctions;
import com.github.jelmerk.knn.SearchResult;
import com.github.jelmerk.knn.hnsw.HnswIndex;
import me.tongfei.progressbar.ProgressBar;
import org.uu.nl.embedding.opt.Embedding;
import org.uu.nl.embedding.opt.Optimizer;
import org.uu.nl.embedding.util.Progress;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class CandidatePairs {

    public static final int M = 16;
    public static final int EF = 200;
    public static final int EF_CONSTRUCTION = 200;

    static class Pair {

        final int[] data;

        public Pair(int a, int b) {
            this.data = new int[]{a,b};
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Pair pair = (Pair) o;
            return data[0] == pair.data[0] && data[1] == pair.data[1];
        }

        @Override
        public int hashCode() {
            return Objects.hash(data[0], data[1]);
        }
    }

    private final HnswIndex<String, float[], Optimizer.EmbeddedEntity, Float> index;

    public CandidatePairs(Embedding embedding) {

        int dim = embedding.getDimension();
        int n = embedding.getSize();
        this.index = HnswIndex
                .newBuilder(dim, DistanceFunctions.FLOAT_INNER_PRODUCT, n)
                .withM(M)
                .withEf(EF)
                .withEfConstruction(EF_CONSTRUCTION)
                .build();
    }

    public void add(Optimizer.EmbeddedEntity entity) {
        index.add(entity);
    }

    public int[][] getNearestNeighborPairs(Embedding embedding, int k, float theta) {

        // Use a set to remove duplicate pairs
        Set<Pair> tempIndex = new HashSet<>();

        try(ProgressBar pb = Progress.progressBar("Approximate Nearest Neighbors", embedding.getSize(), "entities")) {
            for(Optimizer.EmbeddedEntity entity : index.items()) {
                List<SearchResult<Optimizer.EmbeddedEntity, Float>> result = index.findNeighbors(entity.id(), k);

                for (SearchResult<Optimizer.EmbeddedEntity, Float> embeddedEntityFloatSearchResult : result) {
                    int a = entity.getIndex();
                    int b = embeddedEntityFloatSearchResult.item().getIndex();
                    if(a == b) continue;
                    if(Util.cosineSimilarity(embedding.getVectors()[a], embedding.getVectors()[b]) < theta) continue;
                    tempIndex.add(a < b ? new Pair(a, b) : new Pair(b, a));
                }
                pb.step();
            }
        }


        int nPairs = tempIndex.size();
        int[][] candidatePairs = new int[nPairs][2];
        int i = 0;
        for(Pair p : tempIndex) {
            candidatePairs[i++] = p.data;
        }
        return candidatePairs;
    }


}
