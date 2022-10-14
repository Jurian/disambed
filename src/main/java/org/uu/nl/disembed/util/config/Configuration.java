package org.uu.nl.embedding.util.config;

public class Configuration {

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

    public void check() throws InvalidConfigException {

        if(embedding != null) embedding.check();
        if(clustering != null) clustering.check();
        if(output != null) output.check();
    }
}
