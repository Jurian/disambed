package org.uu.nl.embedding.util.similarity;

import info.debatty.java.stringsimilarity.interfaces.NormalizedStringSimilarity;

import java.util.Arrays;

public abstract class NameSimilarity implements LiteralSimilarity {

    private final NormalizedStringSimilarity method;

    public NameSimilarity(NormalizedStringSimilarity method) {
        this.method = method;
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

        return method.similarity(normalize(s1), normalize(s2));
    }

    private static final char[] ILLEGAL_CHARS = new char[] {
            '|',';','\'','/','"','<','>','?',':', '.', ',', '(', ')', '[', ']', '{', '}','-','_','!','@','#','$','%','^','&','*'
    };

    private String removeIllegalChars(String s) {

        char[] chars = s.toCharArray();
        int j = 0;
        for(int i = 0; i < chars.length; i++) {
            boolean removed = false;
            for(char illegal_char : ILLEGAL_CHARS) {
                if(chars[i] == illegal_char) {
                    removed = true;
                    break;
                }
            }
            if(!removed) {
                chars[j] = chars[i];
                j++;
            }
        }

        return new String(Arrays.copyOf(chars, j));
    }

    private String normalize(String s) {
        if(s.contains(",")) {
            final String[] splitName = s.split(",", 2);
            s = splitName[1].trim() + " " + splitName[0].trim();
        }
        s = removeIllegalChars(s).toLowerCase().trim();
        return s;
    }
}
