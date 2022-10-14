package org.uu.nl.embedding.util.rnd;

import org.uu.nl.embedding.util.config.EmbeddingConfiguration;

public class Permutation {

    private static final ExtendedRandom random = EmbeddingConfiguration.getThreadLocalRandom();

    private final int[] permutation;

    public Permutation(int size) {
        permutation = new int[size];
        for(int i = 0; i < permutation.length; i++)
            permutation[i] = i;
    }

    public int randomAccess(int i) {
        return permutation[i];
    }

    public void shuffle() {
        random.shuffle(permutation);
    }
}
