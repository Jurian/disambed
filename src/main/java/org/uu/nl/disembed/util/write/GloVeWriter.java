package org.uu.nl.embedding.util.write;

import org.uu.nl.embedding.opt.Embedding;
import org.uu.nl.embedding.util.config.Configuration;
import org.uu.nl.embedding.util.config.EmbeddingConfiguration;

import java.io.IOException;
import java.io.Writer;

/**
 * Write the embedding to a GloVe type file, of the format:
 *     word1 0.123 0.134 0.532 0.152
 *     word2 0.934 0.412 0.532 0.159
 *     word3 0.334 0.241 0.324 0.188
 *     ...
 *     word9 0.334 0.241 0.324 0.188
 */
public class GloVeWriter extends EmbeddingWriter {

    public GloVeWriter(Embedding embedding, Configuration config) {
        super(embedding,config);
    }

    private void writeConfig(Writer writer) throws IOException {

        writer.write("# Starting the embedding creation process with following settings:" + "\n");
        writer.write("# Graph File: " + embeddingConfig.getGraph() + "\n");
        writer.write("# Embedding dimensions: " + embeddingConfig.getDim() + "\n");
        writer.write("# Threads: " + embeddingConfig.getThreads() + "\n");
        writer.write("# BCA Alpha: " + embeddingConfig.getBca().getAlpha() + "\n");
        writer.write("# BCA Epsilon: " + embeddingConfig.getBca().getEpsilon() + "\n");
        writer.write("# Gradient Descent Algorithm: " + embeddingConfig.getOpt().getMethod() + "\n");
        writer.write("# " + embeddingConfig.getMethod() + " Tolerance: " + embeddingConfig.getOpt().getTolerance() + "\n");
        writer.write("# " + embeddingConfig.getMethod() + " Maximum Iterations: " + embeddingConfig.getOpt().getMaxiter() + "\n");
        switch (embeddingConfig.getPredicates().getTypeEnum()) {
            case NONE -> {
                writer.write("# Using no predicate weights:" + "\n");
                for (String s : embeddingConfig.getPredicates().getFilter()) {
                    writer.write("# " + s + ": " + 1.0F + "\n");
                }
            }
            case MANUAL -> {
                writer.write("# Using manual predicate weights:" + "\n");
                for (String s : embeddingConfig.getPredicates().getFilter()) {
                    writer.write("# " + s + ": " + embeddingConfig.getPredicates().getWeights().getOrDefault(s, 1.0F) + "\n");
                }
            }
            case PAGERANK -> writer.write("# Pagerank weights used" + "\n");
            case FREQUENCY -> writer.write("# Predicate frequency weights used" + "\n");
            case INVERSE_FREQUENCY -> writer.write("# Inverse predicate frequency weights used" + "\n");
        }
        if(embeddingConfig.usingSimilarity()) {
            writer.write("# Using the following similarity metrics:" + "\n");
            for (EmbeddingConfiguration.SimilarityGroup s : embeddingConfig.getSimilarity()) {
                writer.write("# " + s.toString() + "\n");
            }
        } else writer.write("# No similarity matching will be performed" + "\n");
    }

    @Override
    public void customWrite(Writer w, Embedding embedding) throws IOException {
        writeConfig(w);
    }

}
