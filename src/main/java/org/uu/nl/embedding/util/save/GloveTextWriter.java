package org.uu.nl.embedding.util.save;

import org.uu.nl.embedding.analyze.glove.GloveModel;
import org.uu.nl.embedding.progress.DoNothingPublisher;
import org.uu.nl.embedding.progress.ProgressState;
import org.uu.nl.embedding.progress.Publisher;

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
	private final String FILETYPE = ".tsv";

	public GloveTextWriter(String fileName) {
		this.VECTORS_FILE = fileName + "." + "vectors" + FILETYPE;
		this.DICT_FILE = fileName + "." + "dict" + FILETYPE;
	}
	
	@Override
	public void write(GloveModel model, Path outputFolder, Publisher publisher) throws IOException {
		
		Files.createDirectories(outputFolder);
		
		final int vocabSize = model.getVocabSize();
		final int dimension = model.getDimension();
		final String[] out = new String[dimension];
		final double[] result = model.getOptimum().getResult();

		// Create a tab-separated file
		final String delimiter = "\t";
		final String newLine = "\n";

		if(publisher == null) {
			publisher = new DoNothingPublisher();
		}
		publisher.setNewMax(vocabSize);

		ProgressState progress = new ProgressState();

		try(Writer dict = new BufferedWriter(new FileWriter(outputFolder.resolve(DICT_FILE).toFile()))) {
			try(Writer vect = new BufferedWriter(new FileWriter(outputFolder.resolve(VECTORS_FILE).toFile()))) {

				for(int i = 0; i < vocabSize; i++) {

					for(int d = 0; d < out.length; d++) 
						out[d] = String.format("%11.6E", result[d + i*dimension]);
					
					vect.write(String.join(delimiter, out) + newLine);
					dict.write(model.getCoMatrix().getKey(i)
							// Remove newlines and tabs
							.replace("\n", "")
							.replace("\r", "")
							.replace(delimiter, "")
							+ delimiter
							+ model.getCoMatrix().getType(i)
							+ newLine
					);

					progress.setN(i);
					publisher.updateProgress(progress);
				}

				progress.setFinished(true);
				progress.setN(vocabSize);
				publisher.updateProgress(progress);
			}
		}
	}
}
