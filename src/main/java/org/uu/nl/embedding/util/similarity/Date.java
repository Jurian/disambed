package org.uu.nl.embedding.util.similarity;

import org.apache.log4j.Logger;
import org.uu.nl.embedding.util.config.Configuration;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

public abstract class Date implements LiteralSimilarity {

    private final static Logger logger = Logger.getLogger(Date.class);
    private final Configuration.SimilarityGroup.Time timeDirection;
    private final double alpha;
    private final double offset;
    private final DateTimeFormatter format;

    public Date(String pattern, double alpha, double offset, Configuration.SimilarityGroup.Time timeEnum) {
        this.alpha = alpha;
        this.offset = offset;
        this.timeDirection = timeEnum;
        this.format = pattern.equals("iso") ? DateTimeFormatter.BASIC_ISO_DATE : DateTimeFormatter.ofPattern(pattern);
    }

    protected abstract ChronoUnit unit();

    @Override
    public double similarity(String s1, String s2) {

        if (s1 == null) {
            throw new NullPointerException("s1 must not be null");
        } else if (s2 == null) {
            throw new NullPointerException("s2 must not be null");
        }

        if(s1.isEmpty() || s2.isEmpty()) return 0;
        if(s1.equals(s2)) return 1;

        try {

            final int s1hat = s1.indexOf('^');
            final int s2hat = s2.indexOf('^');

            if(s1hat != -1) s1 = s1.substring(0, s1hat);
            if(s2hat != -1) s2 = s2.substring(0, s2hat);

            final LocalDate d1 = LocalDate.parse(s1, format);
            final LocalDate d2 = LocalDate.parse(s2, format);

            switch (timeDirection) {
                case BACKWARDS:
                    if(d1.isBefore(d2)) return 0;
                    break;
                case FORWARDS:
                    if(d1.isAfter(d2)) return 0;
                    break;
            }
            long diff = Math.abs(unit().between(d1, d2));
            return Math.pow(Math.abs(diff - offset) + 1, -alpha);
        } catch (DateTimeParseException e) {
            logger.warn("Could not compare dates: " + e.getMessage());
            return 0;
        }
    }
}
