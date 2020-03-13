package org.uu.nl.embedding.pca;

import com.github.fommil.netlib.LAPACK;
import me.tongfei.progressbar.ProgressBar;
import org.apache.commons.math.util.FastMath;
import org.netlib.util.intW;
import org.uu.nl.embedding.util.config.Configuration;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

/**
 * <p>
 *  Principal component analysis (PCA) is a statistical procedure that uses an orthogonal transformation to convert a set
 *  of observations of possibly correlated variables (entities each of which takes on various numerical values) into a
 *  set of values of linearly uncorrelated variables called principal components.
 * </p>
 *
 * <p>
 *  This class is useful because it turns out that most of the time, the vast majority of principal components can
 *  be discarded while keeping 95% of variance.
 * </p>
 *
 * @author Jurian Baas
 */
public class PCA {

    static {
        // Tell the LAPACK logger to shut up
        final Logger logger = Logger.getLogger(LAPACK.class.getName());
        logger.setLevel(Level.OFF);
    }

    private static final DecimalFormat df = new DecimalFormat("####0.0000");

    /**
     * Write a matrix as a pretty String
     * @param data The data of the matrix
     * @param nCols The number of columns of the matrix
     * @return Formatted String
     */
    private static String toStringMatrix(double[] data, int nCols) {

        StringBuilder out = new StringBuilder(data.length * 7 + nCols * 2);

        for(int i = 0; i < data.length; i ++) {
            out.append(" ").append(df.format(data[i]));
            if((i % nCols) == nCols-1) out.append("\n");
        }

        return out.toString();
    }

    /**
     * Create a String of a vector ordered to the eigen values
     * @param vector The vector to create a String version of
     * @return The vector but ordered to the eigen values
     */
    private String toStringOrderedVector(double[] vector) {
        StringBuilder out = new StringBuilder(vector.length * 7 );

        for(int i = 0; i < vector.length; i ++) {
            out.append(" ").append(df.format(vector[sortedIndices[i]]));
        }

        return out.toString();
    }

    /**
     * Contains the real and imaginary parts of the eigenvalues
     */
    private final double[] eigenValuesReal;
    private final double[] varPercentage;
    private final double[] sd;
    private final double[] data;
    private final double[] leftEigenVectors;
    private final int nRows, nCols, numThreads;
    private int[] sortedIndices;


    /**
     * Calculate all necessary prerequisites for projecting the input matrix into a lower dimensional space
     * @param vectors Input matrix
     * @param inPlace Whether to overwrite the input matrix (true) or create a copy (false)
     */
    public PCA(double[] vectors, boolean inPlace, Configuration config)  {

        final int dim = config.getDim();

        assert vectors.length != 0;
        assert vectors.length % dim == 0;

        this.numThreads = config.getThreads();

        this.nCols = dim;
        this.nRows = vectors.length / dim;

        // Allocate space for the decomposition
        eigenValuesReal = new double[nCols];
        double[] eigenValuesImaginary = new double[nCols];
        sd = new double[nCols];
        varPercentage = new double[nCols];
        leftEigenVectors = new double[nCols * nCols];

        if(inPlace)
            this.data = vectors;
        else
            this.data = Arrays.copyOf(vectors, vectors.length);

        center(this.data, nCols);
        double[] covarianceMatrix = covariance();

        // Find the needed workspace
        double[] workSize = new double[1];
        intW info = new intW(0);
        int ld = FastMath.max(1, nCols);
        LAPACK.getInstance().dgeev("V", "N", nCols,
                new double[0], ld, new double[0], new double[0],
                new double[0], ld, new double[0], ld,
                workSize, -1, info);

        // Allocate workspace
        int workSpace;
        if (info.val != 0) {
            workSpace = 4 * nRows;
        } else
            workSpace = (int) workSize[0];

        workSpace = Math.max(1, workSpace);
        double[] work = new double[workSpace];

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
        for (double v : eigenValuesReal) sum += FastMath.abs(v);

        for(int i = 0; i < eigenValuesReal.length; i++) {
            varPercentage[i] = FastMath.abs(eigenValuesReal[i] / sum);
            sd[i] = FastMath.sqrt(FastMath.abs(eigenValuesReal[i]));
        }

    }

    @Override
    public String toString() {

        return
                "Variance as percentage:\n" + toStringOrderedVector(varPercentage) +
                "\n\nStandard deviation:\n" + toStringOrderedVector(sd) +
                "\n\nEigen-values:\n" + toStringOrderedVector(eigenValuesReal);
                //"\nEigen-vectors:\n" + toStringMatrix(leftEigenVectors, nCols);
    }

