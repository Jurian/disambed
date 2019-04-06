package org.uu.nl.embedding.convert.util;


import grph.Grph;
import grph.algo.VertexAdjacencyAlgorithm;
import me.tongfei.progressbar.ProgressBar;
import org.uu.nl.embedding.Settings;
import toools.collections.primitive.IntCursor;
import toools.collections.primitive.LucIntSet;

import java.util.Iterator;

public abstract class EdgeNeighborhoodAlgorithm extends VertexAdjacencyAlgorithm {

    private static final Settings settings = Settings.getInstance();

    @Override
    public int[][] compute(Grph g) {

        LucIntSet vertices = g.getVertices();

        if (vertices.isEmpty()) return new int[0][];

        final int n = vertices.getGreatest() + 1;
        final int[][] v = new int[n][];

        final Iterator iterator = IntCursor.fromFastUtil(vertices).iterator();

        IntCursor c;
        switch (getDirection()){
            case in:
                try(ProgressBar bp = settings.progressBar("Edge in", n, "nodes")) {
                    while(iterator.hasNext()) {
                        c = (IntCursor) iterator.next();
                        int[] neighbors = g.getNeighbours(c.value, Grph.DIRECTION.in).toIntArray();
                        LucIntSet edges = g.getInEdges(c.value);
                        v[c.value] = new int[neighbors.length];
                        for(int i = 0; i < neighbors.length; i++)
                            v[c.value][i] = getEdge(g, neighbors[i], c.value, g.getOutEdges(neighbors[i]), edges);
                        bp.step();
                    }
                }

                break;
            case out:
                try(ProgressBar bp = settings.progressBar("Edge out", n, "nodes")) {
                    while (iterator.hasNext()) {
                        c = (IntCursor) iterator.next();
                        int[] neighbors = g.getNeighbours(c.value, Grph.DIRECTION.out).toIntArray();
                        LucIntSet edges = g.getOutEdges(c.value);
                        v[c.value] = new int[neighbors.length];
                        for (int i = 0; i < neighbors.length; i++)
                            v[c.value][i] = getEdge(g, c.value, neighbors[i], edges, g.getInEdges(neighbors[i]));
                        bp.step();
                    }
                }
                break;
        }

        return v;
    }

    private int getEdge(Grph graph, int source, int destination, LucIntSet out, LucIntSet in) {

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
    }

    public static class Out extends EdgeNeighborhoodAlgorithm {

        public Grph.DIRECTION getDirection() {
            return Grph.DIRECTION.out;
        }
    }
}
