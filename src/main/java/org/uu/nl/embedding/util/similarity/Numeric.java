package org.uu.nl.embedding.util.similarity;

import org.apache.log4j.Logger;

public class Numeric implements LiteralSimilarity {

    private final static Logger logger = Logger.getLogger(Numeric.class);
    private final double alpha;
    private final double offset;

    public Numeric(double alpha, double offset) {
        this.alpha = alpha;
        this.offset = offset;
    }

    @Override
    public double similarity(String s1, String s2) {

        if (s1 == null) {
            throw new NullPointerException("s1 must not be null");
        } else if (s2 == null) {
            throw new NullPointerException("s2 must not be null");
        }

        if(s1.isEmpty() || s2.isEmpty()) return 0;
        if (s1.equals(s2)) return 1;

        final int s1hat = s1.indexOf('^');
        final int s2hat = s2.indexOf('^');

        if(s1hat != -1) s1 = s1.substring(0, s1hat);
        if(s2hat != -1) s2 = s2.substring(0, s2hat);

        try {
            final double a = Double.parseDouble(s1);
            final double b = Double.parseDouble(s2);
            return Math.pow(Math.abs(Math.abs(a - b) - offset) + 1, -alpha);
        } catch (NumberFormatException e) {
            logger.warn("Could not compare numbers: " + e.getMessage());
            return 0;
        }

    }
}
