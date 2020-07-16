package org.uu.nl.embedding.lensr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.uu.nl.embedding.lensr.utils.ActivationFunction;
import org.uu.nl.embedding.util.ArrayUtils;
import org.uu.nl.embedding.util.MatrixUtils;

import Jama.Matrix;

public class LensrRun {
	
	private LensrModel model;
	private boolean hasIndepWeights;
	private ActivationFunction af;
	private int nHiddenLayers;
	private int nLayerNeurons;
	private int embedDim;
	private double lambda;
	private double lambda_r;
	private double margin;
	
	public LensrRun(final LensrModel model, final boolean hasIndepWeights, final int nHiddenLayers, final int nLayerNeurons,
					final int embedDim, final double lambda, final double lambda_r, final double margin, 
					final int nVars, final int maxD) {
		
		// Set own model
		this.model = model;
		
		// Set independent weights.
		this.hasIndepWeights = hasIndepWeights;
		
		// Model specifications.
		this.nHiddenLayers = nHiddenLayers;
		this.nLayerNeurons = nLayerNeurons;
		this.embedDim = embedDim;
		
		// Hyper-parameters.
		this.lambda = lambda;
		this.lambda_r = lambda_r;
		this.margin = margin;
		
		// Formulae complexity.
		int nVariables = nVars; // or 6, n_v
		int maxDepth = maxD; // or 6, d_m
		
		// Activation function.
		af = new ActivationFunction();
	}
	
	private Matrix forwardProp(final Matrix input, final String activation, final int layer, final int[] labels) {
		Matrix output, supportMat, resultMat;
		boolean emptySupport = true; // WAAROM SUPPORT MATRIX????????????????
		
		if (this.hasIndepWeights) {
			// Faux initialization.
			resultMat = new Matrix(1, 1);
			for (int i = 0; i < labels.length; i++) {
				
				if (labels[i] == 0) { // Global node.
					resultMat = input.times(model.Wglobal[i]);
					resultMat = resultMat.plus(model.Bglobal[i]); }
				else if (labels[i] == 1) { // Leaf node.
					resultMat = input.times(model.Wleaf[i]);
					resultMat = resultMat.plus(model.Bleaf[i]); }
				else if (labels[i] == 2) { // OR node.
					resultMat = input.times(model.Wor[i]);
					resultMat = resultMat.plus(model.Bor[i]); }
				else if (labels[i] == 3) { // AND node.
					resultMat = input.times(model.Wand[i]);
					resultMat = resultMat.plus(model.Band[i]); }
				else if (labels[i] == 4) { // NOT node.
					resultMat = input.times(model.Wnot[i]);
					resultMat = resultMat.plus(model.Bnot[i]); }
				else {
					resultMat = input.arrayTimes(model.defaultWeights[i]);
					resultMat = resultMat.plus(model.defaultBiases[i]);
				}
			}
			
			if (emptySupport) { supportMat = resultMat; }
			else { 
				// Faux initialization.
				supportMat = new Matrix(1, 1);
				supportMat = MatrixUtils.concat(new Matrix[] {supportMat, resultMat}, 0); }
			
		} else {
			supportMat = input.times(model.defaultWeights[0]);
		}
		
		output = af.sigmoid(supportMat);
		return output;
		
	}
	
	private void SGD(final ArrayList<HashMap<Matrix, Matrix>> trainData, final ArrayList<HashMap<Matrix, Matrix>> testData, final int epochs, final int batchSize, final double eta) {
		/*
		 * Layer moet nog naar worden gekeken!!!!!
		 */
		final int layer = 0;
		/*
		 * 
		 */
		
		int dataLen = trainData.size();
		int[] range;
		ArrayList<ArrayList<HashMap<Matrix, Matrix>>> miniBatches;
		
		// For each epoch, shuffle the training dataset
		// and divide into mini-batches.
		for (int epoch = 0; epoch < epochs; epoch++) {
			MatrixUtils.shuffleDataset(trainData);
			miniBatches = ArrayUtils.getRangeSteps(trainData, 0, dataLen, batchSize);
			
			// Do gradient descent per batch.
			for (ArrayList<HashMap<Matrix, Matrix>> miniBatch : miniBatches) {
				updateMiniBatch(miniBatch, layer, eta);
			}
			
			// If test dataset is given:
			// evaluate network against test dataset after
			// each epoch and print partial process for tracking.
			if (testData.size() > 0) {
				System.out.println("Epoch " + epoch + ": " + evaluate(testData) + " / " + testData.size());
			} else {
				System.out.println("Epoch " + epoch + " completed.");
			}
		}
	}
	
