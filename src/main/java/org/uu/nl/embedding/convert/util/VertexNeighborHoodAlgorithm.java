package org.uu.nl.embedding.convert.util;

import grph.Grph;
import grph.algo.VertexAdjacencyAlgorithm;
import toools.collections.primitive.IntCursor;
import toools.collections.primitive.LucIntSet;

import java.util.Iterator;

public abstract class VertexNeighborHoodAlgorithm extends VertexAdjacencyAlgorithm {

    @Override
    public int[][] compute(Grph g) {

        LucIntSet vertices = g.getVertices();

        if (vertices.isEmpty()) return new int[0][];

        int n = vertices.getGreatest() + 1;
        int[][] v = new int[n][];

        Iterator iterator = IntCursor.fromFastUtil(vertices).iterator();
        IntCursor c;
        while(iterator.hasNext()) {
            c = (IntCursor) iterator.next();
            v[c.value] = g.getNeighbours(c.value, this.getDirection()).toIntArray();
        }

        return v;
    }

    public abstract Grph.DIRECTION getDirection();

    public static class In extends VertexNeighborHoodAlgorithm {
        public Grph.DIRECTION getDirection() {
            return Grph.DIRECTION.in;
        }
    }

    public static class InOut extends VertexNeighborHoodAlgorithm {
        public Grph.DIRECTION getDirection() {
            return Grph.DIRECTION.in_out;
        }
    }

    public static class Out extends VertexNeighborHoodAlgorithm {
        public Grph.DIRECTION getDirection() {
            return Grph.DIRECTION.out;
        }
    }
}
