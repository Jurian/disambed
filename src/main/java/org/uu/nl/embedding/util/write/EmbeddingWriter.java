package org.uu.nl.embedding.util.write;

import org.uu.nl.embedding.opt.OptimizerModel;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Jurian Baas
 */
public interface EmbeddingWriter {
	void write(OptimizerModel model, Path outputFolder) throws IOException;
}
