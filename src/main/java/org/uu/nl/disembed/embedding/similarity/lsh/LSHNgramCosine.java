package org.uu.nl.disembed.embedding.similarity.lsh;

import info.debatty.java.lsh.LSH;

public class LSHNgramCosine extends LSHNgram {

    public LSHNgramCosine(int shingleSize, int bands, int buckets) {
        super(shingleSize, bands, buckets);
    }

    @Override
    public LSH getLSHMethod() {
        return new LSHSuperBit(bands, buckets, shingleIndex.size());
    }
}
