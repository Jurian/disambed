package org.uu.nl.embedding.util.save;

import java.io.IOException;
import java.nio.file.Path;

import org.uu.nl.embedding.analyze.glove.GloveModel;

public interface GloveWriter {
	public void write(GloveModel model, Path outputFolder) throws IOException;
}
