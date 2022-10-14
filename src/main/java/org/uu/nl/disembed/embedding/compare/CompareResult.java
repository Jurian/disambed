package org.uu.nl.disembed.embedding.compare;

import java.util.ArrayList;
import java.util.List;

/**
 * The result of comparing a node with other nodes
 */
public class CompareResult {

    public final int targetSize;
    public final int vert;
    public final List<Integer> otherVerts;
    public final List<Float> similarities;

    public CompareResult(int vert, int targetSize) {
        this.vert = vert;
        this.targetSize = targetSize;
        this.otherVerts = new ArrayList<>();
        this.similarities = new ArrayList<>();
    }
}
