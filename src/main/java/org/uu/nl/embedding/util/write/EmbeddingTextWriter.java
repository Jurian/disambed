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
import java.util.Map;

/**
 * Writes the output of the GloVe model to 2 text files. One file stores the vectors, the other stores the node names.
 * 
 * @author Jurian Baas
 */
public class EmbeddingTextWriter implements EmbeddingWriter {

	private final String VECTORS_FILE;
	private final String DICT_FILE;
	private final boolean[] writeNodeTypes;
	private final Configuration config;

	public EmbeddingTextWriter(String fileName, Configuration config) {
		String FILETYPE = ".tsv";
		this.VECTORS_FILE = fileName + "." + "vectors" + FILETYPE;
		this.DICT_FILE = fileName + "." + "dict" + FILETYPE;
		this.config = config;
		this.writeNodeTypes = new boolean[3];
		this.writeNodeTypes[NodeInfo.URI.id] = config.getOutput().outputUriNodes();
		this.writeNodeTypes[NodeInfo.BLANK.id]  = config.getOutput().outputBlankNodes();
		this.writeNodeTypes[NodeInfo.LITERAL.id] = config.getOutput().outputLiteralNodes();
	}

	private void writeConfig(Writer writer) throws IOException {

		writer.write("# Starting the embedding creation process with following settings:" + "\n");
		writer.write("# Graph File: " + config.getGraph() + "\n");
		writer.write("# Embedding dimensions: " + config.getDim() + "\n");
		writer.write("# Threads: " + config.getThreads() + "\n");
		writer.write("# BCA Alpha: " + config.getBca().getAlpha() + "\n");
		writer.write("# BCA Epsilon: " + config.getBca().getEpsilon() + "\n");
		writer.write("# BCA Type: " + config.getBca().getType() + "\n");
		writer.write("# BCA Normalize: " + config.getBca().getNormalize() + "\n");
		writer.write("# Gradient Descent Algorithm: " + config.getOpt().getMethod() + "\n");
		writer.write("# " + config.getMethod() + " Tolerance: " + config.getOpt().getTolerance() + "\n");
		writer.write("# " + config.getMethod() + " Maximum Iterations: " + config.getOpt().getMaxiter() + "\n");

		if(config.usingPca()) writer.write("# PCA Minimum Variance: " + config.getPca().getVariance() + "\n");
		else writer.write("# No PCA will be performed" + "\n");

		if(config.usingWeights()) {
			writer.write("# Using weights, predicates that are not listed are ignored:" + "\n");
			for (Map.Entry<String, Float> entry : config.getWeights().entrySet()) {
				writer.write("# " + entry.getKey() + ": " + entry.getValue() + "\n");
			}
		} else writer.write("# No weights specified, using linear weight" + "\n");

		if(config.usingSimilarity()) {
			writer.write("# Using the following similarity metrics:" + "\n");
			for (Configuration.SimilarityGroup s : config.getSimilarity()) {
				writer.write("# " + s.toString() + "\n");
			}
		} else writer.write("# No similarity matching will be performed" + "\n");
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
