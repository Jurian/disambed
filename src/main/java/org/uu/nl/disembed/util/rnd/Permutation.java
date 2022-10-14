package org.uu.nl.disembed.util.rnd;

import org.uu.nl.disembed.util.config.Configuration;

public class Permutation {

    private static final ExtendedRandom random = Configuration.getThreadLocalRandom();

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
