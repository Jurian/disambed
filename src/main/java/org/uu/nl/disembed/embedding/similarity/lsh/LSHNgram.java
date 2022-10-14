package org.uu.nl.disembed.embedding.similarity.lsh;

import org.uu.nl.disembed.util.sparse.BMatrixSparseCSR;

import java.util.HashMap;
import java.util.Map;

public abstract class LSHNgram extends LSHSimilarity {

    protected final int shingleSize;
    protected Map<String, Integer> shingleIndex;
    protected BMatrixSparseCSR shingleAllocation;

    public LSHNgram(int shingleSize, int bands, int buckets) {
        super(bands, buckets);
        this.shingleSize = shingleSize;
        this.shingleIndex = new HashMap<>();
        this.shingleAllocation = new BMatrixSparseCSR();
        this.shingleAllocation.setGrowthFactor(2);
    }

    @Override
    public void preCompute(String token, int index) {

        token = normalize(token);
        int nShingles = (token.length() - shingleSize + 1);
        if(nShingles < 1) {
            shingleAllocation.setRowIndexes(index, new int[0]);
        } else {
            int[] shingles = new int[nShingles];

            for (int i = 0; i < nShingles; i++) {
                String shingle = token.substring(i, i + shingleSize);

                shingleIndex.putIfAbsent(shingle, shingleIndex.size());
                shingles[i] = shingleIndex.get(shingle);
            }

            shingleAllocation.setRowIndexes(index, shingles);
        }
    }

    @Override
    public void postCompute() {
        this.lsh = getLSHMethod();
        for(int i = 0; i < shingleAllocation.nRows(); i++) {
            // For each band, which bucket does this shingle allocation fall in?

            int[] hash = lsh.hashSignature(shingleAllocation.getRow(i));
            int[] bucketIndexes = new int[bands];
            for(int j = 0; j < bucketIndexes.length; j++) {
                bucketIndexes[j] = (j * buckets) + hash[j];
            }
            bucketAllocation.setRowIndexes(i, bucketIndexes);
        }
        // We need very fast lookup of columns later on, but row retrieval is much faster
        // So also store the transpose
        bucketAllocationT = bucketAllocation.transpose();
        this.shingleAllocation = null;
        this.shingleIndex = null;
    }
}
