package org.uu.nl.embedding.util.similarity;

import info.debatty.java.stringsimilarity.interfaces.MetricStringDistance;
import info.debatty.java.stringsimilarity.interfaces.NormalizedStringDistance;
import info.debatty.java.stringsimilarity.interfaces.NormalizedStringSimilarity;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TokenJaccard extends TokenBased implements MetricStringDistance, NormalizedStringDistance, NormalizedStringSimilarity {

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
            final Set<String> union = new HashSet<>();

            union.addAll(profile1.keySet());
            union.addAll(profile2.keySet());
            int inter = profile1.keySet().size() + profile2.keySet().size() - union.size();

            return  inter / (double)union.size();
        }
    }

    @Override
    public double distance(String s1, String s2) {
        return 1 - this.similarity(s1, s2);
    }

}
