package org.uu.nl.disembed.embedding.similarity.lsh;

import info.debatty.java.lsh.LSH;

public class LSHByteCharJaccard extends LSHByteChar {

    public LSHByteCharJaccard(int bands, int buckets) {
        super(bands, buckets);
    }

    @Override
    public LSH getLSHMethod() {
        return new LSHMinHash(SIGNATURE_SIZE, bands, buckets, DIMENSIONS);
    }
}
