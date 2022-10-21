package org.uu.nl.disembed.util.write;

import com.github.jelmerk.knn.hnsw.HnswIndex;
import org.apache.log4j.Logger;
import org.uu.nl.disembed.embedding.opt.Optimizer;
import org.uu.nl.disembed.util.config.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public record HnswIndexWriter(
        HnswIndex<String, float[], Optimizer.EmbeddedEntity, Float> index,
        Configuration config) implements Writer {

    public final static Logger logger = Logger.getLogger(HnswIndexWriter.class);
    public static final String OUTPUT_DIRECTORY = "ann";
    public static final String FILETYPE = ".index";

    @Override
    public void write() throws IOException {

        Path outputFolder = Paths.get("").toAbsolutePath().resolve(OUTPUT_DIRECTORY);
        Files.createDirectories(outputFolder);

        final String fileName = config.getIntermediateOutput().getHnsw().getFilename() + FILETYPE;

        index.save(outputFolder.resolve(fileName));
    }
}
