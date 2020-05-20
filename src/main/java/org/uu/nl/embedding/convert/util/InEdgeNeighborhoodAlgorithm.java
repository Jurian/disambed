package org.uu.nl.embedding.convert.util;

import grph.Grph;
import me.tongfei.progressbar.ProgressBar;
import org.uu.nl.embedding.util.config.Configuration;
import toools.collections.primitive.LucIntSet;

/**
 * When asking for the edges of a vertex in the graph we don't get them in the same order as the neighbors. Most of the
 * time spent in the Bookmark Coloring Algorithm (BCA) is on finding the correct edge between two nodes. That's why we
 * pre-compute the edges in the same order as the neighbors. This means we only have to find an edge between nodes once
 * and then we can re-use it multiple times in the BCA process. This greatly increases the efficiency of BCA.
 *
 * @author Jurian Baas
 */
public class InEdgeNeighborhoodAlgorithm extends EdgeNeighborhoodAlgorithm {

    public InEdgeNeighborhoodAlgorithm(Configuration config) { super(config); }

    @Override
    FindEdges getEdgesAlgorithm(ProgressBar pb, Grph g, int threadId, int numThreads, int[] vertices, int[] verticesPerThread, int[][] v) {
        return new FindInEdges(pb, g, threadId, numThreads, vertices, verticesPerThread, v);
    }

    @Override
    public Grph.DIRECTION getDirection() {
        return Grph.DIRECTION.in;
    }

    class FindInEdges extends FindEdges {

        FindInEdges(ProgressBar pb, Grph g, int threadId, int numThreads, int[] vertices, int[] verticesPerThread, int[][] v) {
            super(pb, g, threadId, numThreads, vertices, verticesPerThread, v);
        }

        @Override
        public void run() {
            int c;
            int[] neighbors;
            LucIntSet edgesUnordered;

            for(int i = 0; i < verticesPerThread[threadId]; i++) {
                c = vertices[offset + i];
                neighbors = g.getNeighbours(c, getDirection()).toIntArray();
                edgesUnordered = g.getInEdges(c);
                v[c] = new int[neighbors.length];
                for(int n = 0; n < neighbors.length; n++)
                    v[c][n] = getEdge(g, neighbors[n], c, g.getOutEdges(neighbors[n]), edgesUnordered);
                pb.step();
            }
        }
    }
}
