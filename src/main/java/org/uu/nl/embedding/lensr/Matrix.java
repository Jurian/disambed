package org.uu.nl.embedding.lensr;

public class Matrix {
	
	final int rowLength, colLength;
	final boolean wasInt;
	final double[][] matrix;
	
	public Matrix(double[][] matrix) {
		this.wasInt = false;
		checkDimensions(matrix);
		
		this.matrix = matrix;
		this.rowLength = matrix.length;
		this.colLength = matrix[0].length;
	}
	
	public Matrix(int[][] matrix) {
		this.wasInt = true;
		this.matrix = convertToDouble(matrix);
		
		this.rowLength = matrix.length;
		this.colLength = matrix[0].length;
	}
	
	
	
	private double[][] convertToDouble(int[][] mat) {
		int minLen = mat[0].length;
		int maxLen = mat[0].length;
		boolean sameLength = true;
		
		double[][] resMat = new double[mat.length][mat[0].length];
		
		while(sameLength) {
			for(int row = 0; row < mat.length; row++) {
				// Check lengths
				if(minLen > mat[row].length) {
					minLen = mat[row].length;
				}
				if(maxLen < mat[row].length) {
					maxLen = mat[row].length;
				}
				if(minLen != maxLen) {
					sameLength = false;
				}

				// Throw exception if dimensions are wrong
				if(!sameLength) {
			        throw new IllegalArgumentException("Matrix rows have different dimensions.");
				}
				
				for(int col = 0; col < mat[row].length; col++) {
					resMat[row][col] = Double.valueOf(mat[row][col]);
				}
			}
		}
		return resMat;
	}
	
	private void checkDimensions(double[][] mat) {
		int minLen = mat[0].length;
		int maxLen = mat[0].length;
		boolean sameLength = true;
		
		while(sameLength) {
			for(int row = 0; row < mat.length; row++) {
				if(minLen > mat[row].length) {
					minLen = mat[row].length;
				}
				if(maxLen < mat[row].length) {
					maxLen = mat[row].length;
				}
				if(minLen != maxLen) {
					sameLength = false;
				}
			}
		}
		
		if(!sameLength) {
	        throw new IllegalArgumentException("Matrix rows have different dimensions.");
		}
	}

}
