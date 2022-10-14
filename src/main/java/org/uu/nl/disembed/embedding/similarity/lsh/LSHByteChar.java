package org.uu.nl.embedding.util.similarity.lsh;

public abstract class LSHByteChar extends LSHSimilarity {

    protected static final int DIMENSIONS = 512;
    protected static final int SIGNATURE_SIZE = 128;

    public LSHByteChar(int bands, int buckets) {
        super(bands, buckets);
        this.lsh = getLSHMethod();
    }

    private boolean[] byteToBoolArr(byte b) {
        boolean[] boolArr = new boolean[Byte.SIZE];
        for (int i = 0; i < Byte.SIZE; i++) {
            boolArr[i] = (b & (byte) (SIGNATURE_SIZE / Math.pow(2, i))) != 0;
        }
        return boolArr;
    }

    private boolean[] fromString(String token) {
        if(token.length() / Byte.SIZE > DIMENSIONS) {
            throw new IllegalArgumentException(
                    "Input string too large to be encoded. " +
                    "Would need at least " + (token.length()*Byte.SIZE) + " dimensions.");
        }
        byte[] bytes = token.getBytes();
        boolean[] result = new boolean[DIMENSIONS];
        for (int c_i = 0, b_i = 0; c_i < bytes.length; c_i++, b_i += Byte.SIZE) {
            System.arraycopy(byteToBoolArr(bytes[c_i]), 0, result, b_i, Byte.SIZE);
        }
        return result;
    }

    @Override
    public void postCompute() {
        this.bucketAllocationT = bucketAllocation.transpose();
    }

    @Override
    public void preCompute(String token, int index) {

        boolean[] signature = fromString(normalize(token));
        int[] hash = lsh.hashSignature(signature);
        int[] bucketIndexes = new int[bands];
        for(int j = 0; j < bucketIndexes.length; j++) {
            bucketIndexes[j] = (j * buckets) + hash[j];}


        bucketAllocation.setRowIndexes(index, bucketIndexes);
    }
}
