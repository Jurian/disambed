package org.uu.nl.embedding.util.similarity;

import info.debatty.java.stringsimilarity.interfaces.StringSimilarity;

public class Numeric implements StringSimilarity {

    private final double alpha;

    public Numeric(double alpha) {
        this.alpha = alpha;
    }

    @Override
    public double similarity(String s1, String s2) {
        final int a = Integer.parseInt(s1);
        final int b = Integer.parseInt(s2);

        return 1 / Math.pow((Math.abs(a - b) + 1d), alpha);
    }
}
