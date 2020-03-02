package org.uu.nl.embedding.util.compare;

public class NumericSimilarity implements Similarity<String> {

    private final double alpha;

    public NumericSimilarity(double alpha) {
        this.alpha = alpha;
    }

    @Override
    public void preProcess(String a) { }

    @Override
    public double calculate(String s1, String s2) {
        final int a = Integer.parseInt(s1);
        final int b = Integer.parseInt(s2);

        return 1 / Math.pow((Math.abs(a - b) + 1d), alpha);
    }
}
