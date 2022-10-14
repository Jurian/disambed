package org.uu.nl.disembed.embedding.similarity.lsh;

import info.debatty.java.lsh.LSH;

public class LSHNgramJaccard extends LSHNgram {

    public LSHNgramJaccard(int shingleSize, int bands, int buckets) {
        super(shingleSize, bands, buckets);
    }

    @Override
    public LSH getLSHMethod() {
        return new LSHMinHash(bands, buckets, shingleIndex.size());
    }
}
