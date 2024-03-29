package org.uu.nl.disembed.embedding.similarity;

import org.uu.nl.disembed.util.config.EmbeddingConfiguration;

import java.time.temporal.ChronoUnit;

public class DateDays extends Date {

    public DateDays(String pattern, double smooth, double distance, EmbeddingConfiguration.SimilarityGroup.Time timeEnum) {
        super(pattern, smooth, distance, timeEnum);
    }

    @Override
    protected ChronoUnit unit() {
        return ChronoUnit.DAYS;
    }
}
