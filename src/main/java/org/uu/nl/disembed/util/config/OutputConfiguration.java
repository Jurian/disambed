package org.uu.nl.embedding.util.config;

public class OutputConfiguration {

    private Linkset linkset;
    private Clusters clusters;
    private Embedding embedding;

    public Linkset getLinkset() {
        return linkset;
    }

    public void setLinkset(Linkset linkset) {
        this.linkset = linkset;
    }

    public Clusters getClusters() {
        return clusters;
    }

    public void setClusters(Clusters clusters) {
        this.clusters = clusters;
    }

    public Embedding getEmbedding() {
        return embedding;
    }

    public void setEmbedding(Embedding embedding) {
        this.embedding = embedding;
    }

    public static abstract class OutputFormat {
        public String filename;

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }
    }

    public static class Linkset extends OutputFormat { }

    public static class Clusters extends OutputFormat { }

    public static class Embedding extends OutputFormat {

        public enum EmbeddingWriter {
            GLOVE, WORD2VEC
        }

        private String writer;

        public void setWriter(String writer) {
            this.writer = writer;
        }
        public String getWriter() {return this.writer;}

        public EmbeddingWriter getWriterEnum() {
            return EmbeddingWriter.valueOf(writer.toUpperCase());
        }
    }

    public void check() throws InvalidConfigException {

    }
}
