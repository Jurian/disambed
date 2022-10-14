package org.uu.nl.disembed.util.config;

import org.uu.nl.disembed.util.rnd.ExtendedRandom;
import org.uu.nl.disembed.util.rnd.ThreadLocalSeededRandom;

public class Configuration implements Configurable {

    private static ThreadLocalSeededRandom threadLocalRandom;

    public static void setThreadLocalRandom() {
        threadLocalRandom = new ThreadLocalSeededRandom(System.currentTimeMillis());
    }

    public static void setThreadLocalRandom(long seed) {
        threadLocalRandom = new ThreadLocalSeededRandom(seed);
    }

    public static ExtendedRandom getThreadLocalRandom() {
        return threadLocalRandom.get();
    }

    private int threads;

    public int getThreads() {
        return threads == 0 ? (Runtime.getRuntime().availableProcessors() -1) : threads;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }

    EmbeddingConfiguration embedding;
    ClusterConfiguration clustering;
    OutputConfiguration output;

    public EmbeddingConfiguration getEmbedding() {
        return embedding;
    }

    public void setEmbedding(EmbeddingConfiguration embedding) {
        this.embedding = embedding;
    }

    public ClusterConfiguration getClustering() {
        return clustering;
    }

    public void setClustering(ClusterConfiguration clustering) {
        this.clustering = clustering;
    }

    public OutputConfiguration getOutput() {
        return output;
    }

    public void setOutput(OutputConfiguration output) {
        this.output = output;
    }

    @Override
    public void check() throws InvalidConfigException {

        if(embedding != null) embedding.check();
        if(clustering != null) clustering.check();
        if(output != null) output.check();
    }

    @Override
    public CommentStringBuilder getBuilder() {
        CommentStringBuilder builder = new CommentStringBuilder();

        builder.appendKeyValueLine("Nr of threads", getThreads());

        if(embedding != null)
            builder.appendLine(embedding.getBuilder());
        else
            builder.appendLine("# No embedding configuration specified");
        if(clustering != null)
            builder.appendLine(clustering.getBuilder());
        else
            builder.appendLine("# No clustering configuration specified");
        if(output != null)
            builder.appendLine(output.getBuilder());
        else
            builder.appendLine("# No output configuration specified");

        return builder;
    }


    @Override
    public String toString() {
        return getBuilder().toString();
    }
}
