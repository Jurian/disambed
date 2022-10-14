package org.uu.nl.embedding.util.similarity.lsh;

import info.debatty.java.lsh.LSH;
import info.debatty.java.stringsimilarity.interfaces.NormalizedStringDistance;
import info.debatty.java.stringsimilarity.interfaces.NormalizedStringSimilarity;
import org.uu.nl.embedding.util.similarity.NameSimilarity;
import org.uu.nl.embedding.util.similarity.PostComputed;
import org.uu.nl.embedding.util.similarity.PreComputed;
import org.uu.nl.embedding.util.sparse.BMatrixSparseCSR;

import java.util.Arrays;

public abstract class LSHSimilarity extends NameSimilarity implements PreComputed<int[]>, PostComputed, NormalizedStringDistance, NormalizedStringSimilarity {

    protected final BMatrixSparseCSR bucketAllocation;
    protected BMatrixSparseCSR bucketAllocationT;
    protected LSH lsh;
    protected final int bands, buckets;

    public LSHSimilarity(int bands, int buckets) {
        super(new info.debatty.java.stringsimilarity.NormalizedLevenshtein());
        this.bands = bands;
        this.buckets = buckets;
        this.bucketAllocation = new BMatrixSparseCSR(0, bands * buckets);
    }

    public abstract LSH getLSHMethod();

    public int[] candidates(int token, int[] targetNodes) {

        int[] columns = bucketAllocation.getRowIndexes(token);
        boolean[] b = new boolean[bucketAllocation.nRows()];
        int[] candidates = new int[bucketAllocation.nRows()];

        int i = 0;
        for (int column : columns) {

            int[] row = bucketAllocationT.getRowIndexes(column);
            if(row.length > 1) {

                for (int j : row) {
                    if(!b[j] && j != token) {
                        b[j] = true;
                        candidates[i] = targetNodes[j];
                        i++;
                    }
                }
            }
        }
        return  Arrays.copyOf(candidates, i);
    }

    public abstract void preCompute(String token, int index);

    @Override
    public double similarity(int[] profile1, int[] profile2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public  double distance(String s1, String s2) {
        return 1 - this.similarity(s1, s2);
    }

}