	/**
	 * 
	 * @param miniBatch
	 * @param eta
	 */
	private void updateMiniBatch(ArrayList<HashMap<Matrix, Matrix>> miniBatch, final int layer, final double eta) {
		final Matrix[] defWeights = model.defaultWeights;
		final Matrix[] defBiases = model.defaultBiases;
		int wRows = defWeights[0].getRowDimension();
		int wCols = defWeights[0].getColumnDimension();
		int bRows = defBiases[0].getRowDimension();
		int bCols = defBiases[0].getColumnDimension();
		Matrix newWeights = new Matrix(wRows, wCols);
		Matrix newBiases = new Matrix(wRows, wCols);
		double w, b, dw, db, nw, nb;
		
		Matrix input, answer;
		Matrix[][] dNablaTuple = new Matrix[2][];
		Matrix[] nablaW, nablaB, dNablaW, dNablaB;
		
		nablaW = MatrixUtils.initMatrixArray(defWeights);
		nablaB = MatrixUtils.initMatrixArray(defBiases);
		
		int mapSize;
		for (HashMap<Matrix, Matrix> map : miniBatch) {
			mapSize = 0;
			for (Map.Entry<Matrix, Matrix> data : map.entrySet()) {
				input = data.getKey();
				answer = data.getValue();
				
				// get delta nabla tuple.
				dNablaTuple = backProp(input, answer, layer);
				dNablaW = dNablaTuple[0];
				dNablaB = dNablaTuple[1];
				
				// Update each layer of both weights and biases.
				for (int l = 0; l < dNablaW.hashCode(); l++) {
					nablaW[l] = nablaW[l].plus(dNablaW[l]);
				}
				for (int l = 0; l < dNablaB.hashCode(); l++) {
					nablaB[l] = nablaB[l].plus(dNablaB[l]);
				}
				// Increment mapSize for check.
				mapSize++;
			}
			if (mapSize > 1) { System.out.println("WARNING: mini-batch map size > 1 (LensrRun class)."); }
			
			for (int l = 0; l < model.nLayers; l++) {
				// reset matrices.
				wRows = defWeights[l].getRowDimension();
				wCols = defBiases[l].getColumnDimension();
				bRows = defBiases[l].getRowDimension();
				bCols = defBiases[l].getColumnDimension();
				newWeights = new Matrix(wRows, wCols);
				newBiases = new Matrix(bRows, bCols);
				
				for (int r = 0; r < wRows; r++) {
					for (int c = 0; c < wCols; c++) {
						dw = defWeights[l].get(r, c);
						nw = nablaW[l].get(r, c);
						db = defBiases[l].get(r, c);
						
						w = (dw - (eta/miniBatch.size())*nw);
						newWeights.set(r, c, w);
				}}
				for (int r = 0; r < bRows; r++) {
					for (int c = 0; c < bCols; c++) {
						db = defBiases[l].get(r, c);
						nb = nablaB[l].get(r, c);
						
						b = (db - (eta/miniBatch.size())*nb);
						newBiases.set(r, c, b);
				}}
				
				// Set new weight and bias.
				model.defaultWeights[l] = newWeights.copy();
				model.defaultBiases[l] = newBiases.copy();
			}
		}
	}
	
