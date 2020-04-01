package org.uu.nl.embedding.util.similarity;

import org.uu.nl.embedding.util.config.Configuration;

import java.time.temporal.ChronoUnit;

public class DateDays extends Date {

    public DateDays(String pattern, double smooth, double distance, Configuration.SimilarityGroup.Time timeEnum) {
        super(pattern, smooth, distance, timeEnum);
    }

    @Override
    protected ChronoUnit unit() {
        return ChronoUnit.DAYS;
    }
}
