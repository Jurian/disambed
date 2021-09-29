package org.uu.nl.embedding.util.config;

import java.util.List;

public class SimilarityGroup {

    public enum ResultFunction {
        MIN, MAX, AVG, HRM
    }

    private String sourceType;
    private String targetType;
    private double threshold;
    private List<Condition> conditions;
    private List<Similarity> similarities;
    private String resultFunction;
    private ResultFunction resultFunctionEnum;

    public List<Condition> getConditions() {
        return conditions;
    }

    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }

    public List<Similarity> getSimilarities() {
        return similarities;
    }

    public void setSimilarities(List<Similarity> similarities) {
        this.similarities = similarities;
    }

    public String getResultFunction() {
        return this.resultFunction;
    }

    public void setResultFunctionEnum(ResultFunction resultFunctionEnum) {
        this.resultFunctionEnum = resultFunctionEnum;
    }

    public void setResultFunction(String resultFunction) {
        this.resultFunction = resultFunction;
        setResultFunctionEnum(ResultFunction.valueOf(resultFunction));
    }

    public ResultFunction getResultFunctionEnum() {
        return this.resultFunctionEnum;
    }

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

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }


    @Override
    public String toString() {
        StringBuilder out = new StringBuilder("# " + sourceType + " -> " + targetType + "\n");
        if (conditions != null && conditions.size() != 0) {
            out.append("# Conditions:\n");
            for (Condition c : conditions) {
                out.append(c.toString()).append("\n");
            }
        } else {
            out.append("# No conditions\n");
        }

        if (similarities != null && similarities.size() != 0) {
            out.append("# Similarities:\n");
            for (Similarity s : similarities) {
                out.append(s.toString()).append("\n");
            }
        } else {
            out.append("# No similarities\n");
        }
        return out.toString();
    }

}
