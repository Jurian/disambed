package org.uu.nl.embedding.util.compare;

public interface Similarity<T> {
    void preprocess(T a);
    double calculate(T a, T b);
}
