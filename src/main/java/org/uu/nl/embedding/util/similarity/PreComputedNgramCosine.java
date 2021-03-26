package org.uu.nl.embedding.util.similarity;

import info.debatty.java.stringsimilarity.ShingleBased;
import info.debatty.java.stringsimilarity.interfaces.NormalizedStringDistance;
import info.debatty.java.stringsimilarity.interfaces.NormalizedStringSimilarity;

import java.util.HashMap;
import java.util.Map;

public class PreComputedNgramCosine extends ShingleBased implements PreComputed, NormalizedStringDistance, NormalizedStringSimilarity, LiteralSimilarity {

    private final Map<String, Map<String, Integer>> profiles;

    public PreComputedNgramCosine(int k) {
        super(k);
        profiles = new HashMap<>();
    }


    public double similarity(String s1, String s2) {
        if (s1 == null) {
            throw new NullPointerException("s1 must not be null");
        } else if (s2 == null) {
            throw new NullPointerException("s2 must not be null");
        } else if (s1.equals(s2)) {
            return 1;
        } else if (s1.length() >= this.getK() && s2.length() >= this.getK()) {
            return similarity(this.profiles.get(s1), this.profiles.get(s2));
        } else {
            return 0;
        }
    }

    /**
     * Returns Euclidean norm
     */
    private double norm(Map<String, Integer> profile) {
        return Math.sqrt(profile.values().stream().mapToDouble(v -> Math.pow(v, 2)).sum());
    }

    private double dotProduct(final Map<String, Integer> profile1, final Map<String, Integer> profile2) {
        if(profile1.size() > profile2.size()) {
            return profile2.entrySet().stream().mapToInt(e -> profile1.getOrDefault(e.getKey(), 0) * e.getValue()).sum();
        } else {
            return profile1.entrySet().stream().mapToInt(e -> profile2.getOrDefault(e.getKey(), 0) * e.getValue()).sum();
        }
    }

    @Override
    public double distance(String s1, String s2) {
        return 1 - this.similarity(s1, s2);
    }

    @Override
    public double similarity(Map<String, Integer> profile1, Map<String, Integer> profile2) {
        return dotProduct(profile1, profile2) / (norm(profile1) * norm(profile2));
    }

    @Override
    public void preCompute(String string) {
        profiles.put(string, this.getProfile(string.toLowerCase()));
    }
}
