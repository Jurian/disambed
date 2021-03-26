package org.uu.nl.embedding.util.write;

import me.tongfei.progressbar.ProgressBar;
import org.uu.nl.embedding.convert.util.NodeInfo;
import org.uu.nl.embedding.opt.Optimizer;
import org.uu.nl.embedding.opt.Optimum;
import org.uu.nl.embedding.util.CoOccurrenceMatrix;
import org.uu.nl.embedding.util.config.Configuration;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

/**
 * Writes the output of the GloVe model to 2 text files. One file stores the vectors, the other stores the node names.
 * 
 * @author Jurian Baas
 */
public class SplitFileWriter extends EmbeddingWriter {

	private final String VECTORS_FILE;
	private final String DICT_FILE;

	public SplitFileWriter(String fileName, Configuration config) {
		super(fileName, config);
		this.VECTORS_FILE = fileName + "." + "vectors" + FILETYPE;
		this.DICT_FILE = fileName + "." + "dict" + FILETYPE;
	}

	@Override
	public void write(Optimum optimum, CoOccurrenceMatrix coMatrix, Path outputFolder) throws IOException {

		Files.createDirectories(outputFolder);

		final int vocabSize = coMatrix.nrOfFocusVectors();
		final int dimension = config.getDim();
		final String[] out = new String[dimension];
		final Iterator<Optimizer.EmbeddedEntity> entityIterator = optimum.iterator();

		// Create a tab-separated file
		final String delimiter = "\t";
		final String newLine = "\n";

		try (ProgressBar pb = Configuration.progressBar("Writing to file", vocabSize, "vectors");
			 Writer dict = new BufferedWriter(new FileWriter(outputFolder.resolve(DICT_FILE).toFile()));
			 Writer vect = new BufferedWriter(new FileWriter(outputFolder.resolve(VECTORS_FILE).toFile()))) {

			writeConfig(dict);
			writeConfig(vect);

			dict.write("key" + delimiter + "type" + delimiter + "predicate" + newLine);

			Configuration.Output output = config.getOutput();

			while(entityIterator.hasNext()) {
				Optimizer.EmbeddedEntity entity = entityIterator.next();

				for (int d = 0; d < out.length; d++)
					out[d] = String.format("%11.6E", entity.getVector()[d]);

				vect.write(String.join(delimiter, out) + newLine);
				dict.write(entity.getKey()
						// Remove newlines and tabs
						.replace("\n", "")
						.replace("\r", "")
						.replace(delimiter, "")
						+ delimiter
						+ entity.getInfo().name()
						+ delimiter
						+ (entity.getInfo() == NodeInfo.LITERAL ? coMatrix.getGraph().getLiteralPredicateProperty().getValueAsString(coMatrix.focusIndex2Context(entity.getIndex())) : "")
						+ newLine
				);
				pb.step();
			}
		}
	}
}
