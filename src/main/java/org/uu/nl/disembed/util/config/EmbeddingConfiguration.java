package org.uu.nl.embedding.util.config;


import org.uu.nl.embedding.util.rnd.ExtendedRandom;
import org.uu.nl.embedding.util.rnd.ThreadLocalSeededRandom;
import org.uu.nl.embedding.util.similarity.*;
import org.uu.nl.embedding.util.similarity.lsh.LSHByteCharCosine;
import org.uu.nl.embedding.util.similarity.lsh.LSHByteCharJaccard;
import org.uu.nl.embedding.util.similarity.lsh.LSHNgramCosine;
import org.uu.nl.embedding.util.similarity.lsh.LSHNgramJaccard;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EmbeddingConfiguration {

    public enum PredicateWeightingMethod {
        NONE, MANUAL, PAGERANK, FREQUENCY, INVERSE_FREQUENCY
    }

    public enum EmbeddingMethod {
        GLOVE, PGLOVE
    }

    public enum OptimizationMethod {
        ADAGRAD, AMSGRAD, ADAM
    }

    public enum SimilarityMethod {
        LSH_NGRAM_COSINE,
        LSH_NGRAM_JACCARD,
        LSH_BIT_JACCARD,
        LSH_BIT_COSINE,
        NGRAM_COSINE,
        NGRAM_JACCARD,
        TOKEN_COSINE,
        TOKEN_JACCARD,
        JAROWINKLER,
        LEVENSHTEIN,
        NUMERIC,
        DATE_DAYS,
        DATE_MONTHS,
        DATE_YEARS,
        LOCATION
    }

    public enum BCANormalization {
        NONE, UNITY, COUNTS
    }

    private String graph;

    public File getGraphFile() {
        return Paths.get("").toAbsolutePath().resolve(graph).toFile();
    }

    public String getGraph() {
        return graph;
    }

    public void setGraph(String graph) {
        this.graph = graph;
    }

    private String method;

    public String getMethod() {
        return  method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    private int dim;

    public int getDim() {
        return dim;
    }

    public void setDim(int dim) {
        this.dim = dim;
    }

    private int threads;

    public int getThreads() {
        return threads == 0 ? (Runtime.getRuntime().availableProcessors() -1) : threads;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }

    private List<String> targetTypes;

    public List<String> getTargetTypes() {
        return targetTypes;
    }

    public void setTargetTypes(List<String> type) {
        this.targetTypes = type;
    }

    private Map<String, String> prefixes;

    public void setPrefixes(Map<String, String> prefixes) {
        this.prefixes = prefixes;
    }

    public Map<String, String> getPrefixes() {
        return this.prefixes;
    }

    public EmbeddingMethod getMethodEnum() {
        return EmbeddingMethod.valueOf(this.method.toUpperCase());
    }

    private List<SimilarityGroup> similarity;

    public List<SimilarityGroup> getSimilarity() {
        return similarity;
    }

    public boolean hasSimilarity() {
        return similarity != null && similarity.size() != 0;
    }

    public boolean usingSimilarity() {
        return similarity != null && !similarity.isEmpty();
    }

    public void setSimilarity(List<SimilarityGroup> similarity) {
        this.similarity = similarity;
    }

    private PredicateWeights predicates;

    public void setPredicates(PredicateWeights predicates) {
        this.predicates = predicates;
    }

    public PredicateWeights getPredicates() {
        return this.predicates;
    }

    private BCA bca;

    public BCA getBca() {
        return bca;
    }

    public void setBca(BCA bca) {
        this.bca = bca;
    }

    private Opt opt;

    public Opt getOpt() {
        return opt;
    }

    public void setOpt(Opt opt) {
        this.opt = opt;
    }

    private static ThreadLocalSeededRandom threadLocalRandom;

    public static void setThreadLocalRandom() {
        threadLocalRandom = new ThreadLocalSeededRandom(System.currentTimeMillis());
    }

    public static void setThreadLocalRandom(long seed) {
        threadLocalRandom = new ThreadLocalSeededRandom(seed);
    }

    public static ExtendedRandom getThreadLocalRandom() {
        return threadLocalRandom.get();
    }

    public static class PredicateWeights {

        private String type;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public PredicateWeightingMethod getTypeEnum() {
            if(this.type == null) return PredicateWeightingMethod.NONE;
            return PredicateWeightingMethod.valueOf(this.type.toUpperCase());
        }

        private Set<String> filter;

        public Set<String> getFilter(){
            return  this.filter;
        }

        public void setFilter(Set<String> filter) {
            this.filter = filter;
        }

        private Map<String, Float> weights;

        public boolean usingManualWeights() {
            return getTypeEnum() == PredicateWeightingMethod.MANUAL;
        }

        public Map<String, Float> getWeights() {
            return weights;
        }

        public void setWeights(Map<String, Float> weights) {
            this.weights = weights;
        }

        public boolean usingPageRankWeights() {
            return getTypeEnum() == PredicateWeightingMethod.PAGERANK;
        }

        public boolean usingNoWeights() {
            return getTypeEnum() == PredicateWeightingMethod.NONE;
        }
    }

    public static class SimilarityGroup {

        public enum Time {
            BACKWARDS, FORWARDS, BIDIRECTIONAL
        }

        private String sourcePredicate;
        private String targetPredicate;

        public String getSourceType() {
            return sourceType;
        }

        public void setSourceType(String sourceType) {
            this.sourceType = sourceType;
        }

        public String getTargetType() {
            return targetType;
        }

        public void setTargetType(String targetType) {
            this.targetType = targetType;
        }

        private String sourceType;
        private String targetType;
        private String method;
        private double threshold;
        private int ngram;
        private double offset;
        private double thresholdDistance;
        private double alpha;
        private String pattern;
        private String time;
        private int bands;
        private int buckets;

        /**
         * Instantiate a similarity object from the configuration information
         */
        public LiteralSimilarity toFunction() {

            return switch (getMethodEnum()) {
                case LSH_NGRAM_COSINE -> new LSHNgramCosine(getNgram(), getBands(), getBuckets());
                case LSH_NGRAM_JACCARD -> new LSHNgramJaccard(getNgram(), getBands(), getBuckets());
                case LSH_BIT_COSINE -> new LSHByteCharCosine(getBands(), getBuckets());
                case LSH_BIT_JACCARD -> new LSHByteCharJaccard(getBands(), getBuckets());
                case NUMERIC -> new Numeric(getAlpha(), getOffset());
                case DATE_DAYS -> new DateDays(getPattern(), getAlpha(), getOffset(), getTimeEnum());
                case DATE_MONTHS -> new DateMonths(getPattern(), getAlpha(), getOffset(), getTimeEnum());
                case DATE_YEARS -> new DateYears(getPattern(), getAlpha(), getOffset(), getTimeEnum());
                case LEVENSHTEIN -> new NormalizedLevenshtein();
                case JAROWINKLER -> new JaroWinkler();
                case NGRAM_JACCARD -> new PreComputedNgramJaccard(getNgram());
                case NGRAM_COSINE -> new PreComputedNgramCosine(getNgram());
                case TOKEN_JACCARD -> new PreComputedTokenJaccard();
                case TOKEN_COSINE -> new PreComputedTokenCosine();
                case LOCATION -> new Location(getAlpha(), getOffset());
            };
        }

        @Override
        public String toString() {
            String out = getSourcePredicate() + " -> " + getTargetPredicate() + ", method:" + getMethod() + ", threshold: " + getThreshold();
            return switch (getMethodEnum()) {
                default -> out;
                case NGRAM_COSINE, NGRAM_JACCARD -> out + ", ngram: " + getNgram();
                case NUMERIC -> out + ", threshold distance: " + getThresholdDistance() + " , offset: " + getOffset();
                case DATE_DAYS, DATE_MONTHS, DATE_YEARS -> out + ", threshold distance: " + getThresholdDistance() + " , offset: " + getOffset() + ", pattern:" + getPattern() + ", time: " + getTime();
            };
        }

        public int getBands() {
            return bands;
        }

        public int getBuckets() {
            return buckets;
        }

        public void setBands(int bands) {
            this.bands = bands;
        }

        public void setBuckets(int buckets) {
            this.buckets = buckets;
        }

        public String getTime() {
            return time == null ? "bidirectional" : time;
        }

        public Time getTimeEnum() {
            return Time.valueOf(getTime().toUpperCase());
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getTargetPredicate() {
            return targetPredicate;
        }

        public void setTargetPredicate(String targetPredicate) {
            this.targetPredicate = targetPredicate;
        }

        public String getSourcePredicate() {
            return sourcePredicate;
        }

        public void setSourcePredicate(String sourcePredicate) {
            this.sourcePredicate = sourcePredicate;
        }

        public double getOffset() { return offset; }

        public void setOffset(double offset) { this.offset = offset; }

        public SimilarityMethod getMethodEnum() {
            return SimilarityMethod.valueOf(this.method.toUpperCase());
        }

        public String getPattern() {
            return pattern == null ? "iso" : pattern;
        }

        public void setPattern(String pattern) {
            this.pattern = pattern;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public double getThreshold() {
            return threshold;
        }

        public void setThreshold(double threshold) {
            this.threshold = threshold;
        }

        public int getNgram() {
            return ngram == 0 ? 3 : ngram;
        }

        public void setNgram(int ngram) {
            this.ngram = ngram;
        }

        public double getAlpha() {return -Math.log(threshold) / Math.log(1 + thresholdDistance);}

        public void setThresholdDistance(double thresholdDistance) {
            this.thresholdDistance = thresholdDistance;
        }

        public double getThresholdDistance() {
            return this.thresholdDistance;
        }
    }

    public static class BCA {

        private float alpha;
        private float epsilon;

        public float getAlpha() {
            return alpha;
        }

        public void setAlpha(float alpha) {
            this.alpha = alpha;
        }

        public float getEpsilon() {
            return epsilon;
        }

        public void setEpsilon(float epsilon) {
            this.epsilon = epsilon;
        }

    }

    public static class Opt {

        private String method;
        private double tolerance;
        private int maxiter;

        public OptimizationMethod getMethodEnum() {
            return OptimizationMethod.valueOf(method.toUpperCase());
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public double getTolerance() {
            return tolerance;
        }

        public void setTolerance(double tolerance) {
            this.tolerance = tolerance;
        }

        public int getMaxiter() {
            return maxiter;
        }

        public void setMaxiter(int maxiter) {
            this.maxiter = maxiter;
        }
    }


    public void check() throws InvalidConfigException {
        boolean hasDim = dim > 0;
        boolean hasGraph = graph != null && !graph.isEmpty();
        boolean hasMethod = method != null && !method.isEmpty();
        boolean hasBca = bca != null && bca.alpha > 0 && bca.epsilon > 0;
        boolean hasTarget = getTargetTypes() != null && getTargetTypes().size() != 0;
        boolean hasSimilarity = similarity  != null && !similarity.isEmpty();

        if(!hasDim) throw new InvalidConfigException("No dimension specified");
        if(!hasGraph) throw new InvalidConfigException("No input graph specified");
        if(!hasMethod) throw new InvalidConfigException("Invalid method, choose one of: glove, pglove");
        if(!hasBca) throw new InvalidConfigException("Invalid BCA parameters, alpha and epsilon are mandatory");
        if(!hasTarget) throw new InvalidConfigException("Invalid target parameters, specify at least one type");

        if(hasSimilarity && getSimilarity().stream().anyMatch(s ->
                !(s.sourcePredicate.equals(s.targetPredicate) && s.sourceType.equals(s.targetType))
                        &&
                 (s.getMethodEnum().toString().contains("LSH"))
        )) {
            throw new InvalidConfigException("Currently LSH only supports in group comparisons");
        }

    }
}
