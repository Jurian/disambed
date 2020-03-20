package org.uu.nl.embedding.util.write;

import org.uu.nl.embedding.opt.Optimum;
import org.uu.nl.embedding.util.CoOccurrenceMatrix;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Jurian Baas
 */
public interface EmbeddingWriter {
	void write(Optimum optimum, CoOccurrenceMatrix coMatrix, Path outputFolder) throws IOException;
}
