package org.uu.nl.disembed.util.config;

import org.uu.nl.disembed.util.write.*;

public class OutputConfiguration  implements Configurable {

    private Linkset linkset;
    private Clusters clusters;
    private Embedding embedding;
    private HnswIndex hnsw;
    private BCA bca;

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

    public HnswIndex getHnsw() {
        return hnsw;
    }

    public void setHnsw(HnswIndex hnsw) {
        this.hnsw = hnsw;
    }

    public BCA getBca() {
        return bca;
    }

    public void setBca(BCA bca) {
        this.bca = bca;
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

    public static class BCA extends OutputFormat {}

    public static class HnswIndex extends OutputFormat {}

    public static class Linkset extends OutputFormat {}

    public static class Clusters extends OutputFormat {}

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

    @Override
    public String toString() {
        return getBuilder().toString();
    }

    public void check() throws InvalidConfigException {

        if(embedding != null) {
            if(embedding.filename == null || embedding.filename.isEmpty())
                throw new InvalidConfigException("Embedding filename missing or empty");
        }

        if(hnsw != null) {
            if(hnsw.filename == null || hnsw.filename.isEmpty())
                throw new InvalidConfigException("HNSW index filename missing or empty");
        }

        if(clusters != null) {
            if(clusters.filename == null || clusters.filename.isEmpty())
                throw new InvalidConfigException("Clusters filename missing or empty");
        }

        if(linkset != null) {
            if(linkset.filename == null || linkset.filename.isEmpty())
                throw new InvalidConfigException("Linkset filename missing or empty");
        }

        if(bca != null) {
            if(bca.filename == null || bca.filename.isEmpty())
                throw new InvalidConfigException("BCA filename missing or empty");
        }
    }

    @Override
    public CommentStringBuilder getBuilder() {
        CommentStringBuilder builder = new CommentStringBuilder();

        builder.appendLine("Output Configuration:");

        if(embedding != null) {
            builder.appendLine();
            builder.append("Writing embedding to: ");
            builder.appendNoComment(EmbeddingWriter.OUTPUT_DIRECTORY);
            builder.appendNoComment("/");
            builder.appendNoComment(embedding.getFilename());
            builder.appendLineNoComment(EmbeddingWriter.FILETYPE);
            builder.appendKeyValueLine("With writer", embedding.getWriterEnum().toString());
        }

        if(hnsw != null) {
            builder.appendLine();
            builder.append("Writing HNSW index to: ");
            builder.appendNoComment(HnswIndexWriter.OUTPUT_DIRECTORY);
            builder.appendNoComment("/");
            builder.appendNoComment(hnsw.getFilename());
            builder.appendLineNoComment(HnswIndexWriter.FILETYPE);
        }

        if(clusters != null) {
            builder.appendLine();
            builder.append("Writing clusters to: ");
            builder.appendNoComment(ClusterWriter.OUTPUT_DIRECTORY);
            builder.appendNoComment("/");
            builder.appendNoComment(clusters.getFilename());
            builder.appendLineNoComment(ClusterWriter.FILETYPE);
        }

        if(linkset != null) {
            builder.appendLine();
            builder.append("Writing linkset to: ");
            builder.appendNoComment(LinksetWriter.OUTPUT_DIRECTORY);
            builder.appendNoComment("/");
            builder.appendNoComment(linkset.getFilename());
            builder.appendLineNoComment(LinksetWriter.FILETYPE);
        }

        if(bca != null) {
            builder.appendLine();
            builder.append("Writing BCA co-occurrence matrix to: ");
            builder.appendNoComment(BCAWriter.OUTPUT_DIRECTORY);
            builder.appendNoComment("/");
            builder.appendNoComment(bca.getFilename());
            builder.appendLineNoComment(BCAWriter.FILETYPE);
        }

        return builder;
    }
}
