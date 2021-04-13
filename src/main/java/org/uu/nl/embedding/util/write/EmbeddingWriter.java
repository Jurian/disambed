package org.uu.nl.embedding.util.write;

import me.tongfei.progressbar.ProgressBar;
import org.uu.nl.embedding.opt.Optimizer;
import org.uu.nl.embedding.opt.Optimum;
import org.uu.nl.embedding.util.CoOccurrenceMatrix;
import org.uu.nl.embedding.util.config.Configuration;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.Iterator;

/**
 * @author Jurian Baas
 */
public abstract class EmbeddingWriter {

	public static final String FILETYPE = ".tsv";
	protected final String VECTORS_FILE;
	protected final Configuration config;

	public EmbeddingWriter(String fileName, Configuration config){
		this.VECTORS_FILE = fileName + FILETYPE;
		this.config = config;
	}

	protected void writeConfig(Writer writer) throws IOException {

		writer.write("# Starting the embedding creation process with following settings:" + "\n");
		writer.write("# Graph File: " + config.getGraph() + "\n");
		writer.write("# Embedding dimensions: " + config.getDim() + "\n");
		writer.write("# Threads: " + config.getThreads() + "\n");
		writer.write("# BCA Alpha: " + config.getBca().getAlpha() + "\n");
		writer.write("# BCA Epsilon: " + config.getBca().getEpsilon() + "\n");
		writer.write("# Gradient Descent Algorithm: " + config.getOpt().getMethod() + "\n");
		writer.write("# " + config.getMethod() + " Tolerance: " + config.getOpt().getTolerance() + "\n");
		writer.write("# " + config.getMethod() + " Maximum Iterations: " + config.getOpt().getMaxiter() + "\n");
		switch(config.getPredicates().getTypeEnum()) {
			case NONE:
				writer.write("# Using no predicate weights:"+ "\n");
				for(String s : config.getPredicates().getFilter()) {
					writer.write("# " + s + ": " + 1.0F + "\n");
				}
				break;
			case MANUAL:
				writer.write("# Using manual predicate weights:"+ "\n");
				for(String s : config.getPredicates().getFilter()) {
					writer.write("# " + s + ": " + config.getPredicates().getWeights().getOrDefault(s, 1.0F) + "\n");
				}
				break;
			case PAGERANK:
				writer.write("# Pagerank weights used"+ "\n");
				break;
			case FREQUENCY:
				writer.write("# Predicate frequency weights used"+ "\n");
				break;
			case INVERSE_FREQUENCY:
				writer.write("# Inverse predicate frequency weights used"+ "\n");
				break;
		}
		if(config.usingSimilarity()) {
			writer.write("# Using the following similarity metrics:" + "\n");
			for (Configuration.SimilarityGroup s : config.getSimilarity()) {
				writer.write("# " + s.toString() + "\n");
			}
		} else writer.write("# No similarity matching will be performed" + "\n");
	}

	public abstract void write(Optimum optimum, CoOccurrenceMatrix coMatrix, Path outputFolder) throws IOException;

	protected void writeLines(Iterator<Optimizer.EmbeddedEntity> entityIterator, String[] out, ProgressBar pb, Writer w) throws IOException {
		while(entityIterator.hasNext()) {
			Optimizer.EmbeddedEntity entity = entityIterator.next();

			for (int d = 0; d < out.length; d++)
				out[d] = String.format("%11.6E", entity.getVector()[d]);

			w.write(entity.getKey()
					.replace("\n", "")
					.replace("\r", "")
					.replace("	", "")
					+ "	"
					+ String.join("	", out) + "\n"
			);
			pb.step();
		}
	}
}
