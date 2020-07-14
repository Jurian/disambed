package org.uu.nl.embedding.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.log4j.Logger;
import org.uu.nl.embedding.lensr.LensrModel;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

public class MatrixUtils {

    private final static Logger logger = Logger.getLogger(MatrixUtils.class);
	/*
	 * This class is primarily meant to use statically
	 */
	
	final Matrix matrixObj;
	
	final int rowLength, colLength;
	final boolean wasInt;
	final double[][] dblMat;
	final int[][] intMat;
		
	public MatrixUtils(double[][] inputMat) {
		this.wasInt = false;
		this.dblMat = inputMat;
		this.intMat = convertToInteger(inputMat);
		this.matrixObj = new Matrix(inputMat);
		this.rowLength = this.matrixObj.getRowDimension();
		this.colLength = this.matrixObj.getColumnDimension();
	}
	
	public MatrixUtils(int[][] inputMat) {
		this.wasInt = true;
		this.intMat = inputMat;
		this.dblMat = convertToDouble(inputMat);
		this.matrixObj = new Matrix(dblMat);
		this.rowLength = this.matrixObj.getRowDimension();
		this.colLength = this.matrixObj.getColumnDimension();
	}
	
	public MatrixUtils(Matrix inputMat) {
		this(inputMat.getArrayCopy());
	}
	
	/*
	 * Below: Matrix mathematical methods.
	 */

	/**
	 * Static method to calculate power matrix
	 * 
	 * @param matrix The matrix to be raised to the power of fraction
	 * @param pwr The power of the fraction
	 * @return Returns a power matrix
	 */
	public static Matrix power(Matrix matrix, final double pwr) {
		Matrix m;
		if(pwr == 0.0) {
			m = Matrix.identity(matrix.getRowDimension(), matrix.getColumnDimension());
			
		} else {
			double usePwr;
			if(pwr < 0.0) {
				// Make power positive and take inverse of the matrix
				usePwr = pwr * (-1);
				m = matrix.inverse();
			
			} else { m = matrix; usePwr = pwr; }
			
			try {
				EigenvalueDecomposition evd = new EigenvalueDecomposition(m);
				Matrix ev = evd.getV(); // Get eigenvector matrix
				Matrix dev = evd.getD(); // Get block diagonal eigenvalue matrix
				Matrix powDev = dev.copy(); // Place holder for dev to the power of fraction
				
				for(int i = 0; i < dev.getRowDimension(); i++) {
					powDev.set(i, i, Math.pow(dev.get(i, i), (double) (usePwr)));
				}
				m = ev.arrayTimes(powDev).times(ev.inverse());
				
			} catch (IllegalArgumentException e) { throw new IllegalArgumentException("Invalid argument(s)."); }
		}
		return m;
	}
	

    /**
     * @param M The matrix
     * @return sigmoid result on M
     */
    public static Matrix sigmoid(final Matrix M) {
        int r = M.getRowDimension();
        int c = M.getColumnDimension();
        double val;
        Matrix resultM = new Matrix(r, c);

        for (int i = 0; i < r; i++) {
            for (int j = 0; j < c; j++) {
            	val = (1.0 / (1 + Math.exp(-resultM.get(i, j))));
                resultM.set(i, j, val);
            
        }}
        return resultM;
    }
    
    /**
     * Derivative of the sigmoid function.
     * @param M
     * @return
     */
    public static Matrix sigmoidPrime(final Matrix M) {
    	//Element wise:
    	// sigmoid(z)*(1-sigmoid(z))
    	
    	Matrix onesM = onesMatrix(M.getRowDimension(), M.getColumnDimension());
    	return sigmoid(M).times(onesM.minus(sigmoid(M)));
    }
	
