package org.uu.nl.disembed.embedding.compare;

import grph.properties.Property;
import info.debatty.java.stringsimilarity.interfaces.StringSimilarity;
import org.uu.nl.disembed.embedding.similarity.lsh.LSHSimilarity;

import java.util.concurrent.Callable;

/**
 * Do the similarity matching in parallel
 */
public class CompareJob implements Callable<CompareResult> {

    private final int index;
    private final int source;
    private final int[] target;
    private final double threshold;
    private final StringSimilarity metric;
    private final Property vertexLabels;
    private final String sourceLabel;
    private final boolean inGroupComparison;

    public CompareJob(int index, int[] source, int[] target, double threshold, StringSimilarity metric, Property vertexLabels, boolean inGroupComparison) {
        this.index = index;
        this.source = source[index];
        this.target = target;
        this.threshold = threshold;
        this.metric = metric;
        this.vertexLabels = vertexLabels;
        this.sourceLabel = vertexLabels.getValueAsString(this.source);
        this.inGroupComparison = inGroupComparison;
    }

    @Override
    public CompareResult call() {

        final int vert = source;

        final int[] otherVerts =
                (metric instanceof LSHSimilarity) ? ((LSHSimilarity)metric).candidates(index, target) : target;
        final CompareResult result = new CompareResult(vert, otherVerts.length);

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
