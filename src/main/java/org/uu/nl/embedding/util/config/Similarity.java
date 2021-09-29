package org.uu.nl.embedding.util.config;

import org.uu.nl.embedding.util.similarity.*;

public class Similarity  {

    public enum Time {
        BACKWARDS, FORWARDS, BIDIRECTIONAL, BEFORE, AFTER
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

    private String sourcePredicate;
    private String targetPredicate;
    private String method;
    private double threshold;
    private int ngram;
    private double offset;
    private double thresholdDistance;
    private double alpha;
    private String pattern;
    private String time;
    private LiteralSimilarity similarityFunction;

    @Override
    public String toString() {
        String out = "# " + getSourcePredicate() + " -> " + targetPredicate + ", method:" + method + ", threshold: " + threshold;
        switch (getMethodEnum()) {
            default:
                return out;
            case NGRAM_COSINE:
            case NGRAM_JACCARD:
                return out + ", ngram: " + ngram;
            case NUMERIC:
                return out + ", threshold distance: " + thresholdDistance + " , offset: " + offset;
            case DATE_DAYS:
            case DATE_MONTHS:
            case DATE_YEARS:
                return out + ", threshold distance: " + thresholdDistance + " , offset: " + offset + ", pattern:" + pattern + ", time: " + time;
        }
    }

    public LiteralSimilarity getSimilarityFunction() {
        if (this.similarityFunction == null) {
            this.similarityFunction = toFunction();
        }
        return this.similarityFunction;
    }

    /**
     * Instantiate a similarity object from the configuration information
     */
    private LiteralSimilarity toFunction() {
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

    public String getTime() {
        return time == null ? "bidirectional" : time;
    }

    public Time getTimeEnum() {
        return Time.valueOf(getTime().toUpperCase());
    }

    public void setTime(String time) {
        this.time = time;
    }

    public double getOffset() {
        return offset;
    }

    public void setOffset(double offset) {
        this.offset = offset;
    }

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

    public int getNgram() {
        return ngram == 0 ? 3 : ngram;
    }

    public void setNgram(int ngram) {
        this.ngram = ngram;
    }

    public double getAlpha() {
        return -Math.log(threshold) / Math.log(1 + thresholdDistance);
    }

    public void setThresholdDistance(double thresholdDistance) {
        this.thresholdDistance = thresholdDistance;
    }

    public double getThresholdDistance() {
        return this.thresholdDistance;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    private boolean optional = false;

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }
}