	/**
	 * Static method to calculate fractional power matrix
	 * 
	 * @param matrix The matrix to be raised to the power of fraction
	 * @param numerator The numerator of the fraction
	 * @param denominator The denominator of the fractions
	 * @return Returns a fractional power matrix
	 */
	public static Matrix fractionalPower(Matrix matrix, final int numerator, final int denominator) {
		/*
		 * ************************************
		 * Checken of dit helemaal goed gaat!
		 * ************************************
		 */
		
		Matrix m;
		boolean negativeDenom = false;
		if(denominator < 0) { negativeDenom = true; }
		
		if(numerator == 0) {
			m = Matrix.identity(matrix.getRowDimension(), matrix.getColumnDimension());
			
		} else {
			// Check for valid denominator
			if(denominator == 0) { throw new IllegalArgumentException("Denominator must be positive."); }
			
			int useNumerator;
			if((numerator < 0 && !negativeDenom) || (numerator > 0 && negativeDenom)) {
			
				// Make power positive and take inverse of the matrix
				useNumerator = numerator * (-1);
				m = matrix.inverse();
			
			} else { m = matrix; useNumerator = numerator; }
			
			try {
				EigenvalueDecomposition evd = new EigenvalueDecomposition(matrix);
				Matrix ev = evd.getV(); // Get eigenvector matrix
				Matrix dev = evd.getD(); // Get block diagonal eigenvalue matrix
				Matrix powDev = dev.copy(); // Place holder for dev to the power of fraction
				
				for(int i = 0; i < dev.getRowDimension(); i++) {
					powDev.set(i, i, Math.pow(dev.get(i, i), (double) (useNumerator/denominator)));
				}
				m = ev.arrayTimes(powDev).times(ev.inverse());
				
			} catch (IllegalArgumentException e) { throw new IllegalArgumentException("Invalid argument(s)."); }
		}
		return m;
	}
	
	/**
	 * Non-static method to calculate fractional power matrix
	 * 
	 * @param numerator The numerator of the fraction
	 * @param denominator The denominator of the fractions
	 * @return Returns a fractional power matrix
	 */
	public Matrix fractionalPower(final int numerator, final int denominator) {
		return fractionalPower(this.matrixObj, numerator, denominator);
	}


    /**
     * Calculates the dot product matrix of two input matrices.
     * 
     * @param M1 first matrix
     * @param M2 second matrix
     * @return resultM resulting matrix
     */
    public static Matrix dot(final Matrix M1, final Matrix M2) {
    	
        int row1 = M1.getRowDimension();
        int col1 = M1.getColumnDimension();
        int row2 = M2.getRowDimension();
        int col2 = M2.getColumnDimension();
        
        if (col1 != row2) {
            throw new RuntimeException("Matrix dimension do not match: " + col1 + " and " + row2);
        }
        
        double val;
    	Matrix resultM =  new Matrix(row1, col2);
        for (int i = 0; i < row1; i++) {
            for (int j = 0; j < col2; j++) {
                for (int k = 0; k < col1; k++) {
                	
                	val = resultM.get(i, j) + (M1.get(i, k) * M2.get(k, j));
                	resultM.set(i, j, val);
                
        }}}
        return resultM;
    }
    

