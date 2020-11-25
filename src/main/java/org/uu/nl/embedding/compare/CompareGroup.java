package org.uu.nl.embedding.compare;

import info.debatty.java.stringsimilarity.interfaces.StringSimilarity;
import org.uu.nl.embedding.util.config.Configuration;
import org.uu.nl.embedding.util.similarity.PreComputed;
import org.apache.jena.graph.Node;
import java.util.HashSet;
import java.util.Set;

/**
 * Associates a set of nodes with a similarity metric
 */
public class CompareGroup {

    public final StringSimilarity similarity;
    public final Set<Integer> sourceNodes, targetNodes;
    public final double threshold;
    public final boolean needsPrecompute;
    public final String sourceURI, sourcePredicate, targetURI, targetPredicate;

    public CompareGroup(StringSimilarity similarity, double threshold, String sourceURI, String sourcePredicate, String targetURI, String targetPredicate) {
        this.similarity = similarity;
        this.sourceNodes = new HashSet<>();
        this.targetNodes = new HashSet<>();
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
             similarityGroup.getSourceURI(),
             similarityGroup.getSourcePredicate(),
             similarityGroup.getTargetURI(),
             similarityGroup.getTargetPredicate());
    }

    public void process(Node s, Node o, int o_i, String predicate) {

        final boolean processSourceNode =
                s.isURI() &&
                        s.getURI().startsWith(sourceURI) &&
                        predicate.equals(sourcePredicate);

        final boolean processTargetNode =
                s.isURI() &&
                        s.getURI().startsWith(targetURI) &&
                        predicate.equals(targetPredicate);

        if (processSourceNode) {
            addToSource(o_i);
            // Some similarity metrics require pre-processing
            if (needsPrecompute())
                ((PreComputed) similarity).preCompute(o.toString(false));
        }

        if (processTargetNode) {
            addToTarget(o_i);
            // Some similarity metrics require pre-processing
            if (needsPrecompute())
                ((PreComputed) similarity).preCompute(o.toString(false));
        }

    }

    public boolean needsPrecompute(){
        return needsPrecompute;
    }

    public void addToSource(int i){
        this.sourceNodes.add(i);
    }

    public void addToTarget(int i){
        this.targetNodes.add(i);
    }
}
