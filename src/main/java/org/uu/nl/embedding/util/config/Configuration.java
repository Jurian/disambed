package org.uu.nl.embedding.util.config;


import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;
import org.uu.nl.embedding.util.rnd.ExtendedRandom;
import org.uu.nl.embedding.util.rnd.ThreadLocalSeededRandom;
import org.uu.nl.embedding.util.similarity.*;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Configuration {

    public enum PredicateWeightingMethod {
        NONE, MANUAL, PAGERANK, FREQUENCY, INVERSE_FREQUENCY
    }

    public enum EmbeddingWriter {
        GLOVE, WORD2VEC, SPLIT
    }

    public enum EmbeddingMethod {
        GLOVE, PGLOVE
    }

    public enum OptimizationMethod {
        ADAGRAD, AMSGRAD, ADAM
    }

    public enum SimilarityMethod {
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

    public EmbeddingMethod getMethodEnum() {
        return EmbeddingMethod.valueOf(this.method.toUpperCase());
    }

    private List<SimilarityGroup> similarity;

    public List<SimilarityGroup> getSimilarity() {
        return similarity;
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

    private Output output;

    public Output getOutput() {
        return output;
    }

    public void setOutput(Output output) {
        this.output = output;
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

    public static ProgressBar progressBar(String name, long max, String unitName) {
        return new ProgressBar (
                name,
                max,
                250,
                System.out,
                ProgressBarStyle.COLORFUL_UNICODE_BLOCK,
                " " + unitName,
                1,
                false
        );
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

        /**
         * Instantiate a similarity object from the configuration information
         */
        public LiteralSimilarity toFunction() {

            switch (getMethodEnum()) {
                case NUMERIC:
                    return new Numeric(getAlpha(), getOffset());
                case DATE_DAYS:
                    return new DateDays(getPattern(), getAlpha(), getOffset(), getTimeEnum());
                case DATE_MONTHS:
                    return new DateMonths(getPattern(), getAlpha(), getOffset(), getTimeEnum());
                case DATE_YEARS:
                    return new DateYears(getPattern(), getAlpha(), getOffset(), getTimeEnum());
                case LEVENSHTEIN:
                    return new NormalizedLevenshtein();
                case JAROWINKLER:
                    return new JaroWinkler();
                case NGRAM_JACCARD:
                    return new PreComputedNgramJaccard(getNgram());
                case NGRAM_COSINE:
                    return new PreComputedNgramCosine(getNgram());
                case TOKEN_JACCARD:
                    return new PreComputedTokenJaccard();
                case TOKEN_COSINE:
                    return new PreComputedTokenCosine();
                case LOCATION:
                    return new Location(getAlpha(), getOffset());
                default:
                    throw new IllegalArgumentException("Unsupported similarity method: " + getMethodEnum());
            }
        }

        @Override
        public String toString() {
            String out = getSourcePredicate() + " -> " + getTargetPredicate() + ", method:" + getMethod() + ", threshold: " + getThreshold();
            switch (getMethodEnum()) {
                default: return out;
                case NGRAM_COSINE:
                case NGRAM_JACCARD: return out + ", ngram: " + getNgram();
                case NUMERIC: return out + ", threshold distance: " + getThresholdDistance() + " , offset: " + getOffset();
                case DATE_DAYS:
                case DATE_MONTHS:
                case DATE_YEARS: return out + ", threshold distance: " + getThresholdDistance() + " , offset: " + getOffset() + ", pattern:" + getPattern() + ", time: " + getTime();
            }
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

        private double alpha;
        private double epsilon;

        public double getAlpha() {
            return alpha;
        }

        public void setAlpha(double alpha) {
            this.alpha = alpha;
        }

        public double getEpsilon() {
            return epsilon;
        }

        public void setEpsilon(double epsilon) {
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

    public static class Output {

        private String writer;

        public void setWriter(String writer) {
            this.writer = writer;
        }
        public String getWriter() {return this.writer;}
        public EmbeddingWriter getWriterEnum() {
            return EmbeddingWriter.valueOf(writer.toUpperCase());
        }

        private String name;

        public List<String> getType() {
            return type;
        }

        public void setType(List<String> type) {
            this.type = type;
        }

        private List<String> type;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        private final Set<Integer> nodeIndex = new HashSet<>();

        public Set<Integer> getNodeIndex() {
            return nodeIndex;
        }

        public void addNodeIndex(int index) {
            this.nodeIndex.add(index);
        }
    }

    public static void check(Configuration config) throws InvalidConfigException {
        boolean hasDim = config.dim > 0;
        boolean hasGraph = config.graph != null && !config.graph.isEmpty();
        boolean hasMethod = config.method != null && !config.method.isEmpty();
        boolean hasBca = config.bca != null && config.bca.alpha > 0 && config.bca.epsilon > 0;
        boolean hasOut = config.output != null && config.output.getType() != null && config.output.getType().size() != 0;

        if(!hasDim) throw new InvalidConfigException("No dimension specified");
        if(!hasGraph) throw new InvalidConfigException("No input graph specified");
        if(!hasMethod) throw new InvalidConfigException("Invalid method, choose one of: glove, pglove");
        if(!hasBca) throw new InvalidConfigException("Invalid BCA parameters, alpha and epsilon are mandatory");
        if(!hasOut) throw new InvalidConfigException("Invalid output parameters, specify at least one type");
    }
}
