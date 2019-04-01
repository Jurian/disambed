package org.uu.nl.embedding.util.write;

import me.tongfei.progressbar.ProgressBar;
import org.uu.nl.embedding.Settings;
import org.uu.nl.embedding.convert.util.NodeInfo;
import org.uu.nl.embedding.glove.GloveModel;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Writes the output of the GloVe model to 2 text files. One file stores the vectors, the other stores the node names.
 * 
 * @author Jurian Baas
 */
public class GloveTextWriter implements GloveWriter {

	private final String VECTORS_FILE;
	private final String DICT_FILE;

	public GloveTextWriter(String fileName) {
		String FILETYPE = ".tsv";
		this.VECTORS_FILE = fileName + "." + "vectors" + FILETYPE;
		this.DICT_FILE = fileName + "." + "dict" + FILETYPE;
	}

	private static final Settings settings = Settings.getInstance();

	@Override
	public void write(GloveModel model, Path outputFolder) throws IOException {

		Files.createDirectories(outputFolder);

		byte type;
		final int vocabSize = model.getVocabSize();
		final int dimension = model.getDimension();
		final String[] out = new String[dimension];
		final double[] result = model.getOptimum().getResult();

		// Create a tab-separated file
		final String delimiter = "\t";
		final String newLine = "\n";

		try (ProgressBar pb = settings.progressBar("Writing to file", vocabSize, "vectors");
			 Writer dict = new BufferedWriter(new FileWriter(outputFolder.resolve(DICT_FILE).toFile()));
			 Writer vect = new BufferedWriter(new FileWriter(outputFolder.resolve(VECTORS_FILE).toFile()))) {

			dict.write("key" + delimiter + "type" + newLine);

			for (int i = 0; i < vocabSize; i++) {

				type = model.getCoMatrix().getType(i);

				// Do not write blank nodes
				if(type == NodeInfo.BLANK) {
					pb.step();
					continue;
				}

				for (int d = 0; d < out.length; d++)
					out[d] = String.format("%11.6E", result[d + i * dimension]);

				vect.write(String.join(delimiter, out) + newLine);
				dict.write(model.getCoMatrix().getKey(i)
						// Remove newlines and tabs
						.replace("\n", "")
						.replace("\r", "")
						.replace(delimiter, "")
						+ delimiter
						+ type
						+ newLine
				);
				pb.step();
			}
		}
	}
}
