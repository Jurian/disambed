package org.uu.nl.embedding.util.similarity.lsh;

import info.debatty.java.lsh.LSH;

public class LSHByteCharCosine extends LSHByteChar {

    public LSHByteCharCosine(int bands, int buckets) {
        super(bands, buckets);
    }

    @Override
    public LSH getLSHMethod() {
        return new LSHSuperBit(bands, buckets, DIMENSIONS);
    }
}
