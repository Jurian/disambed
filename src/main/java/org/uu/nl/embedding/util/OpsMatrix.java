package org.uu.nl.embedding.util;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

public class OpsMatrix {
	/*
	 * This class is primarily meant to use statically
	 */
	
	final Matrix matrixObj;
	
	final int rowLength, colLength;
	final boolean wasInt;
	final double[][] dblMat;
	final int[][] intMat;
		
	public OpsMatrix(double[][] inputMat) {
		this.wasInt = false;
		this.dblMat = inputMat;
		this.intMat = convertToInteger(inputMat);
		this.matrixObj = new Matrix(inputMat);
		this.rowLength = this.matrixObj.getRowDimension();
		this.colLength = this.matrixObj.getColumnDimension();
	}
	
	public OpsMatrix(int[][] inputMat) {
		this.wasInt = true;
		this.intMat = inputMat;
		this.dblMat = convertToDouble(inputMat);
		this.matrixObj = new Matrix(dblMat);
		this.rowLength = this.matrixObj.getRowDimension();
		this.colLength = this.matrixObj.getColumnDimension();
	}
	
	public OpsMatrix(Matrix inputMat) {
		this(inputMat.getArrayCopy());
	}

	/**
	 *  OLD METHOD: Do not use.
	 * @param matrix
	 * @param pwr
	 * @return
	 */
	public static Matrix powerOLD(Matrix matrix, final int pwr) {
		/*
		 * Misschien nog versnellen met Identity Matrix trucje?
		 * --> Ja; Zie power() hieronder
		 */
		try {
			
			if(pwr == 0) { return matrix; } 
			else { return OpsMatrix.powerOLD(matrix.times(matrix), pwr-1); }
			
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Power must be non-negative");
		}
	}

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
	 * 
	 * @param matrix The matrix to get the diagonal from
	 * @return Returns the diagonal
	 */
	public static double[] getDiagonal(Matrix matrix) {
		double[] diag = new double[matrix.getRowDimension()];
		for(int row = 0; row < matrix.getRowDimension(); row++) {
			diag[row] = matrix.get(row, row);
		}
		return diag;
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