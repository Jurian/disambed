package org.uu.nl.embedding.util.similarity;

import info.debatty.java.stringsimilarity.interfaces.NormalizedStringDistance;
import info.debatty.java.stringsimilarity.interfaces.NormalizedStringSimilarity;

import java.util.Map;

public class TokenCosine extends TokenBased implements NormalizedStringDistance, NormalizedStringSimilarity {

    @Override
    public double similarity(String s1, String s2) {
        if (s1 == null) {
            throw new NullPointerException("s1 must not be null");
        } else if (s2 == null) {
            throw new NullPointerException("s2 must not be null");
        } else if (s1.equals(s2)) {
            return 1;
        } else {

            final Map<String, Integer> profile1 = getProfile(s1);
            final Map<String, Integer> profile2 = getProfile(s2);

            return dotProduct(profile1, profile2) / (norm(profile1) * norm(profile2));
        }
    }

    /**
     * Returns Euclidean norm
     */
    protected double norm(Map<String, Integer> profile) {
        return Math.sqrt(profile.values().stream().mapToDouble(v -> Math.pow(v, 2)).sum());
    }

    protected double dotProduct(final Map<String, Integer> profile1, final Map<String, Integer> profile2) {
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

}
