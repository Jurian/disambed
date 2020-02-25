package org.uu.nl.embedding.util.compare;

public class NGramSimilarity extends JaccardSimilarity  {

    public static final int DEFAULT_NGRAM_SIZE = 3;

    private final int ngramSize;

    public NGramSimilarity(){this(DEFAULT_NGRAM_SIZE);}

    public NGramSimilarity(int ngramSize) {
        this.ngramSize = ngramSize;
    }

    protected void preProcessNormalized(String item) {

        if(item.isEmpty()) return;

        if(!tokenIndex.containsKey(item) && item.length() >= ngramSize) {

            final int ngramCount = item.length() - ngramSize + 1;
            final int[] indexes = new int[ngramCount];

            for(int i = 0; i < ngramCount; i++) {
                final String ngram = item.substring(i, i + ngramSize);

                if(!tokenMap.containsKey(ngram))
                    tokenMap.put(ngram, tokenMap.size());

                indexes[i] = tokenMap.get(ngram);
            }

            tokenIndex.put(item, indexes);
        }
    }
}
