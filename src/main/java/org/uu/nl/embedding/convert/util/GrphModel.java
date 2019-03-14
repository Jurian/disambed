package org.uu.nl.embedding.convert.util;

import grph.Grph;

public class GrphModel {

    private final Grph g;
    private final int[][] inEdgeNeighborhood;
    private final int[][] outEdgeNeighborhood;

    public GrphModel(Grph g, int[][] inEdgeNeighborhood, int[][] outEdgeNeighborhood) {
        this.g = g;
        this.inEdgeNeighborhood = inEdgeNeighborhood;
        this.outEdgeNeighborhood = outEdgeNeighborhood;
    }

    public Grph getG() {
        return g;
    }

    public int[][] getInEdgeNeighborhood() {
        return inEdgeNeighborhood;
    }

    public int[][] getOutEdgeNeighborhood() {
        return outEdgeNeighborhood;
    }
}
