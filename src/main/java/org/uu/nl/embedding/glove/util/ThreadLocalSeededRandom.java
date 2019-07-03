package org.uu.nl.embedding.glove.util;

import java.util.Random;

public class ThreadLocalSeededRandom extends ThreadLocal<Random> {

    private final long seed;

    public ThreadLocalSeededRandom(long seed) {
        this.seed = seed;
    }

    @Override
    protected Random initialValue() {
        return new Random(seed);
    }

    public static Random current(long seed) {
        return new ThreadLocalSeededRandom(seed).get();
    }
}
