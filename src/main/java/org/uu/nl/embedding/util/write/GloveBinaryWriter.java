package org.uu.nl.embedding.util.write;

import me.tongfei.progressbar.ProgressBar;
import org.uu.nl.embedding.Settings;
import org.uu.nl.embedding.glove.GloveModel;
import org.uu.nl.embedding.glove.util.WritableUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;


/**
 * Writes the output of the GloVe model to a binary file for efficient random access lookup. 
 * This code is a modified version of <a href="https://github.com/thomasjungblut">Thomas Jungblut's</a> code.
 * 
 * @author Jurian Baas & Thomas Jungblut (original)
 * @see <a href="https://github.com/thomasjungblut/glove/blob/master/src/de/jungblut/glove/impl/GloveBinaryWriter.java">Original version</a>
 */
public class GloveBinaryWriter implements GloveWriter {
	
	private final String VECTORS_FILE;
	private final String DICT_FILE;
	private static final Settings settings = Settings.getInstance();

	public GloveBinaryWriter(String fileName) {
		this.VECTORS_FILE = fileName + "." + "vectors.bin";
		this.DICT_FILE = fileName + "." + "dict.bin";
	}
	
	private void writeVectorData(double[] v, DataOutput out) throws IOException {
		for (int i = 0; i < v.length; i++) 
			out.writeInt(Float.floatToIntBits((float) v[i]));
	}

	private BufferedOutputStream createStream(Path outputFolder, String fileName) throws FileNotFoundException {
		return new BufferedOutputStream(new FileOutputStream(outputFolder.resolve(fileName).toFile()));
	}

	@Override
	public void write(GloveModel model, Path outputFolder) throws IOException {
		Files.createDirectories(outputFolder);
		
		long blockSize = -1, offset = 0;
		final int vocabSize = model.getVocabSize();
		final int dimension = model.getDimension();
		final double[] results = model.getOptimum().getResult();
		byte[] buf;

		try(ProgressBar pb = settings.progressBar("Writing to file", vocabSize, "vectors");
			DataOutputStream dict = new DataOutputStream(createStream(outputFolder, DICT_FILE));
			BufferedOutputStream vec = new BufferedOutputStream(createStream(outputFolder, VECTORS_FILE));
			ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream()) {

			for (int i = 0; i < vocabSize; i++) {

				byteBuffer.reset();

				try (DataOutputStream out = new DataOutputStream(byteBuffer)) {
					writeVectorData(Arrays.copyOfRange(results, i * dimension, i * dimension + dimension), out);
				}

				buf = byteBuffer.toByteArray();
				if (blockSize == -1) blockSize = buf.length;

				if (blockSize != buf.length) {
					System.err.println(
							String.format("Can't write different block size! Expected %d but was %d. "
											+ "This happened because the vectors in the stream had different dimensions.",
									blockSize, buf.length));
				}

				vec.write(buf);

				dict.writeUTF(model.getCoMatrix().getKey(i));
				WritableUtils.writeVLong(dict, offset);

				offset += buf.length;
				pb.step();
			}
		}
	}
}
