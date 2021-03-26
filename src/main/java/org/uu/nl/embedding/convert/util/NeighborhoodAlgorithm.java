package org.uu.nl.embedding.convert.util;


import grph.Grph;
import grph.GrphAlgorithm;
import me.tongfei.progressbar.ProgressBar;
import org.uu.nl.embedding.util.InMemoryRdfGraph;
import org.uu.nl.embedding.util.config.Configuration;

import java.util.concurrent.*;

/**
 * When asking for the edges of a vertex in the graph we don't get them in the same order as the neighbors. Most of the
 * time spent in the Bookmark Coloring Algorithm (BCA) is on finding the correct edge between two nodes. That's why we
 * pre-compute the edges in the same order as the neighbors. This means we only have to find an edge between nodes once
 * and then we can re-use it multiple times in the BCA process. This greatly increases the efficiency of BCA.
 *
 * @author Jurian Baas
 */
public abstract class NeighborhoodAlgorithm extends GrphAlgorithm<int[][]> {

    protected final Configuration config;
    protected final String label;
    public NeighborhoodAlgorithm(Configuration config, String label) {
        this.config = config;
        this.label = label;
    }

    @Override
    public int[][] compute(Grph g) {

        final int[] vertices = g.getVertices().toIntArray();

        if (vertices.length == 0) return new int[0][];

        final int numThreads = config.getThreads();
        final int numVertices = vertices.length;
        final int[][] v = new int[numVertices][];
        final int[] verticesPerThread = new int[numThreads];
        for (int i = 0; i < numThreads; i++) verticesPerThread[i] = numVertices / numThreads;
        verticesPerThread[numThreads - 1] += numVertices % numThreads;
        final ExecutorService es = Executors.newWorkStealingPool(numThreads);
        final CompletionService<Void> cs = new ExecutorCompletionService<>(es);

        try(ProgressBar pb = Configuration.progressBar(label, numVertices, "nodes")) {

            for(int t = 0; t < numThreads; t++) {
                cs.submit(findNeighborHood(pb, (InMemoryRdfGraph) g, t, numThreads, vertices, verticesPerThread, v), null);
            }

            int handled = 0;
            while(handled < numThreads) {
                try {
                    cs.take().get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                } finally {
                    handled++;
                }
            }

        }

        return v;
    }

    abstract FindNeighborHood findNeighborHood(ProgressBar pb, InMemoryRdfGraph g, int threadId, int numThreads, int[] vertices, int[] verticesPerThread, int[][] v);

    abstract static class FindNeighborHood implements Runnable {

        final ProgressBar pb;
        final InMemoryRdfGraph g;
        final int threadId;
        final int numThreads;
        final int numVertices;
        final int offset;
        final int[] vertices;
        final int[] verticesPerThread;
        final int[][] v;

        protected boolean isLiteral(int vertex) {
            return g.getVertexTypeProperty().getValueAsInt(vertex) == 2;
        }

        public FindNeighborHood(ProgressBar pb, InMemoryRdfGraph g, int threadId, int numThreads, int[] vertices, int[] verticesPerThread, int[][] v) {
            this.pb = pb;
            this.g = g;
            this.threadId = threadId;
            this.numThreads = numThreads;
            this.vertices = vertices;
            this.verticesPerThread = verticesPerThread;
            this.v = v;
            this.numVertices = vertices.length;
            this.offset = (numVertices / numThreads) * threadId;
        }
    }


}
