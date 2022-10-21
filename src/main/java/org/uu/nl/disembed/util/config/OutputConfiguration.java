package org.uu.nl.disembed.util.config;

import org.uu.nl.disembed.util.write.ClusterWriter;
import org.uu.nl.disembed.util.write.LinksetWriter;

public class OutputConfiguration  implements Configurable {

    private OutputLinkset linkset;
    private OutputClusters clusters;

    public boolean isEmpty() {
        return linkset == null && clusters == null;
    }

    public OutputLinkset getLinkset() {
        return linkset;
    }

    public void setLinkset(OutputLinkset linkset) {
        this.linkset = linkset;
    }

    public OutputClusters getClusters() {
        return clusters;
    }

    public void setClusters(OutputClusters clusters) {
        this.clusters = clusters;
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

        if(clusters != null) {
            if(clusters.filename == null || clusters.filename.isEmpty())
                throw new InvalidConfigException("Clusters filename missing or empty");
        }

        if(linkset != null) {
            if(linkset.filename == null || linkset.filename.isEmpty())
                throw new InvalidConfigException("Linkset filename missing or empty");
        }
    }

    @Override
    public CommentStringBuilder getBuilder() {
        CommentStringBuilder builder = new CommentStringBuilder();

        builder.appendLine("Output Configuration:");

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

        return builder;
    }
}
