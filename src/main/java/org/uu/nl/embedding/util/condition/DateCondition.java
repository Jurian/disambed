package org.uu.nl.embedding.util.condition;

import org.apache.log4j.Logger;
import org.uu.nl.embedding.util.config.Condition;
import org.uu.nl.embedding.util.similarity.Date;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateCondition implements LiteralCondition {

    private final static Logger logger = Logger.getLogger(DateCondition.class);
    private final DateTimeFormatter format;
    private final Condition.ConditionMethod condition;

    public DateCondition(String pattern, Condition.ConditionMethod condition) {
        this.format = pattern.equals("iso") ? DateTimeFormatter.BASIC_ISO_DATE : DateTimeFormatter.ofPattern(pattern);
        this.condition = condition;
    }

    @Override
    public boolean isValid(String s1, String s2) {
        if (s1 == null) {
            throw new NullPointerException("s1 must not be null");
        } else if (s2 == null) {
            throw new NullPointerException("s2 must not be null");
        }

        if(s1.isEmpty() || s2.isEmpty()) return false;
        if(s1.equals(s2)) return true;

        try {

            final LocalDate d1 = Date.parse(s1, format);
            final LocalDate d2 = Date.parse(s2, format);

            switch (condition) {
                case BEFORE:
                    return d1.isBefore(d2);
                case AFTER:
                    return d1.isAfter(d2);
                case IDENTICAL:
                    logger.warn("Identical date not supported");
                    return false;
                default:
                    throw new IllegalArgumentException("Invalid condition");
            }

        } catch (DateTimeParseException e) {
            logger.warn("Could not compare dates: " + e.getMessage());
            return false;
        }
    }
}
