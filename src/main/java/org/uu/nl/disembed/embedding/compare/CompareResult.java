package org.uu.nl.embedding.compare;

import java.util.ArrayList;
import java.util.List;

/**
 * The result of comparing a node with other nodes
 */
public class CompareResult {

    public final int vert;
    public final List<Integer> otherVerts;
    public final List<Float> similarities;

    public CompareResult(int vert) {
        this.vert = vert;
        this.otherVerts = new ArrayList<>();
        this.similarities = new ArrayList<>();
    }
}
