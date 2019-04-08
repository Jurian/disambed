package org.uu.nl.embedding.convert.util;


import grph.Grph;
import grph.algo.VertexAdjacencyAlgorithm;
import me.tongfei.progressbar.ProgressBar;
import org.uu.nl.embedding.Settings;
import toools.collections.primitive.IntCursor;
import toools.collections.primitive.LucIntSet;

import java.util.Iterator;
import java.util.concurrent.*;

@SuppressWarnings("Duplicates")
public abstract class EdgeNeighborhoodAlgorithm extends VertexAdjacencyAlgorithm {

    private static final Settings settings = Settings.getInstance();

    private static int getEdge(Grph graph, int source, int destination, LucIntSet out, LucIntSet in) {

        if (out.size() == 0 || in.size() == 0) return -1;

        if (out.size() < in.size()) {
            for(int e : out) if (graph.getDirectedSimpleEdgeHead(e) == destination) return e;
        } else {
            for(int e : in) if (graph.getDirectedSimpleEdgeTail(e) == source) return e;
        }
        assert true: "Could not find an edge";
        return -1;
    }

    public abstract Grph.DIRECTION getDirection();

    public static class In extends EdgeNeighborhoodAlgorithm {

        public Grph.DIRECTION getDirection() {
            return Grph.DIRECTION.in;
        }

        @Override
        public int[][] compute(Grph g) {

            final int[] vertices = g.getVertices().toIntArray();

            if (vertices.length == 0) return new int[0][];

            final int numThreads = settings.threads();
            final int numVertices = vertices.length;
            final int[][] v = new int[numVertices][];
            final int[] verticesPerThread = new int[numThreads];
            for(int i = 0; i < numThreads; i++) verticesPerThread[i] = numVertices / numThreads;
            verticesPerThread[numThreads - 1] += numVertices % numThreads;
            final ExecutorService es = Executors.newWorkStealingPool(numThreads);
            final CompletionService<Void> cs = new ExecutorCompletionService<>(es);

            try(ProgressBar bp = settings.progressBar("Edge in", numVertices, "nodes")) {

                for(int t = 0; t < numThreads; t++) {
                    final int threadId = t;
                    cs.submit(() -> {

                        int c;
                        final int offset = (numVertices / numThreads) * threadId;
                        int[] neighbors;
                        LucIntSet edgesUnordered;

                        for(int i = 0; i < verticesPerThread[threadId]; i++) {
                            c = vertices[offset + i];
                            neighbors = g.getNeighbours(c, Grph.DIRECTION.in).toIntArray();
                            edgesUnordered = g.getInEdges(c);
                            v[c] = new int[neighbors.length];
                            for(int n = 0; n < neighbors.length; n++)
                                v[c][n] = getEdge(g, neighbors[n], c, g.getOutEdges(neighbors[n]), edgesUnordered);
                            bp.step();

                        }
                        return null;
                    });
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


    }

    public static class Out extends EdgeNeighborhoodAlgorithm {

        public Grph.DIRECTION getDirection() {
            return Grph.DIRECTION.out;
        }

        @Override
        public int[][] compute(Grph g) {

            final int[] vertices = g.getVertices().toIntArray();

            if (vertices.length == 0) return new int[0][];

            final int numThreads = settings.threads();
            final int numVertices = vertices.length;
            final int[][] v = new int[numVertices][];
            final int[] verticesPerThread = new int[numThreads];
            for(int i = 0; i < numThreads; i++) verticesPerThread[i] = numVertices / numThreads;
            verticesPerThread[numThreads - 1] += numVertices % numThreads;
            final ExecutorService es = Executors.newWorkStealingPool(numThreads);
            final CompletionService<Void> cs = new ExecutorCompletionService<>(es);

            try(ProgressBar bp = settings.progressBar("Edge out", numVertices, "nodes")) {

                for(int t = 0; t < numThreads; t++) {
                    final int threadId = t;
                    cs.submit(() -> {

                        int c;
                        final int offset = (numVertices / numThreads) * threadId;
                        int[] neighbors;
                        LucIntSet edgesUnordered;

                        for(int i = 0; i < verticesPerThread[threadId]; i++) {
                            c = vertices[offset + i];
                            neighbors = g.getNeighbours(c, Grph.DIRECTION.out).toIntArray();
                            edgesUnordered = g.getOutEdges(c);
                            v[c] = new int[neighbors.length];
                            for(int n = 0; n < neighbors.length; n++)
                                v[c][n] = getEdge(g, c, neighbors[n], edgesUnordered, g.getInEdges(neighbors[n]));
                            bp.step();

                        }
                        return null;
                    });
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
    }
}