	private Matrix[][] backProp(final Matrix input, final Matrix answer, final int layer) {
		Matrix[][] dNablaTuple = new Matrix[2][]; // 0 = dNablaW, 1 = dNablaB
		final Matrix[] defWeights = model.defaultWeights;
		final Matrix[] defBiases = model.defaultBiases;
		
		Matrix[] nablaW = new Matrix[defWeights.length];
		Matrix[] nablaB = new Matrix[defBiases.length];
		for (int l = 0; l < nablaW.length; l++) {
			nablaW[l] = new Matrix(defWeights[l].getRowDimension(), defWeights[l].getColumnDimension());
			nablaB[l] = new Matrix(defBiases[l].getRowDimension(), defBiases[l].getColumnDimension());
		}
		
		// Feedforward.
		Matrix activation = input.copy();
		ArrayList<Matrix> activations = new ArrayList<Matrix>(); // List to store all the activations by layer.
		activations.add(activation);
		ArrayList<Matrix> vectors = new ArrayList<Matrix>(); // List to store all the resulting vectors by layer.
		
		Matrix vec;
		for (int l = 0; l < nablaW.length; l++) {
			vec = defWeights[l].times(activation).plus(defBiases[l]);
			vectors.add(vec);
			activation = MatrixUtils.sigmoid(vec);
			activations.add(activation);
		}
		
		// Backward pass.
		Matrix deltaVec = activations.get(activations.size()-1).copy().minus(answer);
		deltaVec = deltaVec.arrayTimes( MatrixUtils.sigmoidPrime(vectors.get(vectors.size()-1)) );
		nablaW[model.nLayers-1] = deltaVec.times( activations.get(activations.size()-2).transpose() ).copy();
		nablaB[model.nLayers-1] = deltaVec.copy();
		
		Matrix deriv; // derivative of Sigmoid of the vector.
		Matrix tempMat;
		for (int l = model.nLayers-2; l >= 0; l--) {
			vec = vectors.get(l);
			deriv = MatrixUtils.sigmoidPrime(vec);
			tempMat = defWeights[l+1].copy().transpose();
			deltaVec = tempMat.times(deltaVec).arrayTimes(deriv);
			
			// Add result to nabla layers.
			nablaB[l] = deltaVec.copy();
			tempMat = activations.get(l-1).copy().transpose();
			nablaW[l] = deltaVec.times(tempMat);
		}
		
		dNablaTuple[0] = nablaW;
		dNablaTuple[1] = nablaB;
		
		return dNablaTuple;
	}
	
	/**
	 * 
	 * @param input
	 * @return
	 */
	public Matrix feedForward(final Matrix input) {
		Matrix resultMat = input.copy();
		final Matrix[] defWeights = model.defaultWeights;
		final Matrix[] defBiases = model.defaultBiases;
		
		// Update the input given the weights and biases of
		// each layer in the network. And return the result.
		for (int l = 0; l < model.nHiddenLayers; l++) {
			resultMat = MatrixUtils.sigmoid(defWeights[l].times(resultMat).plus(defBiases[l]));
		}
		return resultMat;
	}
	
	/**
	 * 
	 * @param testSet
	 * @return
	 */
	private int evaluate(ArrayList<HashMap<Matrix, Matrix>> testSet) {
		ArrayList<HashMap<int[], int[]>> testResults = new ArrayList<HashMap<int[], int[]>>();
		int[] result = new int[2];
		int[] answer = new int[2];
		
		// Run all test cases through the network and save
		// it as a tuple with the correct answer for that case.
		int mapSize = 0;
		HashMap<int[], int[]> resMap;
		for (HashMap<Matrix, Matrix> map : testSet) {
			mapSize = 0;
			for (Map.Entry<Matrix, Matrix> data : map.entrySet()) {
				result = MatrixUtils.argmax(feedForward(data.getKey()));
				answer = MatrixUtils.argmax(data.getValue());
				resMap = new HashMap<int[], int[]>();
				resMap.put(result, answer);
				testResults.add(resMap);
			}
			mapSize++;
		}
		if (mapSize > 1) { System.out.println("WARNING: mini-batch map size > 1 (LensrRun class)."); }
		
		// Count number of correctly answered test cases and return it.
		int correctAsnwers = 0;
		for (HashMap<int[], int[]> map : testResults) {
			for (Map.Entry<int[], int[]> data : map.entrySet()) {
				
				if (data.getKey()[0] == data.getValue()[0] && data.getKey()[1] == data.getValue()[1]) {
					correctAsnwers++;
				}
		}}
		return correctAsnwers;
	}

}
