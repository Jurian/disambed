package org.uu.nl.embedding.util.read;

import org.uu.nl.embedding.opt.Embedding;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class EmbeddingReader implements Reader<Embedding> {

    @Override
    public Embedding load(File file) throws IOException {

        try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String firstLine = reader.readLine();

            if(Pattern.matches("(\\d)+\\s(\\d)+", firstLine)) {
                return readWord2Vec(reader, firstLine);
            } else {
                return readGloVe(reader);
            }
        }
    }

    private Embedding readWord2Vec(BufferedReader reader, String firstLine) throws IOException {

        String[] info = firstLine.split("\\s");
        final int size = Integer.parseInt(info[0]);
        final int dim = Integer.parseInt(info[1]);

        Embedding embedding = new Embedding(size, dim);
        String line;

        int i = 0;
        while((line = reader.readLine()) != null) {

            line = line.trim();
            if(line.startsWith("#")) continue;

            String[] lineInfo = line.split("\\s");

            embedding.setKey(i, lineInfo[0]);

            float[] vector = new float[dim];

            for(int j = 0; j < dim; j++) {
                vector[j] = Float.parseFloat(lineInfo[j+1]);
            }
            embedding.setVector(i, vector);
            i++;
        }

        return embedding;
    }

    private Embedding readGloVe(BufferedReader reader) throws IOException {

        String line;

        List<String> dict = new ArrayList<>();
        List<float[]> data = new ArrayList<>();

        int i = 0;
        int dim = 0;
        while((line = reader.readLine()) != null) {

            line = line.trim();
            if(line.startsWith("#")) continue;

            String[] lineInfo = line.split("\\s");

            dim = lineInfo.length - 1;

            dict.add(lineInfo[0]);

            float[] vector = new float[dim];

            for(int j = 0; j < dim; j++) {
                vector[j] = Float.parseFloat(lineInfo[j+1]);
            }
            data.add(vector);
            i++;
        }

        if(i == 0 || dim == 0) {
            throw new IOException("Error reading GloVe embedding file");
        }

        return new Embedding(dict.toArray(String[]::new), data.toArray(float[][]::new));
    }
}
