package org.uu.nl.embedding.compare;

import info.debatty.java.stringsimilarity.interfaces.StringSimilarity;
import org.uu.nl.embedding.util.similarity.PreComputed;

import java.util.HashSet;
import java.util.Set;

/**
 * Associates a set of nodes with a similarity metric
 */
public class CompareGroup {

    public final StringSimilarity similarity;
    public final Set<Integer> source, target;
    public final double threshold;
    public final boolean upperTriangle;

    public CompareGroup(StringSimilarity similarity, double threshold, boolean upperTriangle) {
        this.similarity = similarity;
        this.source = new HashSet<>();
        this.target = new HashSet<>();
        this.threshold = threshold;
        this.upperTriangle = upperTriangle;
    }

    public boolean needsPrecompute(){
        return this.similarity instanceof PreComputed;
    }

    public void addToSource(int i){
        this.source.add(i);
    }

    public void addToTarget(int i){
        this.target.add(i);
    }
}
