package org.uu.nl.disembed.util.config;


import org.uu.nl.disembed.embedding.similarity.*;
import org.uu.nl.disembed.embedding.similarity.lsh.LSHByteCharCosine;
import org.uu.nl.disembed.embedding.similarity.lsh.LSHByteCharJaccard;
import org.uu.nl.disembed.embedding.similarity.lsh.LSHNgramCosine;
import org.uu.nl.disembed.embedding.similarity.lsh.LSHNgramJaccard;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EmbeddingConfiguration  implements Configurable {

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
        MYERS,
        LEVENSHTEIN,
        NUMERIC,
        DATE_DAYS,
        DATE_MONTHS,
        DATE_YEARS,
        LOCATION
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
                case MYERS -> new NormalizedMyers();
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

        public  enum Type {
            DEFAULT, NO_RETURN
        }

        private String type;
        private float alpha;
        private float epsilon;
        private String readFile;

        public Type getTypeEnum() {
            return Type.valueOf(getType().toUpperCase());
        }

        public String getType() {
            return type == null || type.isEmpty() ? Type.DEFAULT.name() : this.type;
        }

        public void setType(String type) {
            this.type = type;
        }

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

        public File getImportFile() {
            return Paths.get("").toAbsolutePath().resolve(readFile).toFile();
        }

        public String getReadFile() {
            return readFile;
        }

        public void setReadFile(String readFile) {
            this.readFile = readFile;
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

    @Override
    public String toString() {
        return getBuilder().toString();
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

    @Override
    public CommentStringBuilder getBuilder() {
        CommentStringBuilder builder = new CommentStringBuilder();

        builder.appendLine("Embedding Configuration:");

        builder.appendKeyValueLine("Embedding method", getMethodEnum().toString());
        builder.appendKeyValueLine("Input RDF graph", getGraph());
        builder.appendKeyValueLine("Dimensions", getDim());

        if(getTargetTypes() != null && !getTargetTypes().isEmpty()) {
            builder.appendLine("Embedding entities of type:");
            builder.appendLine("\t"+String.join(", ", getTargetTypes()));
        } else {
            builder.appendLine("No target types specified, all entities will be embedded");
        }

        if(getPredicates().getFilter() != null && !getPredicates().getFilter().isEmpty()) {
            builder.appendLine("Predicate filter:");
            for(String filter : getPredicates().getFilter()) {
                builder.appendLine("\t"+filter);
            }
        } else {
            builder.appendLine("No predicate filter specified");
        }

        builder.appendKeyValueLine("Predicate weighing method", getPredicates().getTypeEnum().toString());
        if(getPredicates().getTypeEnum() == PredicateWeightingMethod.MANUAL) {
            for (String s : getPredicates().getFilter()) {
                builder.appendKeyValueLine("\t"+s, getPredicates().getWeights().getOrDefault(s, 1.0F));
            }
        }

        if(getSimilarity() != null && !getSimilarity().isEmpty()) {
            builder.appendLine("Similarity groups:");
            int i = 1;
            for(SimilarityGroup group : getSimilarity()) {

                builder.appendKeyValueLine("Group", i++);
                builder.appendKeyValueLine("Threshold", group.getThreshold());
                builder.appendKeyValueLine("Threshold distance", group.getThresholdDistance());
                builder.appendKeyValueLine("Source predicate", group.getSourcePredicate());
                builder.appendKeyValueLine("Source type", group.getSourceType());
                builder.appendKeyValueLine("Target predicate", group.getTargetPredicate());
                builder.appendKeyValueLine("Target type", group.getTargetType());

                builder.appendKeyValueLine("Comparison method", group.getMethodEnum().toString());

                switch (group.getMethodEnum()) {
                    case LSH_NGRAM_COSINE, LSH_NGRAM_JACCARD -> {
                        builder.appendKeyValueLine("Bands", group.getBands());
                        builder.appendKeyValueLine("Buckets", group.getBuckets());
                        builder.appendKeyValueLine("N-gram size", group.getNgram());
                    }

                    case LSH_BIT_COSINE, LSH_BIT_JACCARD -> {
                        builder.appendKeyValueLine("Bands", group.getBands());
                        builder.appendKeyValueLine("Buckets", group.getBuckets());
                    }

                    case NGRAM_COSINE, NGRAM_JACCARD -> builder.appendKeyValueLine("N-gram size", group.getNgram());

                    case NUMERIC -> builder.appendKeyValueLine("Offset", group.getOffset());

                    case LOCATION -> builder.appendKeyValueLine("Kilometer offset", group.getOffset());

                    case DATE_YEARS, DATE_MONTHS, DATE_DAYS -> {
                        builder.appendKeyValueLine("Pattern", group.getPattern());
                        builder.appendKeyValueLine("Time direction", group.getTimeEnum().toString());
                        builder.appendKeyValueLine("Time offset", group.getOffset());
                    }
                }

                builder.appendLine();
            }

        } else {
            builder.appendLine("No similarity comparisons specified");
        }

        builder.appendLine("BCA Configuration:");
        builder.appendKeyValueLine("Alpha",getBca().getAlpha());
        builder.appendKeyValueLine("Epsilon", getBca().getEpsilon());

        if(getBca().getReadFile() != null) {
            builder.appendKeyValueLine("Reading from file", getBca().getReadFile());
        }

        builder.appendLine();

        builder.appendLine("Gradient Descent Configuration:");
        builder.appendKeyValueLine("Method", getOpt().getMethodEnum().toString());
        builder.appendKeyValueLine("Maximum iterations", getOpt().getMaxiter());
        builder.appendKeyValueLine("Tolerance", getOpt().getTolerance());

        return builder;
    }
}
