package org.uu.nl.embedding.util.similarity;

import org.apache.log4j.Logger;

public class Location implements LiteralSimilarity {

    private final static Logger logger = Logger.getLogger(Numeric.class);
    private final double alpha;
    private final double offset;

    public Location(double alpha, double offset) {
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

        final String[] s1Split = s1.split(", ");
        final String[] s2Split = s2.split(", ");

        if(s1Split.length != 2) {
            logger.warn("Invalid GPS coordinate: " + s1);
            return 0;
        }
        if(s2Split.length != 2) {
            logger.warn("Invalid GPS coordinate: " + s2);
            return 0;
        }

        try {
            // Calculate distance in kilometers between gps coordinates
            double distance = gps2km(
                    Double.parseDouble(s1Split[0]),
                    Double.parseDouble(s1Split[1]),
                    Double.parseDouble(s2Split[0]),
                    Double.parseDouble(s2Split[1])
            );

            return Math.pow(Math.abs(distance - offset) + 1, -alpha);
        } catch (NumberFormatException e) {
            logger.warn("Could not compare locations: " + e.getMessage());
            return 0;
        }

    }

    private double gps2km(double lat_a, double lng_a, double lat_b, double lng_b) {

        final double pk = 180/3.14169;

        final double a1 = lat_a / pk;
        final double a2 = lng_a / pk;
        final double b1 = lat_b / pk;
        final double b2 = lng_b / pk;

        final double t1 = Math.cos(a1) * Math.cos(a2) * Math.cos(b1) * Math.cos(b2);
        final double t2 = Math.cos(a1) * Math.sin(a2) * Math.cos(b1) * Math.sin(b2);
        final double t3 = Math.sin(a1) * Math.sin(b1);
        final double tt = Math.acos(t1 + t2 + t3);

        return 6366000 * tt / 1000;
    }
}
