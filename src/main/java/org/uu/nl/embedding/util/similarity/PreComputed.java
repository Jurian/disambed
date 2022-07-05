package org.uu.nl.embedding.util.similarity;

import java.util.Map;

public interface PreComputed<T> {
    void preCompute(String string);
    double similarity(T profile1, T profile2);
}
