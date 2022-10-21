package org.uu.nl.disembed.util.config;

import org.uu.nl.disembed.util.write.BCAWriter;
import org.uu.nl.disembed.util.write.EmbeddingWriter;
import org.uu.nl.disembed.util.write.HnswIndexWriter;

import java.io.File;
import java.nio.file.Paths;

public class IntermediateInputConfiguration implements Configurable {

    private InputEmbedding embedding;
    private InputHnswIndex hnsw;
    private InputBCA bca;

    public InputEmbedding getEmbedding() {
        return embedding;
    }

    public void setEmbedding(InputEmbedding embedding) {
        this.embedding = embedding;
    }

    public InputHnswIndex getHnsw() {
        return hnsw;
    }

    public void setHnsw(InputHnswIndex hnsw) {
        this.hnsw = hnsw;
    }

    public InputBCA getBca() {
        return bca;
    }

    public void setBca(InputBCA bca) {
        this.bca = bca;
    }

    public static abstract class InputFormat {
        public String filename;

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public abstract File getImportFile();
    }

    public static class InputBCA extends InputFormat {
        @Override
        public File getImportFile() {
            return Paths.get("").toAbsolutePath().resolve(BCAWriter.OUTPUT_DIRECTORY + "/" + filename + BCAWriter.FILETYPE).toFile();
        }
    }

    public static class InputHnswIndex extends InputFormat {
        @Override
        public File getImportFile() {
            return Paths.get("").toAbsolutePath().resolve(HnswIndexWriter.OUTPUT_DIRECTORY + "/" + filename + HnswIndexWriter.FILETYPE).toFile();
        }
    }

    public static class InputEmbedding extends InputFormat {
        @Override
        public File getImportFile() {
            return Paths.get("").toAbsolutePath().resolve(EmbeddingWriter.OUTPUT_DIRECTORY + "/" + filename + EmbeddingWriter.FILETYPE).toFile();
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

        builder.appendLine("Input Configuration:");

        if(embedding != null) {
            builder.appendLine();
            builder.append("Reading embedding from: ");
            builder.appendNoComment(EmbeddingWriter.OUTPUT_DIRECTORY);
            builder.appendNoComment("/");
            builder.appendNoComment(embedding.getFilename());
            builder.appendLineNoComment(EmbeddingWriter.FILETYPE);
        }

        if(hnsw != null) {
            builder.appendLine();
            builder.append("Reading HNSW index from: ");
            builder.appendNoComment(HnswIndexWriter.OUTPUT_DIRECTORY);
            builder.appendNoComment("/");
            builder.appendNoComment(hnsw.getFilename());
            builder.appendLineNoComment(HnswIndexWriter.FILETYPE);
        }

        if(bca != null) {
            builder.appendLine();
            builder.append("Reading BCA co-occurrence matrix from: ");
            builder.appendNoComment(BCAWriter.OUTPUT_DIRECTORY);
            builder.appendNoComment("/");
            builder.appendNoComment(bca.getFilename());
            builder.appendLineNoComment(BCAWriter.FILETYPE);
        }

        return builder;
    }
}
