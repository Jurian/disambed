package org.uu.nl.embedding.util.compare;

import java.util.HashMap;
import java.util.Map;

public class JaccardSimilarity implements Similarity<String> {

    public static final int DEFAULT_SHINGLE_SIZE = 3;

    private final int shingleSize;
    private final Map<String, Integer> shingleMap = new HashMap<>();
    private final Map<String, int[]> itemShingleIndex = new HashMap<>();

    public JaccardSimilarity(){this(DEFAULT_SHINGLE_SIZE);}

    public JaccardSimilarity(int shingleSize) {
        this.shingleSize = shingleSize;
    }

    public void preprocess(String item) {

        if(!itemShingleIndex.containsKey(item) && item.length() >= shingleSize) {

            final int nShingles = item.length() - shingleSize + 1;
            final int[] indexes = new int[nShingles];

            for(int i = 0; i < nShingles; i++) {
                final String shingle = item.substring(i, i + shingleSize);

                if(!shingleMap.containsKey(shingle))
                    shingleMap.put(shingle, shingleMap.size());

                indexes[i] = shingleMap.get(shingle);
            }

            itemShingleIndex.put(item, indexes);
        }
    }

    @Override
    public double calculate(String s1, String s2) {
        final int[] index1 = itemShingleIndex.get(s1);
        final int[] index2 = itemShingleIndex.get(s2);

        if(index1 == null || index2 == null) return 0;
        if(index1.length == 0 || index2.length == 0) return 0;

        int union = index1.length + index2.length;
        int intersection = 0;

        if(index1.length <= index2.length) {
            for(int i : index1)
                for(int j : index2)
                    if(i == j) {
                        intersection++;
                        union--;
                        break;
                    }
        } else {
            for(int i : index2)
                for(int j : index1)
                    if(i == j) {
                        intersection++;
                        union--;
                        break;
                    }
        }

        return intersection / (double) union;
    }
}
