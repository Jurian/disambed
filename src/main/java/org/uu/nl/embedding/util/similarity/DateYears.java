package org.uu.nl.embedding.util.similarity;

import java.time.temporal.ChronoUnit;

public class DateYears extends Date {

    public DateYears(String pattern, double alpha) {
        super(pattern, alpha);
    }

    @Override
    protected ChronoUnit unit() {
        return ChronoUnit.YEARS;
    }
}
