package org.uu.nl.embedding.util.config;

import java.io.File;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ClusterConfiguration {

    private String embedding;

    private int threads;
    private float theta;
    private int k;
    private int correlationClusteringMaxSize;

    private ClusterSize clustersize;

    public int getThreads() {
        return threads == 0 ? (Runtime.getRuntime().availableProcessors() -1) : threads;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }

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

    public int getCorrelationClusteringMaxSize() {
        return correlationClusteringMaxSize == 0 ? 100 : correlationClusteringMaxSize;
    }

    public void setCorrelationClusteringMaxSize(int correlationClusteringMaxSize) {
        this.correlationClusteringMaxSize = correlationClusteringMaxSize;
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

        public boolean hasPredicate() {
            return predicate != null;
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

    public void check() throws InvalidConfigException {

        boolean endPoint = getRules().hasEndPoint();
        boolean graph = getRules().hasGraph();

        if(endPoint && graph) throw new InvalidConfigException("Error: both endpoint and graph file are specified");
        if(getK() <= 0) throw new InvalidConfigException("Error: k must be larger than 0");
        if(getTheta() <= -1) throw new InvalidConfigException("Error: theta must be larger than -1");
        if(getRules().getMaxQuerySize() <= 0) throw new InvalidConfigException("Error: maximum query size must be larger than 0");
    }
}
