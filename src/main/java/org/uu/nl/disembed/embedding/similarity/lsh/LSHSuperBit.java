package org.uu.nl.embedding.util.similarity.lsh;

import info.debatty.java.lsh.LSH;
import info.debatty.java.lsh.SuperBit;

import java.io.Serializable;

/**
 *
 * @author Thibault Debatty
 */
public class LSHSuperBit extends LSH implements Serializable {
    private SuperBit sb;



    /**
     * LSH implementation relying on SuperBit, to bin vectors s times (stages)
     * in b buckets (per stage), in a space with n dimensions. Input vectors
     * with a high cosine similarity have a high probability of falling in the
     * same bucket...
     *
     * Supported input types:
     * - double[]
     * - int[]
     * - others to come...
     *
     * @param stages stages
     * @param buckets buckets (per stage)
     * @param dimensions dimensionality
     */
    public LSHSuperBit(
            final int stages, final int buckets, final int dimensions) {

        super(stages, buckets);

        int code_length = stages * buckets / 2;
        int superbit = computeSuperBit(stages, buckets, dimensions);

        this.sb = new SuperBit(dimensions, superbit, code_length / superbit);
    }

    /**
     * LSH implementation relying on SuperBit, to bin vectors s times (stages)
     * in b buckets (per stage), in a space with n dimensions. Input vectors
     * with a high cosine similarity have a high probability of falling in the
     * same bucket...
     *
     * Supported input types:
     * - double[]
     * - int[]
     * - others to come...
     *
     * @param stages stages
     * @param buckets buckets (per stage)
     * @param dimensions dimensionality
     * @param seed random number generator seed. using the same value will
     * guarantee identical hashes across object instantiations
     *
     */
    public LSHSuperBit(
            final int stages,
            final int buckets,
            final int dimensions,
            final long seed) {

        super(stages, buckets);

        int code_length = stages * buckets / 2;
        int superbit = computeSuperBit(stages, buckets, dimensions);

        this.sb = new SuperBit(
                dimensions, superbit, code_length / superbit, seed);
    }

    /**
     * Compute the superbit value.
     * @param stages
     * @param buckets
     * @param dimensions
     * @return
     */
    private int computeSuperBit(
            final int stages,  final int buckets, final int dimensions) {

        // SuperBit code length
        int code_length = stages * buckets / 2;
        int superbit; // superbit value
        for (superbit = dimensions; superbit >= 1; superbit--) {
            if (code_length % superbit == 0) {
                break;
            }
        }

        if (superbit == 0) {
            throw new IllegalArgumentException(
                    "Superbit is 0 with parameters: s=" + stages
                            + " b=" + buckets + " n=" + dimensions);
        }

        return superbit;
    }

    /**
     * Empty constructor, used only for serialization.
     */
    public LSHSuperBit() {
    }

    /**
     * Hash (bin) a vector in s stages into b buckets.
     * @param vector
     * @return
     */
    public final int[] hash(final double[] vector) {
        return hashSignature(sb.signature(vector));
    }

    /**
     * Hash (bin) a vector in s stages into b buckets.
     * @param vector
     * @return
     */
    public final int[] hash(final int[] vector) {

        double[] d = new double[vector.length];
        for (int i = 0; i < vector.length; i++) {
            d[i] = vector[i];
        }
        return hash(d);
    }
}
