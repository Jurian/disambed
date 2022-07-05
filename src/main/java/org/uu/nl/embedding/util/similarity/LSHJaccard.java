package org.uu.nl.embedding.util.similarity;

import info.debatty.java.lsh.LSH;
import info.debatty.java.lsh.LSHMinHash;

public class LSHJaccard extends LSHSimilarity {

    public LSHJaccard( int shingleSize, int bands, int buckets) {
        super(shingleSize, bands, buckets);

    }

    @Override
    public LSH getLSHMethod() {
        return new LSHMinHash(this.bands, this.buckets, shingleIndex.size());
    }
}
