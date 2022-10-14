package org.uu.nl.embedding.util.write;

import me.tongfei.progressbar.ProgressBar;
import org.apache.log4j.Logger;
import org.uu.nl.embedding.opt.Embedding;
import org.uu.nl.embedding.opt.Optimizer;
import org.uu.nl.embedding.util.Progress;
import org.uu.nl.embedding.util.config.Configuration;
import org.uu.nl.embedding.util.config.EmbeddingConfiguration;
import org.uu.nl.embedding.util.config.OutputConfiguration;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

/**
 * @author Jurian Baas
 */
public abstract class EmbeddingWriter implements org.uu.nl.embedding.util.write.Writer {

	public final static Logger logger = Logger.getLogger(EmbeddingWriter.class);

	// Create a tab-separated file
	public static final String DELIMITER = "\t";
	public static final String NEWLINE = "\n";
	public static final String FILETYPE = ".tsv";
	public static final String OUTPUT_DIRECTORY = "embeddings";
	protected final String fileName;
	protected final EmbeddingConfiguration embeddingConfig;
	protected final OutputConfiguration outputConfig;
	protected final Embedding embedding;

	public EmbeddingWriter(Embedding embedding, Configuration config) {

		this.embeddingConfig = config.getEmbedding();
		this.outputConfig = config.getOutput();
		this.embedding = embedding;

		String outFileName = outputConfig.getEmbedding().getFilename();
		if(outFileName == null || outFileName.isEmpty()) {
			outFileName = createFileName(embeddingConfig);
		}

		this.fileName = outFileName + FILETYPE;

	}

	public void write() throws IOException {

		logger.info("Writing file: " + fileName);

		Path outputFolder = Paths.get("").toAbsolutePath().resolve(OUTPUT_DIRECTORY);
		Files.createDirectories(outputFolder);

		final int vocabSize = embedding.getSize();
		final int dimension = embeddingConfig.getDim();
		final String[] out = new String[dimension];
		final Iterator<Optimizer.EmbeddedEntity> entityIterator = embedding.iterator();

		try (ProgressBar pb = Progress.progressBar("Writing to file", vocabSize, "vectors");
			 Writer w = new BufferedWriter(new FileWriter(outputFolder.resolve(fileName).toFile()))) {
			customWrite(w, embedding);
			writeLines(entityIterator, out, pb, w);
		}
	}

	public abstract void customWrite(Writer w, Embedding e) throws IOException ;

	protected void writeLines(Iterator<Optimizer.EmbeddedEntity> entityIterator, String[] out, ProgressBar pb, Writer w) throws IOException {
		while(entityIterator.hasNext()) {
			Optimizer.EmbeddedEntity entity = entityIterator.next();

			for (int d = 0; d < out.length; d++)
				out[d] = String.format("%11.6E", entity.vector()[d]);

			w.write(entity.id()
					.replace("\n", "")
					.replace("\r", "")
					.replace("	", "")
					+ DELIMITER
					+ String.join(DELIMITER, out) + NEWLINE
			);
			pb.step();
		}
	}

	private String createFileName(EmbeddingConfiguration config) {
		String outFileName = config.getGraphFile().getName().toLowerCase();
		if(outFileName.contains(".")) {
			outFileName = outFileName.substring(0, outFileName.lastIndexOf("."));
		}
		outFileName += "_" + config.getMethod().toLowerCase();

		if(config.getSimilarity() != null && !config.getSimilarity().isEmpty()) {
			outFileName += "_partial";
		} else {
			outFileName += "_exact";
		}

		outFileName += "_" + config.getBca().getAlpha() + "_" + config.getBca().getEpsilon();
		outFileName += "_" + config.getOpt().getMethod();
		outFileName += "_" + config.getDim();

		return outFileName;
	}
}
