package org.uu.nl.embedding.compare;

import grph.properties.Property;
import info.debatty.java.stringsimilarity.interfaces.StringSimilarity;

import java.util.concurrent.Callable;

/**
 * Do the similarity matching in parallel
 */
public class CompareJob implements Callable<CompareResult> {

    private final int source;
    private final int[]  target;
    private final double threshold;
    private final StringSimilarity metric;
    private final Property vertexLabels;
    private final String sourceLabel;
    private final boolean inGroupComparison;

    public CompareJob(int source, int[] target, double threshold, StringSimilarity metric, Property vertexLabels, boolean inGroupComparison) {
        this.source = source;
        this.target = target;
        this.threshold = threshold;
        this.metric = metric;
        this.vertexLabels = vertexLabels;
        this.sourceLabel = vertexLabels.getValueAsString(source);
        this.inGroupComparison = inGroupComparison;
    }

    @Override
    public CompareResult call() {

        final int vert = source;
        final CompareResult result = new CompareResult(vert);

        for (final int otherVert : target) {

            if(inGroupComparison && vert > otherVert) continue;
            if (otherVert == vert) continue;

            final String targetLabel = vertexLabels.getValueAsString(otherVert);
            final double similarity = metric.similarity(sourceLabel, targetLabel);

            if (similarity >= threshold) {
                result.otherVerts.add(otherVert);
                result.similarities.add((float) similarity);
            }

        }
        return result;
    }
}
