package org.uu.nl.embedding.convert.util;


import grph.Grph;
import grph.algo.VertexAdjacencyAlgorithm;
import me.tongfei.progressbar.ProgressBar;
import org.uu.nl.embedding.Settings;
import toools.collections.primitive.LucIntSet;

import java.util.concurrent.*;

/**
 * When asking for the edges of a vertex in the graph we don't get them in the same order as the neighbors. Most of the
 * time spent in the Bookmark Coloring Algorithm (BCA) is on finding the correct edge between two nodes. That's why we
 * pre-compute the edges in the same order as the neighbors. This means we only have to find an edge between nodes once
 * and then we can re-use it multiple times in the BCA process. This greatly increases the efficiency of BCA.
 *
 * @author Jurian Baas
 */
public abstract class EdgeNeighborhoodAlgorithm extends VertexAdjacencyAlgorithm {

    private static final Settings settings = Settings.getInstance();

    static int getEdge(Grph graph, int source, int destination, LucIntSet out, LucIntSet in) {

        if (out.size() == 0 || in.size() == 0) return -1;

        if (out.size() < in.size()) {
            for(int e : out) if (graph.getDirectedSimpleEdgeHead(e) == destination) return e;
        } else {
            for(int e : in) if (graph.getDirectedSimpleEdgeTail(e) == source) return e;
        }
        assert true: "Could not find an edge";
        return -1;
    }

    @Override
    public int[][] compute(Grph g) {

        final int[] vertices = g.getVertices().toIntArray();

        if (vertices.length == 0) return new int[0][];

        final int numThreads = settings.threads();
        final int numVertices = vertices.length;
        final int[][] v = new int[numVertices][];
        final int[] verticesPerThread = new int[numThreads];
        for (int i = 0; i < numThreads; i++) verticesPerThread[i] = numVertices / numThreads;
        verticesPerThread[numThreads - 1] += numVertices % numThreads;
        final ExecutorService es = Executors.newWorkStealingPool(numThreads);
        final CompletionService<Void> cs = new ExecutorCompletionService<>(es);

        try(ProgressBar pb = settings.progressBar("Edge " + this.getDirection(), numVertices, "nodes")) {

            for(int t = 0; t < numThreads; t++) {
                cs.submit(getEdgesAlgorithm(pb, g, t, numThreads, vertices, verticesPerThread, v), null);
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

    abstract FindEdges getEdgesAlgorithm(ProgressBar pb, Grph g, int threadId, int numThreads, int[] vertices, int[] verticesPerThread, int[][] v);

    public abstract Grph.DIRECTION getDirection();

    abstract class FindEdges implements Runnable {

        final ProgressBar pb;
        final Grph g;
        final int threadId;
        final int numThreads;
        final int numVertices;
        final int offset;
        final int[] vertices;
        final int[] verticesPerThread;
        final int[][] v;

        public FindEdges(ProgressBar pb, Grph g, int threadId, int numThreads, int[] vertices, int[] verticesPerThread, int[][] v) {
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
