package org.uu.nl.embedding.util.similarity;

import info.debatty.java.lsh.LSH;
import info.debatty.java.stringsimilarity.interfaces.NormalizedStringDistance;
import info.debatty.java.stringsimilarity.interfaces.NormalizedStringSimilarity;
import org.uu.nl.embedding.util.sparse.BMatrixSparseCSR;

import java.util.*;

public abstract class LSHSimilarity extends NameSimilarity implements PreComputed<int[]>, PostComputed, NormalizedStringDistance, NormalizedStringSimilarity {

    protected final BMatrixSparseCSR bucketAllocation;
    protected BMatrixSparseCSR shingleAllocation;
    protected Map<String, Integer> shingleIndex;
    protected final Map<String, Integer> tokenToIndex;
    protected final Map<Integer, String> indexToToken;
    protected final int shingleSize, bands, buckets;

    public LSHSimilarity(int shingleSize, int bands, int buckets) {
        super(new info.debatty.java.stringsimilarity.NormalizedLevenshtein());

        this.bands = bands;
        this.buckets = buckets;
        this.shingleSize = shingleSize;
        this.tokenToIndex = new HashMap<>();
        this.indexToToken = new HashMap<>();
        this.shingleIndex = new HashMap<>();
        this.bucketAllocation = new BMatrixSparseCSR((int) 1e4, bands * buckets);
        this.shingleAllocation = new BMatrixSparseCSR();
        this.shingleAllocation.setGrowthFactor(2);
    }

    public abstract LSH getLSHMethod();

    @Override
    public void postCompute() {
        LSH lsh = getLSHMethod();
        for(int i = 0; i < shingleAllocation.nRows(); i++) {
            // For each band, which bucket does this shingle allocation fall in?
            int[] signature = lsh.hashSignature(shingleAllocation.getRowIndexes(i));
            int[] bucketIndexes = new int[bands];
            for(int j = 0; j < bucketIndexes.length; j++) {
                bucketIndexes[j] = (j * buckets) + signature[j];
            }
            bucketAllocation.setRowIndexes(i, bucketIndexes);
        }
        this.shingleAllocation = null;
        this.shingleIndex = null;
    }

    public int[] candidates(String token) {

        String normalizedToken = normalize(token);

        int[] candidates = new int[0];

        int tokenRow = tokenToIndex.get(normalizedToken);
        int[] buckets = bucketAllocation.getColumnIndexes(tokenRow);
        int i = 0;
        for (int column : buckets) {
            int[] sameBucket = bucketAllocation.getColumnIndexes(column);
            candidates = Arrays.copyOf(candidates, candidates.length + sameBucket.length - 1);
            for (int candidateRow : sameBucket) {
                if(candidateRow == tokenRow) continue;
                candidates[i] = candidateRow;
                i++;
            }
        }
        return candidates;
    }

    @Override
    public void preCompute(String token) {

        String normalizedToken = normalize(token);

        tokenToIndex.putIfAbsent(normalizedToken, tokenToIndex.size());
        int tokenIndex = tokenToIndex.get(normalizedToken);
        indexToToken.putIfAbsent(tokenIndex, normalizedToken);

        int nShingles = (normalizedToken.length() - shingleSize + 1);
        int[] shingles = new int[nShingles];

        for (int i = 0; i < nShingles; i++) {
            String shingle = normalizedToken.substring(i, i + shingleSize);

            shingleIndex.putIfAbsent(shingle, shingleIndex.size());
            shingles[i] = shingleIndex.get(shingle);
        }

        shingleAllocation.setRowIndexes(tokenIndex, shingles);
    }

    @Override
    public double similarity(int[] profile1, int[] profile2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double distance(String s1, String s2) {
        throw new UnsupportedOperationException();
    }
}
