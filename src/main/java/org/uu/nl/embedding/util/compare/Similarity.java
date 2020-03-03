package org.uu.nl.embedding.util.compare;

public interface Similarity<T> {
    boolean needsPreproces();
    void preProcess(T a);
    double calculate(T a, T b);
}
