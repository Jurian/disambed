package org.uu.nl.disembed.embedding.similarity;

public interface PreComputed<T> {
    void preCompute(String string, int index);
    double similarity(T profile1, T profile2);
}
