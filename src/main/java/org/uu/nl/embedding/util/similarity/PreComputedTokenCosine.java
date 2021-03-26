package org.uu.nl.embedding.util.similarity;

import info.debatty.java.stringsimilarity.interfaces.NormalizedStringDistance;
import info.debatty.java.stringsimilarity.interfaces.NormalizedStringSimilarity;

import java.util.HashMap;
import java.util.Map;

public class PreComputedTokenCosine extends TokenCosine implements PreComputed, NormalizedStringDistance, NormalizedStringSimilarity, LiteralSimilarity  {

    private final Map<String, Map<String, Integer>> profiles;

    public PreComputedTokenCosine() {
        super();
        profiles = new HashMap<>();
    }

    @Override
    public  double distance(String s1, String s2) {
        return 1 - this.similarity(s1, s2);
    }

    @Override
    public void preCompute(String string) {
        profiles.put(string.toLowerCase(), this.getProfile(string.toLowerCase()));
    }

    @Override
    public double similarity(String s1, String s2) {
        if (s1 == null) {
            throw new NullPointerException("s1 must not be null");
        } else if (s2 == null) {
            throw new NullPointerException("s2 must not be null");
        } else if (s1.equals(s2)) {
            return 1;
        } else {
            return similarity(this.profiles.get(s1.toLowerCase()), this.profiles.get(s2.toLowerCase()));
        }
    }

    @Override
    public double similarity(Map<String, Integer> profile1, Map<String, Integer> profile2) {
        return dotProduct(profile1, profile2) / (norm(profile1) * norm(profile2));
    }
}