    /**
     * Element wise cross-entropy division.
     *
     * @param A matrix
     * @param Y matrix
     * @param batch_size scaler
     * @return loss
     */
    public static double crossEntropy(int batch_size, double[][] Y, double[][] A) { // AANPASSEN
        int m = A.length;
        int n = A[0].length;
        double[][] z = new double[m][n];

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                z[i][j] = (Y[i][j] * Math.log(A[i][j])) + ((1 - Y[i][j]) * Math.log(1 - A[i][j]));
            }
        }

        double sum = 0;
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                sum += z[i][j];
            }
        }
        return -sum / batch_size;
    }
	
	
	/**
	 * 
	 * @param matrix
	 * @return
	 */
	public static Matrix abs(final Matrix matrix) {
		Matrix resultMat = new Matrix(matrix.getRowDimension(), matrix.getColumnDimension());
		
		for (int row = 0; row < matrix.getRowDimension(); row++) {
			for (int col = 0; col < matrix.getColumnDimension(); col++) {
				
				resultMat.set(row, col, Math.abs(matrix.get(row, col)));
		}}
		
		return resultMat;
	}
	
	/**
	 * 
	 * @param matrix
	 * @return
	 */
	public static double sum(final Matrix matrix) {
		double result = 0d;

		for (int row = 0; row < matrix.getRowDimension(); row++) {
			for (int col = 0; col < matrix.getColumnDimension(); col++) {
				
				result += matrix.get(row, col);
		}}
		
		return result;
	}
	
	/**
	 * 
	 * @param matrix
	 * @return
	 */
	public static int[] argmax(final Matrix matrix) {
		int[] maxCoords = new int[2];
		double maxVal = 0;
		
		for (int r = 0; r < matrix.getRowDimension(); r++) {
			for (int c = 0; c < matrix.getColumnDimension(); c++) {
				
				if (maxVal < matrix.get(r, c)) { 
					maxVal = matrix.get(r, c); 
					maxCoords[0] = r;
					maxCoords[1] = c;
				}
		}}
		return maxCoords;
	}
	
	/**
	 * 
	 * @param matrix
	 * @param d
	 * @return
	 */
	public static Matrix divideBy(final Matrix matrix, final double d) {
		Matrix resultMat = new Matrix(matrix.getRowDimension(), matrix.getColumnDimension());
		double res;

		for (int r = 0; r < matrix.getRowDimension(); r++) {
			for (int c = 0; c < matrix.getColumnDimension(); c++) {
				res = matrix.get(r, c) / d;
				resultMat.set(r, c, res);
		}}
		
		return resultMat;
	}
	
	/**
	 * 
	 * @param matrix
	 * @param i
	 * @return
	 */
	public static Matrix divideBy(final Matrix matrix, final int i) {
		return divideBy(matrix, (double) i);
	}

	/**
	 * 
	 * @param mat1
	 * @param mat2
	 * @return
	 */
	public static Matrix sqrdEuclidDistanceVec(final Matrix mat, final Matrix vec) {
		MatrixUtils.checkMultipMatrixDims(mat, vec);
		
		Matrix resVec = new Matrix(mat.getRowDimension(), vec.getColumnDimension());
		double dist;

		for (int r = 0; r < mat.getRowDimension(); r++) {
			dist = 0;
			for (int c = 0; c < mat.getColumnDimension(); c++) {
				dist += mat.get(r, c)-vec.get(r, 0);
			}
			resVec.set(r, 0, (dist*dist));
		}
		return resVec;
	}

	/**
	 * 
	 * @param mat1
	 * @param mat2
	 * @return
	 */
	public static Matrix euclidDistanceVec(final Matrix mat, final Matrix vec) {
		Matrix resVec = sqrdEuclidDistanceVec(mat, vec);
		
		double val;
		for (int r = 0; r < resVec.getRowDimension(); r++) {
			for (int c = 0; c < resVec.getColumnDimension(); c++) {
				val = resVec.get(r, c);
				resVec.set( r, c, (val*val) );
		}}
		return resVec;
	}
	
	/*
	 * Below: Matrix object manipulation methods.
	 * (Above: mathematical operations.)
	 */
	
	/**
	 * 
	 * @param m1
	 * @param m2
	 */
	public static void checkMatrixDims(final Matrix m1, final Matrix m2) {
		if (m1.getRowDimension() != m2.getRowDimension()) {
			logger.error("Row dimensions of matrices do not match.");
		}
		if (m1.getColumnDimension() != m2.getColumnDimension()) {
			logger.error("Column dimensions of matrices do not match.");
		}
	}
	
	/**
	 * 
	 * @param m1
	 * @param m2
	 */
	public static void checkMultipMatrixDims(final Matrix m1, final Matrix m2) {
		if (m1.getColumnDimension() != m2.getRowDimension()) {
			logger.error("Multiplication dimensions of matrices do not match.");
		}
	}
	
	/**
	 * 
	 * @param matrix
	 * @return
	 */
	public static Matrix getDegreeMatrix(final Matrix matrix) {
		Matrix D = new Matrix(matrix.getRowDimension(), matrix.getColumnDimension());
		
		int degree;
		for (int r = 0; r < matrix.getRowDimension(); r++) {
			// Reset the degree.
			degree = 0;
			for (int c = 0; c < matrix.getColumnDimension(); c++) {
				// Increment degree with found number.
				degree += matrix.get(r, c);
			}
			// Assign degree of current node.
			D.set(r, r, degree);
		}
		
		return D;
	}

	/**
	 * 
	 * @param matrix The matrix to get the diagonal from
	 * @return Returns the diagonal
	 */
	public static double[] getDiagonal(final Matrix matrix) {
		double[] diag = new double[matrix.getRowDimension()];
		for(int row = 0; row < matrix.getRowDimension(); row++) {
			diag[row] = matrix.get(row, row);
		}
		return diag;
	}
	
	/**
	 * 
	 * @param matrix The matrix to get the diagonal from
	 * @return Returns the diagonal matrix
	 */
	public static Matrix getDiagonalMatrix(final Matrix matrix) {
		Matrix diagM = new Matrix(matrix.getRowDimension(), matrix.getColumnDimension());
		
		for (int row = 0; row < matrix.getRowDimension(); row++) {
			for (int col = 0; col < matrix.getColumnDimension(); col++) {
				
				if (row == col) { diagM.set(row, col, matrix.get(row, col)); }
		}}
		return diagM;
	}
	
	/**
	 * 
	 * @param diag The diagonal to set in the new matrix.
	 * @return Returns the diagonal matrix
	 */
	public static Matrix getDiagonalMatrix(final double[] diag) {
		Matrix diagM = new Matrix(diag.length, diag.length);
		
		for (int row = 0; row < diag.length; row++) {
			for (int col = 0; col < diag.length; col++) {
				
				if (row == col) { diagM.set(row, col, diag[row]); }
		}}
		return diagM;
	}
	
	/**
	 * 
	 * @param rows
	 * @param cols
	 * @param nMatrices
	 * @return
	 */
	public static Matrix[] initMatrixArray(final int rows, final int cols, final int nMatrices) {
		Matrix[] mAr = new Matrix[nMatrices];
		
		for (int i = 0; i < nMatrices; i++) {
			mAr[i] = new Matrix(rows, cols);
		}
		return mAr;
	}
	
	/**
	 * 
	 * @param rows
	 * @param cols
	 * @param nMatrices
	 * @return
	 */
	public static Matrix[] initMatrixArray(final Matrix[] formatMatrices) {
		Matrix[] mAr = new Matrix[formatMatrices.length];
		
		for (int l = 0; l < formatMatrices.length; l++) {
			mAr[l] = new Matrix(formatMatrices[l].getRowDimension(), formatMatrices[l].getColumnDimension());
		}
		return mAr;
	}
	
	/**
	 * 
	 * @param rows
	 * @param cols
	 * @return
	 */
	public static Matrix randMatrix(final int rows, final int cols) {
		Matrix resultMat = new Matrix(rows, cols);
		Random rand = new Random();
		double randX;

		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {

				randX = rand.nextDouble();
				resultMat.set(r, c, randX);
		}}
		
		return resultMat;
	}
	
	/**
	 * 
	 * @param rows
	 * @param cols
	 * @return
	 */
	public static Matrix onesMatrix(final int rows, final int cols) {
    	Matrix onesM = new Matrix(rows, cols);
    	for (int r = 0; r < rows; r++) {
    		for (int c = 0; c < cols; c++) {
    			onesM.set(r, c, 1);
    	}}
    	return onesM;
	}
	
	/**
	 * 
	 * @param matrices
	 * @param dim
	 * @return
	 */
	public static Matrix concat(final Matrix[] matrices, final int dim) {
		Matrix resultMat;
		Matrix firstMat = matrices[0];
		int nRows = firstMat.getRowDimension();
		int nCols = firstMat.getColumnDimension();
		
		if (dim == 0) {
			resultMat = new Matrix( (nRows*matrices.length), nCols );
			
			for (int i = 0; i < matrices.length; i++) {
				
				checkMatrixDims(firstMat, matrices[i]);
				
				for (int r = 0; r < nRows; r++) {
					for (int c = 0; c < nCols; c++) {
						
						resultMat.set( ((i*nRows)+r), c, matrices[i].get(r, c) );
					}}
			}
		} 
		else if (dim == 1) {
			resultMat = new Matrix( nRows, (nCols*matrices.length) );
			boolean notAllChecked = true;
				
			for (int r = 0; r < nRows; r++) {

				for (int i = 0; i < matrices.length; i++) {
					for (int c = 0; c < nCols; c++) {
						
						if (notAllChecked) { checkMatrixDims(firstMat, matrices[i]); }
						
						resultMat.set(r, ((i*nCols)+c), matrices[i].get(r, c));
				}}
				notAllChecked = false;
			}
			
		} else { // Returns zero matrix.
			resultMat = new Matrix(nRows, nCols);
		}
		
		return resultMat;
	}

	/**
	 * 
	 * @param row
	 * @param col
	 * @return
	 */
	public static Matrix antiIdentity(final int row, final int col) {
		Matrix subResult = Matrix.identity(row, col);
		Matrix res = new Matrix(row, col);
		
		for(int c = 0; c < col; c++) {
			for(int r = 0; r < row; r++) {
				if(subResult.get(r, c) == 0.0) { res.set(r, c, 1.0); }
				else if(subResult.get(r, c) == 1.0) { res.set(r, c, 0.0); }
				else { throw new IllegalArgumentException("Invalid value in Identity matrix"); }
			}
		}
		return res;
	}
	
	/**
	 * Shuffle matrix array order using the Fisher–Yates shuffle
	 * method.
	 * @param matrixArr matrix array to be shuffled
	 */
	public static void shuffleArray(Matrix[] matrixArr)
	  {
	    // If running on Java 6 or older, use `new Random()` on RHS here
	    Random rnd = ThreadLocalRandom.current();
	    for (int i = matrixArr.length - 1; i > 0; i--)
	    {
	      int index = rnd.nextInt(i + 1);
	      // Simple swap
	      Matrix a = matrixArr[index];
	      matrixArr[index] = matrixArr[i];
	      matrixArr[i] = a;
	    }
	  }

	public static void shuffleDataset(ArrayList<HashMap<Matrix, Matrix>> dataset)
	  {
	    // If running on Java 6 or older, use `new Random()` on RHS here
	    Random rnd = ThreadLocalRandom.current();
	    HashMap<Matrix, Matrix> map;
	    for (int i = dataset.size() - 1; i > 0; i--)
	    {
	      int index = rnd.nextInt(i + 1);
	      // Simple swap
	      map = dataset.get(index);
	      dataset.set(index, dataset.get(i));
	      dataset.set(i, map);
	    }
	  }
	
	/**
	 * 
	 * @param matrix
	 * @return
	 */
	public static Matrix antiIdentity(Matrix matrix) {
		return antiIdentity(matrix.getRowDimension(), matrix.getColumnDimension());
	}
	
	/**
	 * 
	 * @param mat A 2D int array to convert to 2D double
	 * @return Returns a 2D double array
	 */
	private double[][] convertToDouble(int[][] mat) {
		double[][] resMat = new double[mat.length][mat[0].length];
		
		for(int row = 0; row < mat.length; row++) {
			for(int col = 0; col < mat[row].length; col++) {
				resMat[row][col] = (double) mat[row][col];
			}
		}
		return resMat;
	}

	/**
	 * 
	 * @param mat A 2D double array to convert to 2D int
	 * @return Returns a 2D int array
	 */
	private int[][] convertToInteger(double[][] mat) {
		int[][] resMat = new int[mat.length][mat[0].length];
		
		for(int row = 0; row < mat.length; row++) {
			for(int col = 0; col < mat[row].length; col++) {
				resMat[row][col] = (int) mat[row][col];
			}
		}
		return resMat;
	}
	

}