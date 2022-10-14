package org.uu.nl.embedding.convert.util;

import me.tongfei.progressbar.ProgressBar;
import org.uu.nl.embedding.util.InMemoryRdfGraph;

public class EdgeNeighborhoodAlgorithm extends NeighborhoodAlgorithm {
    public EdgeNeighborhoodAlgorithm(int numThreads) {
        super(numThreads, "Edge");
    }

    @Override
    FindNeighborHood findNeighborHood(ProgressBar pb, InMemoryRdfGraph g, int threadId, int numThreads, int[] vertices, int[] verticesPerThread, int[][] v) {
        return new FindInEdges(pb, g, threadId, numThreads, vertices, verticesPerThread, v);
    }

    static class FindInEdges extends FindNeighborHood {

        FindInEdges(ProgressBar pb, InMemoryRdfGraph g, int threadId, int numThreads, int[] vertices, int[] verticesPerThread, int[][] v) {
            super(pb, g, threadId, numThreads, vertices, verticesPerThread, v);
        }

        @Override
        public void run() {
            int c;
            int[] edges;
            for(int i = 0; i < verticesPerThread[threadId]; i++) {
                c = vertices[offset + i];
                v[c] = g.getInOutOnlyEdges(c).toIntArray();
                pb.step();
            }
        }
    }
}
