package org.uu.nl.disembed.util.write;

import org.apache.log4j.Logger;
import org.uu.nl.disembed.embedding.bca.BookmarkColoring;
import org.uu.nl.disembed.util.config.Configuration;
import org.uu.nl.disembed.util.config.IntermediateOutputConfiguration;
import org.uu.nl.disembed.util.sparse.RandomAccessSparseMatrix;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public record BCAWriter(Configuration config, BookmarkColoring bca) implements Writer {

    public final static Logger logger = Logger.getLogger(BCAWriter.class);

    public static final String FILETYPE = ".bca";
    public static final String OUTPUT_DIRECTORY = "bca";

    public static final String HEADER = "BCA_SPARSE_MATRIX";
    public static final int HEADER_LENGTH = HEADER.length();

    @Override
    public void write() throws IOException {

        IntermediateOutputConfiguration outputConfig = config.getIntermediateOutput();

        String fileName = outputConfig.getBca().getFilename() + FILETYPE;
        logger.info("Writing file: " + fileName);

        Path outputFolder = Paths.get("").toAbsolutePath().resolve(OUTPUT_DIRECTORY);
        Files.createDirectories(outputFolder);

        try (DataOutputStream writer = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(outputFolder.resolve(fileName).toFile())))) {

            // Write header
            writer.write(HEADER.getBytes(StandardCharsets.UTF_8));

            final int focusVectors = bca.nrOfFocusVectors();

            writer.writeInt(focusVectors);
            for(int i = 0; i < focusVectors; i++) {
                writer.writeInt(bca.focusIndex2Context(i));
                final String key = bca.getKey(i);
                writer.writeInt(key.length());
                writer.writeBytes(bca.getKey(i));
            }

            writer.writeInt(bca.nrOfContextVectors());
            writer.writeInt(bca.coOccurrenceCount());
            writer.writeFloat(bca.max());

            RandomAccessSparseMatrix matrix = bca.getSparseMatrix();

            // Write data
            writer.writeInt(matrix.rows());
            writer.writeInt(matrix.columns());
            writer.writeInt(matrix.getNonZero());

            for(int i = 0; i < matrix.getNonZero(); i++) {
                writer.writeInt(matrix.getRow(i));
                writer.writeInt(matrix.getColumn(i));
                writer.writeFloat(matrix.getValue(i));
            }
        }
    }
}