    /**
     * Project the data using the pre-calculated eigenvectors
     * @param minVariance Used to determine the number of principal components that have to be used
     * @return The projected matrix
     */
    public Projection project(double minVariance) {

        final int maxEigenCols = getCumulativeVarianceIndex(minVariance);

        final double[] projection = new double[nRows * maxEigenCols];
        final int nRowsData = data.length / nCols;
        final int nColsEigenVectors = nCols;

        // Note that LAPACK returns the eigenvectors in transposed fashion
        // So we have to keep that in mind while indexing
        final ExecutorService es = Executors.newWorkStealingPool(numThreads);
        final CompletionService<Void> cs = new ExecutorCompletionService<>(es);

        try(ProgressBar pb = Configuration.progressBar("Projecting", maxEigenCols, "columns")) {
            for (int c = 0; c < maxEigenCols; c++) {
                final int constC = c;
                cs.submit(() -> {
                    for (int r = 0; r < nRowsData; r++) {
                        for (int k = 0; k < nColsEigenVectors; k++) {
                            projection[constC + r * maxEigenCols] +=
                                    data[k + r * nCols] * leftEigenVectors[k + sortedIndices[constC] * nColsEigenVectors];
                        }
                    }
                    return null;
                });
            }

            int done = 0;
            while(done < maxEigenCols) {
                cs.take();
                done++;
                pb.step();
            }

        } catch(InterruptedException e) {
            e.printStackTrace();
        } finally {
            es.shutdown();
        }

        //centerAroundMedian(projection, maxEigenCols);

        return new Projection(projection, maxEigenCols);
    }

    /**
     * Calculate the number of columns (principal components) we need to include in the projection
     * in order to explain at least the minimum variance given as the argument. If the number is
     * lower than 3, the number of components returned is 3 to make visualization possible.
     * @param minVariance The minimum variance
     * @return A number ranging from 3 and the number of eigenvalues + 1
     */
    private int getCumulativeVarianceIndex(double minVariance) {
        double cumulativeVariance = 0;
        int i = 0;
        while(cumulativeVariance < minVariance) {
            cumulativeVariance += varPercentage[sortedIndices[i++]];
        }
        return FastMath.max(i, 3);
    }

    /**
     * Calculate the mean of every column
     * @return An array of means
     */
    private double[] colMeans(double[] data, int nCols) {
        double[] means = new double[nCols];
        for(int i = 0; i < data.length; i++) {
            int row = i / nCols, col = i % nCols;
            means[col] = (data[i] + row * means[col]) / (row + 1);
        }
        return means;
    }

    /**
     * Center the data around 0, will replace the data matrix with new values
     */
    private void center(double[] data, int nCols) {
        double[] means = colMeans(data, nCols);
        for(int i = 0; i < data.length; i++) {
            data[i] -= means[i % nCols];
        }
    }

    private void centerAroundMedian(double[] data, int nCols) {
        double[] medians = colMedians(data, nCols);
        for(int i = 0; i < data.length; i++) {
            data[i] -= medians[i % nCols];
        }
    }

    private double[] colMedians(double[] data, int nCols) {
        double[] medians = new double[nCols];
        int nRows = data.length / nCols;
        boolean even = nRows % 2 == 0;
        int mid = nRows / 2;

        for(int c = 0; c < nCols; c++) {

            double[] d = new double[nRows];

            for(int r = 0; r < nRows; r++) {
                d[r] = data[r * nCols + c];
            }
            Arrays.sort(d);

            medians[c] = even ? (d[mid]+d[mid+1]) / 2 : d[mid];
        }

        return medians;
    }

    /**
     * Calculate the covariance of an input matrix
     * @return A new square matrix of size nCols*nCols
     */
    private double[] covariance() {

        double[] covMatrix = new double[nCols*nCols];

        // Note that at this point the data has been centered, which means that
        // we do not have to subtract the column means (as they are all 0)
        final ExecutorService es = Executors.newWorkStealingPool(numThreads);
        final CompletionService<Void> cs = new ExecutorCompletionService<>(es);

        try(ProgressBar pb = Configuration.progressBar("Covariance Matrix", nCols, "columns")) {

            for(int col1 = 0; col1 < nCols; col1++) {
                final int constCol1 = col1;
                cs.submit(() -> {
                    for(int col2 = 0; col2 < nCols; col2++) {

                        if(constCol1 < col2) continue;

                        double cov = 0;
                        int offset;
                        for(int i = 0; i < nRows; i++) {
                            offset = i * nCols;
                            cov += data[offset + constCol1] * data[offset + col2] ;
                        }
                        cov /= (nRows - 1);
                        covMatrix[constCol1 + col2 * nCols] = cov;
                        covMatrix[col2 + constCol1 * nCols] = cov;
                    }
                    return null;
                });
            }

            int done = 0;
            while(done < nCols) {
                cs.take();
                done++;
                pb.step();
            }

        } catch(InterruptedException e) {
            e.printStackTrace();
        } finally {
            es.shutdown();
        }


        return covMatrix;
    }

    /**
     * Wrapper for a projection, we need to store the data and also the number of columns
     */
    public static class Projection {

        final double[] projection;
        final int nCols;

        private Projection(double[] projection, int nCols) {
            this.projection = projection;
            this.nCols = nCols;
        }

        @Override
        public String toString() {
            return PCA.toStringMatrix(projection, nCols);
        }

        public double[] getProjection() {
            return projection;
        }

        public int getnCols() {
            return nCols;
        }
    }


}
