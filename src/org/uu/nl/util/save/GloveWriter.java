package org.uu.nl.util.save;

import java.io.IOException;
import java.nio.file.Path;

import org.uu.nl.analyze.glove.GloveModel;

public interface GloveWriter {
	public void write(GloveModel model, Path outputFolder) throws IOException;
}
