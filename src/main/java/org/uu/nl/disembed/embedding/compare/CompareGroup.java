package org.uu.nl.disembed.embedding.compare;

import info.debatty.java.stringsimilarity.interfaces.StringSimilarity;
import org.apache.jena.graph.Node;
import org.uu.nl.disembed.embedding.similarity.PostComputed;
import org.uu.nl.disembed.embedding.similarity.PreComputed;
import org.uu.nl.disembed.util.config.EmbeddingConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * Associates a set of nodes with a similarity metric
 */
public class CompareGroup {

    public final StringSimilarity similarity;
    public int[] sourceIndexes, targetIndexes;
    public final double threshold;
    public final boolean needsPreCompute;
    public final boolean needsPostCompute;
    public final String sourceURI, sourcePredicate, targetURI, targetPredicate;
    public final Map<String, Integer> sourceEntities, targetEntities;

    public CompareGroup(StringSimilarity similarity, double threshold, String sourceURI, String sourcePredicate, String targetURI, String targetPredicate) {
        this.similarity = similarity;
        this.sourceEntities = new HashMap<>();
        this.targetEntities = new HashMap<>();
        this.threshold = threshold;
        this.needsPreCompute = similarity instanceof PreComputed;
        this.needsPostCompute = similarity instanceof PostComputed;
        this.sourceURI = sourceURI;
        this.sourcePredicate = sourcePredicate;
        this.targetURI = targetURI;
        this.targetPredicate = targetPredicate;
    }

    public CompareGroup(EmbeddingConfiguration.SimilarityGroup similarityGroup) {
        this(similarityGroup.toFunction(),
             similarityGroup.getThreshold(),
             similarityGroup.getSourceType(),
             similarityGroup.getSourcePredicate(),
             similarityGroup.getTargetType(),
             similarityGroup.getTargetPredicate());
    }

    public boolean inGroupComparison() {
       return sourceURI.equals(targetURI) && sourcePredicate.equals(targetPredicate);
    }

    public void initIndexes() {
        sourceIndexes = new int[sourceEntities.size()];
        targetIndexes = new int[targetEntities.size()];

        if(needsPostCompute()) ((PostComputed)similarity).postCompute();
    }

    public void process(Node o, int o_i) {
        String key = o.toString(false);
        if(sourceEntities.containsKey(key)) {
            sourceIndexes[sourceEntities.get(key)] = o_i;
        }
        if(targetEntities.containsKey(key)) {
            targetIndexes[targetEntities.get(key)] = o_i;
        }
    }

    public boolean needsPreCompute(){
        return needsPreCompute;
    }
    public boolean needsPostCompute() { return needsPostCompute; }

    public void addSourceEntity(Node literal) {
        String key = literal.toString(false);
        if(!sourceEntities.containsKey(key)) {
            int value = sourceEntities.size();
            if (needsPreCompute()) ((PreComputed<?>) similarity).preCompute(key, value);
            this.sourceEntities.put(key, value);
        }
    }

    public void addTargetEntity(Node literal) {
        String key = literal.toString(false);
        if(!targetEntities.containsKey(key)) {
            int value = targetEntities.size();
            if (needsPreCompute()) ((PreComputed<?>) similarity).preCompute(key, value);
            this.targetEntities.put(key, value);
        }
    }
}
