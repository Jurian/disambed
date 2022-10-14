package org.uu.nl.disembed.util.write;

import org.uu.nl.disembed.embedding.opt.Embedding;
import org.uu.nl.disembed.util.config.Configuration;

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
        super(embedding, config);
    }


    @Override
    public void customWrite(Writer w, Embedding embedding) throws IOException {
        w.write(config.getEmbedding().toString());
    }

}
