package org.uu.nl.embedding.pca;

import com.github.fommil.netlib.LAPACK;

import org.apache.commons.math.util.FastMath;
import org.apache.log4j.Logger;
import org.netlib.util.intW;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.IntStream;

public class PCA {

    private final static Logger logger = Logger.getLogger(PCA.class);

    public static void main(String[] args) {

        double[] vectors = new double[] {4, 6, 10, 3, 10, 13, -2, -6, -8, 1, 7, 5};
        int dim = 3;

        PCA pca = new PCA(vectors, dim, false);

    }

    private String toString(double[] data, int nCols) {

        StringBuilder out = new StringBuilder();
        DecimalFormat df = new DecimalFormat("####0.0000");
        final int nRows = data.length / nCols;
        for (int i = 0; i < nRows; i++) {
            for (int j = 0; j < nCols; j++) {
                double value = data[i + j * nRows];
                if (value >= 0)
                    out.append(" ");
                out.append(" " + df.format(value));
            }
            out.append("\n");
        }

        return out.toString();
    }

    /**
     * Contains the real and imaginary parts of the eigenvalues
     */
    private final double[] eigenValuesReal, eigenValuesImaginary, varPercentage, sd;
    private final double[] data, work, covarianceMatrix, leftEigenVectors;
    private final int nRows, nCols;
    int[] sortedIndices;

    public PCA(double[] vectors, int dim, boolean colMajor)  {

        assert vectors.length != 0;
        assert vectors.length % dim == 0;

        this.nCols = dim;
        this.nRows = vectors.length / dim;

        // Allocate space for the decomposition
        eigenValuesReal = new double[nRows];
        eigenValuesImaginary = new double[nRows];
        sd = new double[nRows];
        varPercentage = new double[nRows];
        leftEigenVectors = new double[nCols * nCols];

        this.data = Arrays.copyOf(vectors, vectors.length);
        if(!colMajor)
            toColumnMajorInPlace();

        System.out.println(toString(data, dim));
        center();
        System.out.println(toString(data, dim));
        covarianceMatrix = covariance();
        System.out.println(toString(covarianceMatrix, dim));

        // Find the needed workspace
        double[] workSize = new double[1];
        intW info = new intW(0);
        int ld = FastMath.max(1, nCols);
        LAPACK.getInstance().dgeev("V", "N", nCols,
                new double[0], ld, new double[0], new double[0],
                new double[0], ld, new double[0], ld,
                workSize, -1, info);

        // Allocate workspace
        int workSpace = 0;
        if (info.val != 0) {
            workSpace = 4 * nRows;
        } else
            workSpace = (int) workSize[0];

        workSpace = Math.max(1, workSpace);
        work = new double[workSpace];

        info = new intW(0);
        ld = FastMath.max(1, nCols);
        LAPACK.getInstance().dgeev("V", "N", nCols,
                covarianceMatrix, ld, eigenValuesReal, eigenValuesImaginary,
                leftEigenVectors, ld, new double[0],
                ld, work, work.length, info);

        sortedIndices = IntStream.range(0, eigenValuesReal.length)
                .boxed().sorted(Comparator.comparingDouble(i -> -eigenValuesReal[i]))
                .mapToInt(i -> i).toArray();

        double sum = 0;
        for(int i = 0; i < eigenValuesReal.length; i++) sum += eigenValuesReal[i];

        for(int i = 0; i < eigenValuesReal.length; i++) {
            varPercentage[i] = FastMath.abs(eigenValuesReal[i] / sum);
            sd[i] = FastMath.sqrt(FastMath.abs(eigenValuesReal[i]));
        }

        System.out.println("eigen values:\n"+toString(eigenValuesReal, dim));
        System.out.println("variance as percentage:\n"+toString(varPercentage, dim));
        System.out.println("standard deviation:\n"+toString(sd, dim));
        System.out.println("eigen vectors:\n"+toString(leftEigenVectors, dim));
        System.out.println("projection:\n"+toString(project(), dim));
    }

    private double[] project() {

        double[] projection = new double[data.length];
        int nRowsData = data.length / nCols;
        int nColsEigenVectors = nCols;

        for (int r = 0; r < nRowsData; r++) {
            for (int c = 0; c < nColsEigenVectors; c++) {
                for (int k = 0; k < nColsEigenVectors; k++) {
                    projection[r + c * nRowsData] += data[r + k * nRowsData] * leftEigenVectors[k + sortedIndices[c] * nColsEigenVectors];
                }
            }
        }
        return  projection;
    }

    private void toColumnMajorInPlace() {
        final int mn1 = data.length - 1;
        final boolean[] visited = new boolean[data.length];
        int c = 0;
        while (++c != data.length) {
            if (visited[c]) continue;
            int a = c;
            do {
                a = a == mn1 ? mn1 : (nRows * a) % mn1;
                swap(data, a, c);
                visited[a] = true;
            } while (a != c);
        }
    }

    /**
     * Swaps the values of two indices in the given array
     * @param d The array to swap values in
     * @param a The first index
     * @param b The second index
     */
    private void swap(double[] d, int a, int b) {
        double t = d[a];
        d[a] = d[b];
        d[b] = t;
    }

    /**
     * Calculate the mean of every column
     * @return An array of means
     */
    private double[] columnMeans() {
        double[] means = new double[nCols];
        for(int i = 0; i < data.length; i++) {
            int row = i % nRows, col = i / nRows;
            means[col] = (data[i] + row * means[col]) / (row + 1);
        }
        return means;
    }

    /**
     * Center the data around 0, will replace the input matrix with new values
     */
    private void center() {
        double[] means = columnMeans();
        for(int i = 0; i < data.length; i++) {
            data[i] -= means[i / nRows];
        }
    }

    /**
     * Calculate the covariance of an input matrix
     * @return A new square matrix of size nCols*nCols
     */
    private double[] covariance() {

        double[] covMatrix = new double[nCols*nCols];

        /*
        Note that at this point the data has been centered, which means that
        we do not have to subtract the column means as they are all 0
         */

        for(int col1 = 0; col1 < nCols; col1++) {
            for(int col2 = 0; col2 < nCols; col2++) {

                double cov = 0;
                for(int i = 0; i < nRows; i++) {
                    cov += (data[i + col1 * nRows] ) * (data[i + col2 * nRows] );
                }
                cov /= (nRows - 1);
                covMatrix[col1 + col2 * nCols] = cov;
            }
        }
        return covMatrix;
    }

}
