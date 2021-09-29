package org.uu.nl.embedding.util.config;

import java.util.Map;
import java.util.Set;

public class PredicateWeights {

    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Configuration.PredicateWeightingMethod getTypeEnum() {
        if (this.type == null) return Configuration.PredicateWeightingMethod.NONE;
        return Configuration.PredicateWeightingMethod.valueOf(this.type.toUpperCase());
    }

    private Set<String> filter;

    public Set<String> getFilter() {
        return this.filter;
    }

    public void setFilter(Set<String> filter) {
        this.filter = filter;
    }

    private Map<String, Float> weights;

    public boolean usingManualWeights() {
        return getTypeEnum() == Configuration.PredicateWeightingMethod.MANUAL;
    }

    public Map<String, Float> getWeights() {
        return weights;
    }

    public void setWeights(Map<String, Float> weights) {
        this.weights = weights;
    }

    public boolean usingPageRankWeights() {
        return getTypeEnum() == Configuration.PredicateWeightingMethod.PAGERANK;
    }

    public boolean usingNoWeights() {
        return getTypeEnum() == Configuration.PredicateWeightingMethod.NONE;
    }
}
