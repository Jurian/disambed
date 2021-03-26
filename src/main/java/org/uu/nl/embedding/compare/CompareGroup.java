package org.uu.nl.embedding.compare;

import info.debatty.java.stringsimilarity.interfaces.StringSimilarity;
import org.apache.jena.graph.Node;
import org.uu.nl.embedding.util.config.Configuration;
import org.uu.nl.embedding.util.similarity.PreComputed;

import java.util.HashSet;
import java.util.Set;

/**
 * Associates a set of nodes with a similarity metric
 */
public class CompareGroup {

    public final StringSimilarity similarity;
    public final Set<Integer> sourceIndexes, targetIndexes;
    public final double threshold;
    public final boolean needsPrecompute;
    public final String sourceURI, sourcePredicate, targetURI, targetPredicate;
    public final Set<Node> sourceEntities, targetEntities;

    public CompareGroup(StringSimilarity similarity, double threshold, String sourceURI, String sourcePredicate, String targetURI, String targetPredicate) {
        this.similarity = similarity;
        this.sourceIndexes = new HashSet<>();
        this.targetIndexes = new HashSet<>();
        this.sourceEntities = new HashSet<>();
        this.targetEntities = new HashSet<>();
        this.threshold = threshold;
        this.needsPrecompute = similarity instanceof PreComputed;
        this.sourceURI = sourceURI;
        this.sourcePredicate = sourcePredicate;
        this.targetURI = targetURI;
        this.targetPredicate = targetPredicate;
    }

    public CompareGroup(Configuration.SimilarityGroup similarityGroup) {
        this(similarityGroup.toFunction(),
             similarityGroup.getThreshold(),
             similarityGroup.getSourceType(),
             similarityGroup.getSourcePredicate(),
             similarityGroup.getTargetType(),
             similarityGroup.getTargetPredicate());
    }

    public void process(Node o, int o_i) {

        if(sourceEntities.contains(o)) {
            sourceIndexes.add(o_i);
        }
        if(targetEntities.contains(o)) {
            targetIndexes.add(o_i);
        }
    }

    public boolean needsPrecompute(){
        return needsPrecompute;
    }

    public void addSourceIndex(int i){
        this.sourceIndexes.add(i);
    }

    public void addTargetIndex(int i){
        this.targetIndexes.add(i);
    }

    public void addSourceEntity(Node literal) {
        if (needsPrecompute()) ((PreComputed) similarity).preCompute(literal.toString(false));
        this.sourceEntities.add(literal);
    }

    public void addTargetEntity(Node literal) {
        if (needsPrecompute()) ((PreComputed) similarity).preCompute(literal.toString(false));
        this.targetEntities.add(literal);
    }
}
