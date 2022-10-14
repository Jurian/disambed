package org.uu.nl.disembed.clustering;

import com.github.jelmerk.knn.DistanceFunctions;
import com.github.jelmerk.knn.hnsw.HnswIndex;
import me.tongfei.progressbar.ProgressBar;
import org.apache.log4j.Logger;
import org.uu.nl.disembed.embedding.opt.Embedding;
import org.uu.nl.disembed.embedding.opt.Optimizer;
import org.uu.nl.disembed.util.config.Configuration;
import org.uu.nl.disembed.util.progress.Progress;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class CandidatePairs {

    private final static Logger logger = Logger.getLogger(CandidatePairs.class);

    public static final int M = 16;
    public static final int EF = 200;
    public static final int EF_CONSTRUCTION = 200;

    record Pair(int a, int b) {

        Pair(int a, int b) {
            if (a > b) {
                this.a = b;
                this.b = a;
            } else {
                this.a = a;
                this.b = b;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Pair pair = (Pair) o;
            return a == pair.a && b == pair.b;
        }

        @Override
        public int hashCode() {
            return Objects.hash(a, b);
        }
    }

    private final HnswIndex<String, float[], Optimizer.EmbeddedEntity, Float> index;
    private final Configuration config;

    public CandidatePairs(Embedding embedding, Configuration config) {
        int dim = embedding.getDimension();
        int n = embedding.getSize();
        this.index = HnswIndex
                .newBuilder(dim, DistanceFunctions.FLOAT_INNER_PRODUCT, n)
                .withM(M)
                .withEf(EF)
                .withEfConstruction(EF_CONSTRUCTION)
                .build();
        this.config = config;

        logger.info("Adding embedded entities to approximate nearest neighbor index");
        try (ProgressBar pb = Progress.progressBar("Added", embedding.getSize(), "entities")) {
            for (Optimizer.EmbeddedEntity embeddedEntity : embedding) {
                add(embeddedEntity);
                pb.step();
            }
        }

    }

    public CandidatePairs(Embedding embedding, HnswIndex<String, float[], Optimizer.EmbeddedEntity, Float> index, Configuration config) {
        this.index = index;
        this.config = config;
    }

    public HnswIndex<String, float[], Optimizer.EmbeddedEntity, Float> getIndex() {
        return index;
    }

    public void add(Optimizer.EmbeddedEntity entity) {
        index.add(entity);
    }

    public int[][] getNearestNeighborPairs(Embedding embedding, int k, float theta) {

        // Use a set to remove duplicate pairs
        final Set<Pair> tempIndex = new HashSet<>();

        final ExecutorService es = Executors.newWorkStealingPool(config.getThreads());
        CompletionService<Set<Pair>> completionService = new ExecutorCompletionService<>(es);

        // The index is thread safe, so find nearest neighbors in parallel
        for(Optimizer.EmbeddedEntity entity : index.items()) {
            final int a = entity.index();
            completionService.submit(() -> index
                    .findNeighbors(entity.id(), k) // The entity itself is not included
                    .stream()
                    .filter(result -> { // Remove pairs with too low similarity
                        final int b = result.item().index();
                        return Util.cosineSimilarity(embedding.getVectors()[a], embedding.getVectors()[b]) >= theta;
                    }).map(result -> new Pair(a, result.item().index())) // Map to pairs for removal of duplicates later
                    .collect(Collectors.toSet())
            );
        }

        int received = 0;
        try(ProgressBar pb = Progress.progressBar("Approximate Nearest Neighbors", embedding.getSize(), "entities")) {

            while(received < index.size()) {
                try {
                    tempIndex.addAll(completionService.take().get());  // Removes duplicate pairs
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                } finally {
                    pb.step();
                    pb.setExtraMessage(Integer.toString(tempIndex.size()));
                    received++;
                }
            }
        } finally {
            es.shutdown();
        }

        int nPairs = tempIndex.size();
        int[][] candidatePairs = new int[nPairs][2];
        int i = 0;
        for(Pair p : tempIndex) {
            candidatePairs[i][0] = p.a;
            candidatePairs[i][1] = p.b;
            i++;
        }
        return candidatePairs;
    }
}
