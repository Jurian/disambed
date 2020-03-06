package org.uu.nl.embedding.util.similarity;

import java.util.Map;

public interface PreComputed {
    void preCompute(String string);
    double similarity(Map<String, Integer> profile1, Map<String, Integer> profile2);
}
