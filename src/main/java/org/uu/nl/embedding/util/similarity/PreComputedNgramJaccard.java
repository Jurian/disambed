package org.uu.nl.embedding.util.similarity;

import info.debatty.java.stringsimilarity.ShingleBased;
import info.debatty.java.stringsimilarity.interfaces.MetricStringDistance;
import info.debatty.java.stringsimilarity.interfaces.NormalizedStringDistance;
import info.debatty.java.stringsimilarity.interfaces.NormalizedStringSimilarity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PreComputedNgramJaccard extends ShingleBased implements PreComputed, MetricStringDistance, NormalizedStringDistance, NormalizedStringSimilarity, LiteralSimilarity {

    private final Map<String, Map<String, Integer>> profiles;

    public PreComputedNgramJaccard(int k) {
        super(k);
        profiles = new HashMap<>();
    }

    @Override
    public double distance(String s1, String s2) {
        return 1 - this.similarity(s1, s2);
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
            return similarity(this.profiles.get(s1), this.profiles.get(s2));
        }
    }

    @Override
    public double similarity(Map<String, Integer> profile1, Map<String, Integer> profile2) {
        final Set<String> union = new HashSet<>();
        union.addAll(profile1.keySet());
        union.addAll(profile2.keySet());
        final int inter = profile1.size() + profile2.size() - union.size();
        return inter / (double)union.size();
    }

    @Override
    public void preCompute(String string) {
        profiles.put(string, this.getProfile(string.toLowerCase()));
    }
}
