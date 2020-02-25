package org.uu.nl.embedding.util.compare;


import java.util.HashMap;
import java.util.Map;

public abstract class JaccardSimilarity implements Similarity<String> {

    protected final Map<String, Integer> tokenMap = new HashMap<>();
    protected final Map<String, int[]> tokenIndex = new HashMap<>();

    private String normalize(String item) {
        return item.toLowerCase().trim();
    }

    @Override
    public void preProcess(String s) {
        preProcessNormalized(normalize(s));
    }

    protected abstract void preProcessNormalized(String s);

    @Override
    public double calculate(String s1, String s2) {

        s1 = normalize(s1);
        s2 = normalize(s2);

        final int[] index1 = tokenIndex.get(s1);
        final int[] index2 = tokenIndex.get(s2);

        if(index1 == null || index2 == null) return 0;
        if(index1.length == 0 || index2.length == 0) return 0;

        int intersection = 0;

        for(int i : index1) for(int j : index2)
            if(i == j) intersection++;

        return intersection / (double) (index1.length + index2.length - intersection);
    }

}
