package org.uu.nl.embedding.util.similarity;

import java.time.temporal.ChronoUnit;

public class DateDays extends Date {

    public DateDays(String pattern, double alpha) {
        super(pattern, alpha);
    }

    @Override
    protected ChronoUnit unit() {
        return ChronoUnit.DAYS;
    }
}
