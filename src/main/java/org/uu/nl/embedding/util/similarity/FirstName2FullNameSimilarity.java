package org.uu.nl.embedding.util.similarity;

import info.debatty.java.stringsimilarity.interfaces.NormalizedStringSimilarity;

public class FirstName2FullNameSimilarity extends NameSimilarity {


    public FirstName2FullNameSimilarity(NormalizedStringSimilarity method) {
        super(method);
    }

    protected String normalizeFullName(String s) {
        // Take only first name
        return super.normalize(s).split(" ", 2)[0];
    }

    @Override
    public double similarity(String s1, String s2) {
        if (s1 == null) {
            throw new NullPointerException("s1 must not be null");
        } else if (s2 == null) {
            throw new NullPointerException("s2 must not be null");
        }

        if(s1.isEmpty() || s2.isEmpty()) return 0;
        if(s1.equals(s2)) return 1;

        return method.similarity(normalize(s1), normalizeFullName(s2));
    }
}
