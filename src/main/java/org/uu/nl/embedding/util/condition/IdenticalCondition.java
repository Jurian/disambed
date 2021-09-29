package org.uu.nl.embedding.util.condition;

public class IdenticalCondition implements LiteralCondition {

    @Override
    public boolean isValid(String s1, String s2) {
        return s1.equals(s2);
    }
}
