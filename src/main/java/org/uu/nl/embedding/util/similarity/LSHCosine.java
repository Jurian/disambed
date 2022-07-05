package org.uu.nl.embedding.util.similarity;

import info.debatty.java.lsh.LSH;
import info.debatty.java.lsh.LSHSuperBit;

public class LSHCosine extends LSHSimilarity {


    public LSHCosine(int shingleSize, int bands, int buckets) {
        super(shingleSize, bands, buckets);
    }

    @Override
    public LSH getLSHMethod() {
        return new LSHSuperBit(this.bands, this.buckets, shingleIndex.size());
    }
}
