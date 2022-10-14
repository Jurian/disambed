package org.uu.nl.disembed.util.config;

import org.uu.nl.disembed.util.write.EmbeddingWriter;
import org.uu.nl.disembed.util.write.HnswIndexWriter;

import java.io.File;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ClusterConfiguration  implements Configurable {

    private String embedding;
    private String hnsw;

    private float theta;
    private int k;
    private int maxCorrelationClusteringSize;
    private int maxComponentSize;

    private ClusterSize clustersize;

    public float getTheta() {
        return theta;
    }

    public void setTheta(float theta) {
        this.theta = theta;
    }

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }

    public int getMaxCorrelationClusteringSize() {
        return maxCorrelationClusteringSize == 0 ? 100 : maxCorrelationClusteringSize;
    }

    public void setMaxCorrelationClusteringSize(int maxCorrelationClusteringSize) {
        this.maxCorrelationClusteringSize = maxCorrelationClusteringSize;
    }

    public int getMaxComponentSize() {
        return maxComponentSize;
    }

    public void setMaxComponentSize(int maxComponentSize) {
        this.maxComponentSize = maxComponentSize;
    }

    public ClusterSize getClustersize() {
        return clustersize;
    }

    public void setClustersize(ClusterSize clustersize) {
        this.clustersize = clustersize;
    }

    public File getEmbeddingFile() {
        return Paths.get("").toAbsolutePath().resolve(embedding).toFile();
    }

    public String getEmbedding(){
        return this.embedding;
    }

    public void setEmbedding(String embedding) {
        this.embedding = embedding;
    }

    public String getHnsw() {
        return hnsw;
    }

    public void setHnsw(String hnsw) {
        this.hnsw = hnsw;
    }

    public static class RuleConfiguration implements Iterable<ClusterConfiguration.Rule> {

        private String graph;
        private String endpoint;

        private int maxQuerySize;

        private Map<String, String> prefixes;
        private String typeFrom, typeTo;
        private List<ProbabilisticRule> probabilistic;
        private List<DefiniteRule> definite;

        public boolean hasEndPoint() {
            return endpoint != null;
        }

        public boolean hasGraph() {
            return graph != null;
        }

        public File getGraphFile() {
            return Paths.get("").toAbsolutePath().resolve(graph).toFile();
        }

        public String getGraph() {
            return this.graph;
        }

        public void setGraph(String graph) {
            this.graph = graph;
        }

        public boolean hasProbabilisticRules() {
            return probabilistic != null && probabilistic.size() > 0;
        }

        public boolean hasDefiniteRules() {
            return definite != null && definite.size() > 0;
        }

        public int ruleCount() {
            return definiteRuleCount() + probabilisticRuleCount();
        }

        public int definiteRuleCount() {
            return  hasDefiniteRules() ? definite.size() : 0;
        }

        public int probabilisticRuleCount() {
            return hasProbabilisticRules() ? probabilistic.size() : 0;
        }

        public List<ProbabilisticRule> getProbabilistic() {
            return probabilistic;
        }

        public void setProbabilistic(List<ProbabilisticRule> probabilistic) {
            this.probabilistic = probabilistic;
        }

        public List<DefiniteRule> getDefinite() {
            return definite;
        }

        public void setDefinite(List<DefiniteRule> definite) {
            this.definite = definite;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public Map<String, String> getPrefixes() {
            return prefixes;
        }

        public void setPrefixes(Map<String, String> prefixes) {
            this.prefixes = prefixes;
        }

        public String getTypeFrom() {
            return typeFrom;
        }

        public void setTypeFrom(String typeFrom) {
            this.typeFrom = typeFrom;
        }

        public String getTypeTo() {
            return typeTo;
        }

        public void setTypeTo(String typeTo) {
            this.typeTo = typeTo;
        }

        public int getMaxQuerySize() {
            return maxQuerySize;
        }

        public void setMaxQuerySize(int maxQuerySize) {
            this.maxQuerySize = maxQuerySize;
        }

        @Override
        public Iterator<Rule> iterator() {

            return new Iterator<>() {

                int i = 0;
                final int n = ruleCount();

                @Override
                public boolean hasNext() {
                    return i < n;
                }

                @Override
                public Rule next() {

                    Rule r = null;

                    if (hasDefiniteRules() && i < definite.size()) {
                        r = definite.get(i);
                    } else if (hasProbabilisticRules()) {
                        r = probabilistic.get(i - definite.size());
                    }

                    i++;
                    return r;
                }
            };
        }
    }


    private RuleConfiguration rules;

    public RuleConfiguration getRules() {
        return rules;
    }

    public void setRules(RuleConfiguration rules) {
        this.rules = rules;
    }


    public static class ClusterSize {
        public int min, max;

        public int getMin() {
            return min == 0 ? 2 : min;
        }

        public void setMin(int min) {
            this.min = min;
        }

        public int getMax() {
            return max;
        }

        public void setMax(int max) {
            this.max = max;
        }
    }

    public static class Pattern {
        private String subject;
        private String predicate;
        private String object;
        private boolean optional;

        public boolean hasSubject() {
            return subject != null;
        }

        public boolean hasObject() {
            return object != null;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public String getPredicate() {
            return predicate;
        }

        public void setPredicate(String predicate) {
            this.predicate = predicate;
        }

        public String getObject() {
            return object;
        }

        public void setObject(String object) {
            this.object = object;
        }

        public boolean isOptional() {
            return optional;
        }

        public void setOptional(boolean optional) {
            this.optional = optional;
        }

    }

    public static abstract class Rule {
        private List<Pattern> triples1;
        private List<Pattern> triples2;
        private List<String> rule;

        public List<Pattern> getTriples1() {
            return triples1;
        }

        public void setTriples1(List<Pattern> triples1) {
            this.triples1 = triples1;
        }

        public List<Pattern> getTriples2() {
            return triples2;
        }

        public void setTriples2(List<Pattern> triples2) {
            this.triples2 = triples2;
        }

        public List<String> getRule() {
            return rule;
        }

        public void setRule(List<String> rule) {
            this.rule = rule;
        }
    }

    public static class DefiniteRule extends Rule { }

    public static class ProbabilisticRule extends Rule {
        private float probability;

        public float getProbability() {
            return probability;
        }

        public void setProbability(float probability) {
            this.probability = probability;
        }
    }

    @Override
    public String toString() {
        return getBuilder().toString();
    }

    public void check() throws InvalidConfigException {

        boolean endPoint = getRules().hasEndPoint();
        boolean graph = getRules().hasGraph();

        if(endPoint && graph) throw new InvalidConfigException("Error: both endpoint and graph file are specified");
        if(getK() <= 0) throw new InvalidConfigException("Error: k must be larger than 0");
        if(getTheta() <= -1) throw new InvalidConfigException("Error: theta must be larger than -1");
        if(getRules().getMaxQuerySize() <= 0) throw new InvalidConfigException("Error: maximum query size must be larger than 0");
        if(getMaxComponentSize() <= 0) throw new InvalidConfigException("Error: maximum component size must be larger than 0");
        if(getMaxCorrelationClusteringSize() <= 2) throw new InvalidConfigException("Error: maximum correlation clustering size must be larger than 0");
    }

    @Override
    public CommentStringBuilder getBuilder() {
        CommentStringBuilder builder = new CommentStringBuilder();

        builder.appendLine("Cluster Configuration:");
        builder.appendKeyValueLine("k", getK());
        builder.appendKeyValueLine("Theta", getTheta());
        builder.appendKeyValueLine("Component max size", getMaxComponentSize());
        builder.appendKeyValueLine("Correlation clustering max size", getMaxCorrelationClusteringSize());
        builder.appendKeyValueLine("Theta", getTheta());

        if(getClustersize() != null) {
            builder.appendKeyValueLine("Clustering min size", getClustersize().getMin());
            builder.appendKeyValueLine("Clustering min size", getClustersize().getMax());
        }

        if(getEmbedding() != null) {
            builder.appendLine("Loading embedding from:");
            builder.append(EmbeddingWriter.OUTPUT_DIRECTORY);
            builder.append("/");
            builder.append(getEmbedding());
            builder.appendLine(EmbeddingWriter.FILETYPE);
        }

        if(getHnsw() != null) {
            builder.appendLine("Loading HNSW index from:");
            builder.append(HnswIndexWriter.OUTPUT_DIRECTORY);
            builder.append("/");
            builder.append(getHnsw());
            builder.appendLine(HnswIndexWriter.FILETYPE);
        }

        if(getRules() != null) {

            builder.appendLine("Rule Configuration:");

            if(getRules().hasGraph())
                builder.appendKeyValueLine("RDF Graph", getRules().getGraph());
            if(getRules().hasEndPoint())
                builder.appendKeyValueLine("Endpoint", getRules().getEndpoint());

            builder.appendKeyValueLine("Max Query Size", getRules().getMaxQuerySize());
            builder.appendKeyValueLine("Type From", getRules().getTypeFrom());
            builder.appendKeyValueLine("Type To", getRules().getTypeTo());

            if(getRules().getPrefixes() != null) {
                builder.appendLine("Prefixes:");
                for(Map.Entry<String, String> entry : getRules().getPrefixes().entrySet()) {
                    builder.appendKeyValueLine(entry.getKey(), entry.getValue());
                }
                builder.appendLine();
            } else {
                builder.appendLine("No prefixes specified.");
            }

            if(getRules().hasDefiniteRules()) {
                builder.appendLine("Definite Rules:");

                int i = 1;

                for(DefiniteRule rule : getRules().getDefinite()) {

                    builder.appendLine();
                    builder.appendKeyValueLine("Rule", i++);

                    if(rule.getTriples1() != null && !rule.getTriples1().isEmpty()) {
                        for(Pattern p : rule.getTriples1()) {
                            builder.append(p.hasSubject() ? p.getSubject() : "?e1");
                            builder.appendNoComment("\t");
                            builder.appendNoComment(p.getPredicate());
                            builder.appendNoComment("\t");
                            builder.appendNoComment(p.hasObject() ? p.getObject() : "?e1");
                            builder.appendLine();
                        }
                        builder.appendLine();
                    }

                    if(rule.getTriples2() != null && !rule.getTriples2().isEmpty()) {
                        for(Pattern p : rule.getTriples2()){
                            builder.append(p.hasSubject() ? p.getSubject() : "?e2");
                            builder.appendNoComment("\t");
                            builder.appendNoComment(p.getPredicate());
                            builder.appendNoComment("\t");
                            builder.appendNoComment(p.hasObject() ? p.getObject() : "?e2");
                            builder.appendLine();
                        }
                        builder.appendLine();
                    }


                    for(int j = 0; j < rule.getRule().size(); j++){
                        builder.append(rule.getRule().get(j));
                        if(j < rule.getRule().size() - 1) {
                            builder.appendNoComment("\tOR\n");
                        }
                    }
                }

                builder.appendLine();
            }

            if(getRules().hasProbabilisticRules()) {

                builder.appendLine("Probabilistic Rules:");

                int i = 1;

                for(ProbabilisticRule rule : getRules().getProbabilistic()) {

                    builder.appendLine();
                    builder.appendKeyValueLine("Rule", i++);

                    builder.appendKeyValueLine("Probability", rule.getProbability());

                    for(Pattern p : rule.getTriples1()) {
                        builder.append(p.hasSubject() ? p.getSubject() : "?e1");
                        builder.appendNoComment("\t");
                        builder.appendNoComment(p.getPredicate());
                        builder.appendNoComment("\t");
                        builder.appendNoComment(p.hasObject() ? p.getObject() : "?e1");
                        builder.appendLineNoComment();
                    }
                    builder.appendLine();

                    for(Pattern p : rule.getTriples2()){
                        builder.append(p.hasSubject() ? p.getSubject() : "?e2");
                        builder.appendNoComment("\t");
                        builder.appendNoComment(p.getPredicate());
                        builder.appendNoComment("\t");
                        builder.appendNoComment(p.hasObject() ? p.getObject() : "?e2");
                        builder.appendLineNoComment();
                    }
                    builder.appendLine();

                    for(int j = 0; j < rule.getRule().size(); j++){
                        builder.append(rule.getRule().get(j));
                        if(j < rule.getRule().size() - 1) {
                            builder.appendNoComment("\tOR\n");
                        }else {
                            builder.appendLineNoComment();
                        }
                    }
                }

                builder.appendLine();
            }
        }

        return builder;
    }
}
