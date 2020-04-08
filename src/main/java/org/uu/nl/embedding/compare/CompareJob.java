package org.uu.nl.embedding.compare;

import grph.properties.Property;
import info.debatty.java.stringsimilarity.interfaces.StringSimilarity;

import java.util.concurrent.Callable;

/**
 * Do the similarity matching in parallel
 */
public class CompareJob implements Callable<CompareResult> {

    private final int index;
    private final int startIndex;
    private final int[] source, target;
    private final double threshold;
    private final StringSimilarity metric;
    private final Property vertexLabels;


    public CompareJob(boolean upperTriangle, int index, int[] source, int[] target, double threshold, StringSimilarity metric, Property vertexLabels) {
        this.index = index;
        this.source = source;
        this.target = target;
        this.threshold = threshold;
        this.metric = metric;
        this.vertexLabels = vertexLabels;
        this.startIndex = upperTriangle ? index + 1 : 0;
    }

    @Override
    public CompareResult call() {

        final int vert = source[index];
        final CompareResult result = new CompareResult(vert);

        for (int j = startIndex; j < target.length; j++) {

            final int otherVert = target[j];
            if(otherVert == vert) continue;

            final String s1 = vertexLabels.getValueAsString(vert);
            final String s2 = vertexLabels.getValueAsString(otherVert);
            final double similarity = metric.similarity(s1, s2);

            if (similarity >= threshold) {
                result.otherVerts.add(otherVert);
                result.similarities.add((float) similarity);
            }

        }
        return result;
    }
}
