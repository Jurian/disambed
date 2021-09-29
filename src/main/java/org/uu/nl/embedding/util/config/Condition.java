package org.uu.nl.embedding.util.config;

import org.uu.nl.embedding.util.condition.DateCondition;
import org.uu.nl.embedding.util.condition.IdenticalCondition;
import org.uu.nl.embedding.util.condition.LiteralCondition;

public class Condition {

    public enum ConditionMethod {
        IDENTICAL,
        BEFORE,
        AFTER
    }

    private String sourcePredicate;
    private String targetPredicate;
    private String method = "IDENTICAL";
    private String pattern;
    private LiteralCondition conditionFunction;

    @Override
    public String toString() {
        return "# " + getSourcePredicate() + " -> " + targetPredicate + ", method:" + method;
    }

    public LiteralCondition getConditionFunction() {
        if (this.conditionFunction == null) {
            this.conditionFunction = toFunction();
        }
        return this.conditionFunction;
    }

    /**
     * Instantiate a similarity object from the configuration information
     */
    private LiteralCondition toFunction() {
        switch (getMethodEnum()) {
            case IDENTICAL:
                return new IdenticalCondition();
            case BEFORE:
            case AFTER:
                return new DateCondition(getPattern(), getMethodEnum());
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

    public ConditionMethod getMethodEnum() {
        return ConditionMethod.valueOf(this.method.toUpperCase());
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

}
