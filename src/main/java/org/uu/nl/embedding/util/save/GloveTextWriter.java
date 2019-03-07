package org.uu.nl.embedding.util.save;

import org.uu.nl.embedding.analyze.glove.GloveModel;
import org.uu.nl.embedding.progress.Progress;
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

	public GloveTextWriter(String fileName) {

		if(fileName.contains(".")) {
			int idx = fileName.lastIndexOf(".");
			fileName = fileName.substring(0, idx);
		}

		this.VECTORS_FILE = fileName + "." + "vectors.txt";
		this.DICT_FILE = fileName + "." + "dict.txt";
	}
	
	@Override
	public void write(GloveModel model, Path outputFolder, Publisher publisher) throws IOException {
		
		Files.createDirectories(outputFolder);
		
		final int vocabSize = model.getVocabSize();
		final int dimension = model.getDimension();
		final String[] out = new String[dimension];
		final double[] result = model.getOptimum().getResult();

		if(publisher!= null) {
			publisher.setNewMax(vocabSize);
		}

		Progress progress = new Progress();

		try(Writer dict = new BufferedWriter(new FileWriter(outputFolder.resolve(DICT_FILE).toFile()))) {
			try(Writer vect = new BufferedWriter(new FileWriter(outputFolder.resolve(VECTORS_FILE).toFile()))) {

				for(int i = 0; i < vocabSize; i++) {
					for(int d = 0; d < out.length; d++) 
						out[d] = String.format("%11.6E", result[d + i*dimension]);
					
					vect.write(String.join(",", out) + "\n");
					dict.write(model.getCoMatrix().getKey(i)
							.replace("\n", "")
							.replace("\r", "")
							.replace('|', '_') + "|" + model.getCoMatrix().getType(i) + "\n");

					if(publisher!= null) {
						progress.setN(i);
						publisher.updateProgress(progress);
					}
				}

				if(publisher != null) {
					progress.setFinished(true);
					publisher.updateProgress(progress);
				}
			}
		}
	}
}
