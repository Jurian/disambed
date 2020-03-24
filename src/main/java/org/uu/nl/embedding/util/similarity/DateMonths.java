package org.uu.nl.embedding.util.similarity;

import java.time.temporal.ChronoUnit;

public class DateMonths extends Date {

    public DateMonths(String pattern, double alpha) {
        super(pattern, alpha);
    }

    @Override
    protected ChronoUnit unit() {
        return ChronoUnit.MONTHS;
    }
}
