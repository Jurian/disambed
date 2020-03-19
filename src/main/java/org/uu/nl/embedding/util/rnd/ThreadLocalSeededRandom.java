package org.uu.nl.embedding.util.rnd;


public class ThreadLocalSeededRandom extends ThreadLocal<ExtendedRandom> {

    private final long seed;

    public ThreadLocalSeededRandom(long seed) {
        this.seed = seed;
    }

    @Override
    protected ExtendedRandom initialValue() {
        return new ExtendedRandom(seed);
    }

    public static ExtendedRandom current(long seed) {
        return new ThreadLocalSeededRandom(seed).get();
    }

}
