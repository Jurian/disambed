package org.uu.nl.embedding.util.similarity.lsh;

import info.debatty.java.lsh.LSH;
import info.debatty.java.lsh.MinHash;

/**
 *
 * @author Thibault Debatty
 */
public class LSHMinHash extends LSH {
    private final MinHash mh;
    private static final double THRESHOLD = 0.5;

    /**
     * Instantiates a LSH instance that internally uses MinHash,
     * with s stages (or bands) and b buckets (per stage), for sets out of a
     * dictionary of n elements.
     *
     * Attention: the number of buckets should be chosen such that we have at
     * least 100 items per bucket.
     *
     * @param s stages
     * @param b buckets (per stage)
     * @param n dictionary size
     * @param signature_size signature size
     */
    public LSHMinHash(final int s, final int b, final int n, final int signature_size) {
        super(s, b);
        this.mh = new MinHash(signature_size, n);
    }

    /**
     * Instantiates a LSH instance that internally uses MinHash,
     * with s stages (or bands) and b buckets (per stage), for sets out of a
     * dictionary of n elements.
     *
     * Attention: the number of buckets should be chosen such that we have at
     * least 100 items per bucket.
     *
     * @param s stages
     * @param b buckets (per stage)
     * @param n dictionary size
     */
    public LSHMinHash(final int s, final int b, final int n) {
        super(s, b);
        int signature_size = computeSignatureSize(s, n);
        this.mh = new MinHash(signature_size, n);
    }

    /**
     * Instantiates a LSH instance that internally uses MinHash,
     * with s stages (or bands) and b buckets (per stage), for sets out of a
     * dictionary of n elements.
     *
     * Attention: the number of buckets should be chosen such that we have at
     * least 100 items per bucket.
     *
     * @param s stages
     * @param b buckets (per stage)
     * @param n dictionary size
     * @param seed random number generator seed. using the same value will
     * guarantee identical hashes across object instantiations
     */
    public LSHMinHash(final int s, final int b, final int n, final long seed) {
        super(s, b);
        int signature_size = computeSignatureSize(s, n);
        this.mh = new MinHash(signature_size, n, seed);
    }

    /**
     * Compute the size of the signature according to "Mining of Massive
     * Datasets" p88.
     * It can be shown that, using MinHash, the probability that the
     * signatures of 2 sets with Jaccard similarity s agree in all the
     * rows of at least one stage (band), and therefore become a candidate
     * pair, is 1−(1−s^R)^b
     * where R = signature_size / b (number of rows in a stage/band)
     * Thus, the curve that shows the probability that 2 items fall in the
     * same bucket for at least one of the stages, as a function of their
     * Jaccard index similarity, has a S shape.
     * The threshold (the value of similarity at which the probability of
     * becoming a candidate is 1/2) is a function of the number of stages
     * (s, or bands b in the book) and the signature size:
     * threshold ≃ (1/s)^(1/R)
     * Hence the signature size can be computed as:
     * R = ln(1/s) / ln(threshold)
     * signature_size = R * b
     */
    private int computeSignatureSize(final int s, final int n) {

        int r = (int) Math.ceil(Math.log(1.0 / s) / Math.log(THRESHOLD)) + 1;
        return r * s;
    }

    /**
     * Bin this vector to corresponding buckets.
     * @param vector
     * @return
     */
    public final int[] hash(final boolean[] vector) {
        return hashSignature(this.mh.signature(vector));
    }

    /**
     * Get the coefficients used by internal hashing functions.
     * @return
     */
    public final long[][] getCoefficients() {
        return mh.getCoefficients();
    }
}
