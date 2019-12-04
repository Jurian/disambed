package org.uu.nl.embedding.util.rnd;

import org.uu.nl.embedding.Settings;

public class Permutation {

    private static final Settings settings = Settings.getInstance();
    private static final ExtendedRandom random = settings.getThreadLocalRandom();

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
