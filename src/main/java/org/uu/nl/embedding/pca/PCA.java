package org.uu.nl.embedding.pca;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.EVD;
import no.uib.cipr.matrix.NotConvergedException;
import org.apache.log4j.Logger;

import java.util.Arrays;

public class PCA {

    private final static Logger logger = Logger.getLogger(PCA.class);

    public static void main(String[] args) {
        try {
            PCA pca = new PCA();

            int dim = 3;

            double[] vectors = new double[] {8,5,2, 7,1,9, 2,2,2, 2,2,2};
            //double[] vectors = new double[] {8,7,2,2, 5,1,2,2, 2,9,2,2};

           // double[] vectors = new double[] {0,3,6, 1,4,7, 2,5,8};

            System.out.println(Arrays.toString(vectors));
            System.out.println(Arrays.toString(pca.toColumnMajor(vectors, dim)));

            System.out.println(Arrays.toString(pca.toColumnMajor2(new double[] {8,5,2, 7,1,9, 2,2,2, 2,2,2}, dim)));

            double[] cov = pca.covariance(vectors, dim);
            // Note that cov is symmetric
            DenseMatrix covMatrix = new DenseMatrix(cov.length / dim, dim, cov, false);
            DenseMatrix vectorMatrix = new DenseMatrix(vectors.length / dim, dim, vectors, false);

            //System.out.println(Arrays.toString(vectors));
            System.out.println(covMatrix);

            EVD eigen = new EVD(dim, true , false);
            eigen.factor(covMatrix);

            DenseMatrix eigenVectors = eigen.getLeftEigenvectors();

            vectorMatrix = transpose(vectorMatrix);
            DenseMatrix projectedVectors = (DenseMatrix) eigenVectors.transAmult(
                    vectorMatrix,
                    new DenseMatrix(vectorMatrix.numRows(), vectorMatrix.numColumns())
            );
            projectedVectors = transpose(projectedVectors);

            System.out.println(projectedVectors);


        } catch (NotConvergedException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private double[] toColumnMajor(double[] data, int numCols) {

        final int numRows = data.length / numCols;
        final double[] transposed = new double[data.length];
        for(int i = 0; i < data.length; i++) {
            transposed[(i / numCols) + ((i % numCols) * numRows)] = data[i];
        }
        return transposed;
    }

    private double[] toColumnMajor2(double[] data, int numCols) {

        final int numRows = data.length / numCols;

        int a;
        int marker = 0;
        double t = 0;

        for(int i = 0; i < data.length; ) {

            data[i] = t;

            a = (i / numCols) + ((i % numCols) * numRows);
            t = data[a];
            i = a;







            if(i == marker) i = marker + 1;
        }
        return data;
    }

    private static DenseMatrix transpose(DenseMatrix m) {
        return (DenseMatrix) m.transpose(invert(m));
    }

    private static DenseMatrix invert(DenseMatrix m) {
        return new DenseMatrix(m.numColumns(), m.numRows());
    }

    /**
     * Calculate the mean of every column
     * @param data The matrix, column-major
     * @param dim The number of columns in the matrix
     * @return An array of means
     */
    private double[] columnMeans(double[] data, int dim) {
        assert data.length % dim == 0;

        int rowCount = data.length / dim;
        double[] means = new double[dim];
        for(int i = 0; i < data.length; i++) {
            int row = i % rowCount, col = i / rowCount;
            means[col] = (data[i] + row * means[col]) / (row + 1);
        }
        return means;
    }

    /**
     * Center the data around 0, will replace the input matrix with new values
     * @param data The matrix, column-major
     * @param dim The number of columns in the matrix
     */
    private void center(double[] data, int dim) {
        assert data.length % dim == 0;
        int rowCount = data.length / dim;
        double[] means = columnMeans(data, dim);
        for(int i = 0; i < data.length; i++) {
            data[i] -= means[i / rowCount];
        }
    }

    /**
     * Calculate the covariance of an input matrix
     * @param data The matrix, row-major
     * @param dim The number of columns in the matrix
     * @return A new square matrix of size dim*dim
     */
    private double[] covariance(double[] data, int dim) {
        assert data.length != 0;
        assert data.length % dim == 0;

        int rowCount = data.length / dim;
        double[] covMatrix = new double[dim*dim];

        center(data, dim);

        /*
        Note that at this point the data has been centered, which means that
        we do not have to subtract the column means as they are all 0
         */

        for(int col1 = 0; col1 < dim; col1++) {
            for(int col2 = 0; col2 < dim; col2++) {

                double cov = 0;
                for(int i = 0; i < rowCount; i++) {
                    cov += (data[i + col1 * rowCount] ) * (data[i + col2 * rowCount] );
                }
                cov /= (rowCount - 1);
                covMatrix[col1 + col2 * dim] = cov;
            }
        }
        return covMatrix;
    }
}
