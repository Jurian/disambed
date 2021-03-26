package org.uu.nl.embedding.convert.util;

import me.tongfei.progressbar.ProgressBar;
import org.uu.nl.embedding.util.InMemoryRdfGraph;
import org.uu.nl.embedding.util.config.Configuration;

public class VertexNeighborhoodAlgorithm extends NeighborhoodAlgorithm{
    public VertexNeighborhoodAlgorithm(Configuration config) {
        super(config, "Vertex");
    }


    @Override
    FindNeighborHood findNeighborHood(ProgressBar pb, InMemoryRdfGraph g, int threadId, int numThreads, int[] vertices, int[] verticesPerThread, int[][] v) {
        return new FindInVertexes(pb, g, threadId, numThreads, vertices, verticesPerThread, v);
    }

    static class FindInVertexes extends FindNeighborHood {

        FindInVertexes(ProgressBar pb, InMemoryRdfGraph g, int threadId, int numThreads, int[] vertices, int[] verticesPerThread, int[][] v) {
            super(pb, g, threadId, numThreads, vertices, verticesPerThread, v);
        }

        @Override
        public void run() {
            int c;
            int[] edges;
            for(int i = 0; i < verticesPerThread[threadId]; i++) {
                c = vertices[offset + i];

                edges = g.getInOutOnlyEdges(c).toIntArray();
                v[c] = new int[edges.length];

                for(int n = 0; n < edges.length; n++) {
                    v[c][n] = g.getTheOtherVertex(edges[n], c);
                }

                pb.step();
            }
        }
    }
}
