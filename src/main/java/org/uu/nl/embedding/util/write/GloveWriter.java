package org.uu.nl.embedding.util.write;

import org.uu.nl.embedding.glove.GloveModel;
import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Jurian Baas
 */
public interface GloveWriter {
	void write(GloveModel model, Path outputFolder) throws IOException;
}
