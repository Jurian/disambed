package org.uu.nl.disembed.util.config;

import org.uu.nl.disembed.util.write.BCAWriter;
import org.uu.nl.disembed.util.write.EmbeddingWriter;
import org.uu.nl.disembed.util.write.HnswIndexWriter;

public class IntermediateOutputConfiguration implements Configurable {

    private OutputEmbedding embedding;
    private OutputHnswIndex hnsw;
    private OutputBCA bca;

    public boolean isEmpty() {
        return embedding == null  && hnsw == null && bca == null;
    }

    public OutputEmbedding getEmbedding() {
        return embedding;
    }

    public void setEmbedding(OutputEmbedding embedding) {
        this.embedding = embedding;
    }

    public OutputHnswIndex getHnsw() {
        return hnsw;
    }

    public void setHnsw(OutputHnswIndex hnsw) {
        this.hnsw = hnsw;
    }

    public OutputBCA getBca() {
        return bca;
    }

    public void setBca(OutputBCA bca) {
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

    public static class OutputBCA extends OutputFormat {}

    public static class OutputHnswIndex extends OutputFormat {}

    public static class OutputLinkset extends OutputFormat {}

    public static class OutputClusters extends OutputFormat {}

    public static class OutputEmbedding extends OutputFormat {

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

        if(bca != null) {
            if(bca.filename == null || bca.filename.isEmpty())
                throw new InvalidConfigException("BCA filename missing or empty");
        }
    }

    @Override
    public CommentStringBuilder getBuilder() {
        CommentStringBuilder builder = new CommentStringBuilder();

        builder.appendLine("Output Configuration:");

        if(bca != null) {
            builder.appendLine();
            builder.append("Writing BCA co-occurrence matrix to: ");
            builder.appendNoComment(BCAWriter.OUTPUT_DIRECTORY);
            builder.appendNoComment("/");
            builder.appendNoComment(bca.getFilename());
            builder.appendLineNoComment(BCAWriter.FILETYPE);
        }

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



        return builder;
    }
}
