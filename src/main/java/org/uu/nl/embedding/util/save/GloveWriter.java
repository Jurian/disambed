package org.uu.nl.embedding.util.save;

import org.uu.nl.embedding.glove.GloveModel;
import java.io.IOException;
import java.nio.file.Path;

public interface GloveWriter {
	void write(GloveModel model, Path outputFolder) throws IOException;
}
