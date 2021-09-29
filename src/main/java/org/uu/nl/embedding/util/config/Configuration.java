package org.uu.nl.embedding.util.config;


import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;
import org.uu.nl.embedding.util.rnd.ExtendedRandom;
import org.uu.nl.embedding.util.rnd.ThreadLocalSeededRandom;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

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

    private Map<String, String> prefixes;

    public Map<String, String> getPrefixes() {
        return prefixes;
    }

    public void setPrefixes(Map<String, String> prefixes) {
        this.prefixes = prefixes;
    }

    private List<SimilarityGroup> similarityGroups;

    public List<SimilarityGroup> getSimilarityGroups() {
        return similarityGroups;
    }

    public boolean usingSimilarity() {
        return similarityGroups != null && !similarityGroups.isEmpty();
    }

    public void setSimilarityGroups(List<SimilarityGroup> similarity) {
        this.similarityGroups = similarity;
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

    public static void check(Configuration config) throws InvalidConfigException {
        boolean hasDim = config.dim > 0;
        boolean hasGraph = config.graph != null && !config.graph.isEmpty();
        boolean hasMethod = config.method != null && !config.method.isEmpty();
        boolean hasBca = config.bca != null && config.bca.getAlpha() > 0 && config.bca.getEpsilon() > 0;
        boolean hasOut = config.output != null && config.output.getType() != null && config.output.getType().size() != 0;

        if(!hasDim) throw new InvalidConfigException("No dimension specified");
        if(!hasGraph) throw new InvalidConfigException("No input graph specified");
        if(!hasMethod) throw new InvalidConfigException("Invalid method, choose one of: glove, pglove");
        if(!hasBca) throw new InvalidConfigException("Invalid BCA parameters, alpha and epsilon are mandatory");
        if(!hasOut) throw new InvalidConfigException("Invalid output parameters, specify at least one type");
    }

}
