package org.uu.nl.embedding.util.write;

import me.tongfei.progressbar.ProgressBar;
import org.uu.nl.embedding.convert.util.NodeInfo;
import org.uu.nl.embedding.opt.OptimizerModel;
import org.uu.nl.embedding.util.config.Configuration;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
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

		writer.write("# Starting the embedding creation process with following settings:");
		writer.write("# Graph File: " + config.getGraph());
		writer.write("# Embedding dimensions: " + config.getDim());
		writer.write("# Threads: " + config.getThreads());
		writer.write("# BCA Alpha: " + config.getBca().getAlpha());
		writer.write("# BCA Epsilon: " + config.getBca().getEpsilon());
		writer.write("# BCA Directed: " + config.getBca().isDirected());
		writer.write("# BCA normalize: " + config.getBca().getNormalize());
		writer.write("# Gradient Descent Algorithm: " + config.getOpt().getMethod());
		writer.write("# " + config.getMethod() + " Tolerance: " + config.getOpt().getTolerance());
		writer.write("# " + config.getMethod() + " Maximum Iterations: " + config.getOpt().getMaxiter());

		if(config.usingPca()) writer.write("# PCA Minimum Variance: " + config.getPca().getVariance());
		else writer.write("# No PCA will be performed");

		if(config.usingWeights()) {
			writer.write("# Using weights, predicates that are not listed are ignored:");
			for (Map.Entry<String, Float> entry : config.getWeights().entrySet()) {
				writer.write("# " + entry.getKey() + ": " + entry.getValue());
			}
		} else writer.write("# No weights specified, using linear weight");

		if(config.usingSimilarity()) {
			writer.write("# Using the following similarity metrics:");
			for (Configuration.SimilarityGroup s : config.getSimilarity()) {
				writer.write("# " + s.toString());
			}
		} else writer.write("# No similarity matching will be performed");
	}

	@Override
	public void write(OptimizerModel model, Path outputFolder) throws IOException {

		Files.createDirectories(outputFolder);

		byte type;
		// Take the number of vertices because we don't want to print vectors for predicates
		final int vocabSize = model.getCoMatrix().vocabSize();
		final int dimension = model.getDimension();
		final String[] out = new String[dimension];
		final double[] result = model.getOptimum().getResult();

		// Create a tab-separated file
		final String delimiter = "\t";
		final String newLine = "\n";

		try (ProgressBar pb = Configuration.progressBar("Writing to file", vocabSize, "vectors");
			 Writer dict = new BufferedWriter(new FileWriter(outputFolder.resolve(DICT_FILE).toFile()));
			 Writer vect = new BufferedWriter(new FileWriter(outputFolder.resolve(VECTORS_FILE).toFile()))) {

			writeConfig(dict);
			writeConfig(vect);

			dict.write("key" + delimiter + "type" + newLine);

			Configuration.Output output = config.getOutput();

			long skipped = 0;

			for (int i = 0; i < vocabSize; i++) {

				type = model.getCoMatrix().getType(i);

				if(!writeNodeTypes[type]) {
					pb.maxHint(pb.getMax()-1);
					skipped++;
					pb.setExtraMessage("Skipped " + skipped);
					continue;
				}

				final String key = model.getCoMatrix().getKey(i);
				final NodeInfo nodeInfo = NodeInfo.fromByte(type);
				boolean skip = false;
				switch (nodeInfo) {
					case URI:
						if(!output.getUri().isEmpty()) skip = output.getUri().stream().noneMatch(key::startsWith);
						break;
					case BLANK:
						if(!output.getBlank().isEmpty()) skip = output.getBlank().stream().noneMatch(key::startsWith);
						break;
					case LITERAL:
						if(!output.getLiteral().isEmpty()) skip = output.getLiteral().stream().noneMatch(key::startsWith);
						break;
				}

				if(skip)  {
					pb.maxHint(pb.getMax()-1);
					skipped++;
					pb.setExtraMessage("Skipped " + skipped);
					continue;
				}

				for (int d = 0; d < out.length; d++)
					out[d] = String.format("%11.6E", result[d + i * dimension]);

				vect.write(String.join(delimiter, out) + newLine);
				dict.write(key
						// Remove newlines and tabs
						.replace("\n", "")
						.replace("\r", "")
						.replace(delimiter, "")
						+ delimiter
						+ nodeInfo.name()
						+ newLine
				);
				pb.step();
			}
		}
	}
}
