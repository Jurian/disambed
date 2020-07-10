package org.uu.nl.embedding.lensr.utils;

import java.util.HashMap;
import java.util.Map;

import Jama.Matrix;

public class ActivationFunction {
	
	HashMap<Integer, String> activMap;
	
	public ActivationFunction() {
		activMap = new HashMap<Integer, String>() {{
	        put(0, "sigmoid");
	        put(1, "adam");
		}};
	}
	
	/**
	 * Element-wise Sigmoid function over input matrix.
	 * @param input Matrix
	 * @return result of element-wise Sigmoid function
	 */
	public Matrix sigmoid(final Matrix input) {
		int nRows = input.getRowDimension();
		int nCols = input.getColumnDimension();
		
		Matrix resultMat = new Matrix(nRows, nCols);
		
		for (int r = 0; r < nRows; r++) {
			for (int c = 0; c < nCols; c++) {
				
				resultMat.set(r, c, sigmoid(input.get(r, c)));
		}}
		return resultMat;
	}
	
	/**
	 * Sigmoid function over x.
	 * @param x Double value
	 * @return result of Sigmoid function over x
	 */
	private double sigmoid(final double x) {
		/*
		 * 1.0/(1.0+np.exp(-z))
		 */
		double result = Math.exp(-x);
		result += 1d;
		result = 1d/(result);
		
		return result;
	}
	
	
	/*
	 * Extra utilities.
	 */
	public String getFunction(final int idx) {
		return this.activMap.get(idx);
	}

	public int getIndex(final String funcName) {
		for (Map.Entry<Integer, String> entry : this.activMap.entrySet()) {
			if (entry.getValue() == funcName) { return entry.getKey(); }
		}
		return -1;
	}
}
