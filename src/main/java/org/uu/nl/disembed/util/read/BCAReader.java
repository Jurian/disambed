package org.uu.nl.disembed.util.read;

import com.carrotsearch.hppc.IntHashSet;
import org.uu.nl.disembed.embedding.convert.GraphInformation;
import org.uu.nl.disembed.util.sparse.RandomAccessSparseMatrix;
import org.uu.nl.disembed.util.write.BCAWriter;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class BCAReader implements Reader<BCAReader.SkeletonBCA> {

    public static class SkeletonBCA implements GraphInformation {

        private RandomAccessSparseMatrix matrix;
        private final int nrOfFocusNodes;
        private int nrOfContextNodes;
        private int nrOfVertices;
        private float max;
        private final String[] keys;
        private final IntHashSet focusNodes;

        public SkeletonBCA(int focusVectors) {
            this.nrOfFocusNodes = focusVectors;
            this.keys = new String[focusVectors];
            this.focusNodes = new IntHashSet();
        }

        public void add(int i, int bookmark, String key) {
            this.focusNodes.add(bookmark);
            this.keys[i] = key;
        }

        public void setMatrix(RandomAccessSparseMatrix matrix) {
            this.matrix = matrix;
        }

        public void setNrOfContextNodes(int nrOfContextNodes) {
            this.nrOfContextNodes = nrOfContextNodes;
        }

        public void setNrOfVertices(int nrOfVertices) {
            this.nrOfVertices = nrOfVertices;
        }

        public void setMax(float max) {
            this.max = max;
        }

        public RandomAccessSparseMatrix getMatrix() {
            return matrix;
        }

        public float getMax() {
            return max;
        }

        public int getNrOfContextNodes() {
            return nrOfContextNodes;
        }

        @Override
        public int nrOfFocusNodes() {
            return this.nrOfFocusNodes;
        }

        @Override
        public int nrOfVertices() {
            return this.nrOfVertices;
        }

        @Override
        public String key(int i) {
            return keys[i];
        }

        @Override
        public IntHashSet focusNodes() {
            return this.focusNodes;
        }
    }

    @Override
    public SkeletonBCA load(File file) throws IOException {

        try (DataInputStream  reader = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {

            byte[] header = new byte[BCAWriter.HEADER_LENGTH];

            int result = reader.read(header);

            if(result == BCAWriter.HEADER_LENGTH && new String(header, StandardCharsets.UTF_8).equals(BCAWriter.HEADER)) {


                final int focusVectors = reader.readInt();
                SkeletonBCA skeleton = new SkeletonBCA(focusVectors);

                for(int i = 0; i < focusVectors; i++) {
                    int bookmark = reader.readInt();
                    int length = reader.readInt();
                    String key = new String(reader.readNBytes(length));
                    skeleton.add(i, bookmark, key);
                }

                final int contextVectors = reader.readInt();
                final int coOccurrenceCount = reader.readInt();
                final float max = reader.readFloat();

                skeleton.setNrOfContextNodes(contextVectors);
                skeleton.setMax(max);

                final int rows = reader.readInt();
                final int cols = reader.readInt();
                final int nonZero = reader.readInt();

                RandomAccessSparseMatrix matrix = new RandomAccessSparseMatrix(rows, cols, nonZero);

                for(int i = 0; i < nonZero; i++) {

                    int row = reader.readInt();
                    int col = reader.readInt();
                    float val = reader.readFloat();

                    matrix.add(row, col, val);
                }

                skeleton.setMatrix(matrix);

                return skeleton;
            }

            throw new IllegalArgumentException("Unsupported binary file");
        }

    }
}
