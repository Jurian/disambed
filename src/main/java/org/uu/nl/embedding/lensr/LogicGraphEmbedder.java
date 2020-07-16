package org.uu.nl.embedding.lensr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.uu.nl.embedding.lensr.utils.ActivationFunction;
import org.uu.nl.embedding.lensr.utils.LensrUtils;
import org.uu.nl.embedding.lensr.utils.VectorUtils;
import org.uu.nl.embedding.logic.cnf.CnfFormula;
import org.uu.nl.embedding.logic.ddnnf.DdnnfLogicRule;
import org.uu.nl.embedding.logic.util.LogicRuleSet;
import org.uu.nl.embedding.util.MatrixUtils;

import Jama.Matrix;

public class LogicGraphEmbedder {
	
	// Activation function
	protected ActivationFunction af;
	// layerWisePropagationRule variables
	public int nLayers, numNodes, numInputFeats;
	public Matrix[] Zleaf, Zglobal, Zand, Zor, Znot; // Z(l) = l times 1xn learnt latent node embedding at different layers
	public Matrix ATilde; // A tilde = 2d adjacency matrix with self-connections
	public Matrix DTilde; // 2d diagonal degree matrix with self-connections
	public Matrix poweredDTilde; // D = 2d DTilde matrix to power of -1/2
	public Matrix DADTilde; // poweredDTilde dot ATilde dot poweredDTilde
	public Matrix[] Wleaf, Wglobal, Wand, Wor, Wnot; // W(l) = l times nxn layer-specific trainable weight matrix.
	public Matrix[] Bleaf, Bglobal, Band, Bor, Bnot; // bias(l) = l times nxn layer-specific bias matrix.
	
	// Formula variables
	protected LogicRuleSet formulaSet;
	protected DdnnfGraph graph;
	protected CnfFormula cnf;
	protected DdnnfLogicRule ddnnf;
	ArrayList<Integer> orNodesF, andNodesF;
	
	// Utility variables
	protected HashMap<String, Integer> typeIndices;
	protected HashMap<Integer, String> indicesTypes;
	
	/*
	 * Firstly, the constructor method and all 
	 * constructor-related methods.
	 */
	
	/**
	 * Constructor method.
	 * @param graph
	 */
	public LogicGraphEmbedder(final LogicRuleSet formulaSet, final ActivationFunction af) {
		this.formulaSet = formulaSet;
		this.graph = formulaSet.getGraph();
		this.cnf = formulaSet.getCnfFormula();
		this.ddnnf = formulaSet.getDdnnfRule();
		this.orNodesF = LensrUtils.getOrNodes(this.cnf);
		this.andNodesF = LensrUtils.getAndNodes(this.cnf);
		
		generateBaseMatrices(this.graph);
		initLayers();
		initTypeIndices();
		this.af = af;
		
	}
	
	/**
	 * 
	 */
	private void initTypeIndices() {
		typeIndices = new HashMap<String, Integer>();
		
		typeIndices.put("AND", 0);
		typeIndices.put("OR", 1);
		typeIndices.put("NOT", 2);
		typeIndices.put("Leaf", 3);
		typeIndices.put("Global", 4);

		indicesTypes = new HashMap<Integer, String>();
		
		indicesTypes.put(0, "AND");
		indicesTypes.put(1, "OR");
		indicesTypes.put(2, "NOT");
		indicesTypes.put(3, "Leaf");
		indicesTypes.put(4, "Global");
	}
	
	/**
	 * Converts a graph into a adjacency matrix with
	 * added self-connections (using an identity matrix). 
	 * @param graph
	 * @return
	 */
	public Matrix graphToMatrix(final DdnnfGraph graph) {
		HashMap<Integer, DdnnfGraph> graphMap = graph.getIntGraphMap();
		this.numNodes = graphMap.size();
		final int nNodes = graphMap.size();
		
		Matrix A = new Matrix(nNodes, nNodes);
		
		// Fill adjacency matrix (undirected).
		HashMap<Integer, DdnnfGraph> childNodes;
		for (Map.Entry<Integer, DdnnfGraph> mapParent : graphMap.entrySet()) {
			childNodes = mapParent.getValue().getChildGraphs();
			
			for (Map.Entry<Integer, DdnnfGraph> mapChild : childNodes.entrySet()) {
				A.set(mapParent.getKey(), mapChild.getKey(), 1);
				A.set(mapChild.getKey(), mapParent.getKey(), 1);
			}
		}
		// Add identity matrix.
		Matrix I = Matrix.identity(nNodes, nNodes);
		A = A.plus(I);
		return A; // Return ATilde.
	}
	
	/**
	 * 
	 * @param graph
	 */
	private void generateBaseMatrices(final DdnnfGraph graph) {
		this.ATilde = graphToMatrix(graph);
		this.DTilde = MatrixUtils.getDegreeMatrix(this.ATilde);
		this.poweredDTilde = MatrixUtils.fractionalPower(this.DTilde, -1, 2);
		this.DADTilde = poweredDTilde.times(ATilde).times(poweredDTilde);
	}
	
	/**
	 * Initializes all weight-layers.
	 */
	private void initLayers() {
		// Initialize weights and biases...
		Wglobal = new Matrix[this.numNodes];
		Wand = new Matrix[this.numNodes];
		Wor = new Matrix[this.numNodes];
		Wnot = new Matrix[this.numNodes];
		Wleaf = new Matrix[this.numNodes];

		Bglobal = new Matrix[this.numNodes];
		Band = new Matrix[this.numNodes];
		Bor = new Matrix[this.numNodes];
		Bnot = new Matrix[this.numNodes];
		Bleaf = new Matrix[this.numNodes];
		
		
		// ... And then fill the weight matrices.
		MatrixUtils.initMatrixArray(Wglobal);
		MatrixUtils.initMatrixArray(Wand);
		MatrixUtils.initMatrixArray(Wor);
		MatrixUtils.initMatrixArray(Wnot);
		MatrixUtils.initMatrixArray(Wleaf);
	}
	
	/*
	 * Secondly, all other methods.
	 */
	
	private Matrix semanticRegularization() { // l_r(F)

		CnfFormula F  = formulaSet.getCnfFormula();
		DdnnfGraph graph = formulaSet.getGraph();
		
		Matrix subResult = new Matrix(this.numNodes, 1);
		Matrix lossResult = new Matrix(this.numNodes, 1);
		HashMap<Integer, DdnnfGraph> childNodes;
		
		DdnnfGraph nodeGraph;
		
		
		// OR-nodes regularization
		for(int v : this.orNodesF) {
			nodeGraph = graph.getIntGraphMap().get(v);
			//subResult = subResult.plus(orLoss(nodeGraph.getChildGraphs()));
			
		}
		lossResult = lossResult.plus(subResult);
		subResult = new Matrix(this.numNodes, 1);
		
		// AND-nodes regularization
		for(int v : this.andNodesF) {
			nodeGraph = graph.getIntGraphMap().get(v);
			childNodes = new HashMap<Integer, DdnnfGraph>(nodeGraph.getChildGraphs());
			 
			//subResult = subResult.plus(andLoss(childNodes));
		}
		lossResult = lossResult.plus(subResult);
		
		return lossResult;
	}
	
	/**
	 * 
	 * @param childNodes
	 * @return
	 *//*
	private Matrix orLoss(final HashMap<Integer, DdnnfGraph> childNodes) {
		Matrix vector, sqrdVec;
		Matrix onesM = MatrixUtils.onesMatrix(childNodes.size(), 1);
		Matrix subSum = new Matrix(childNodes.size(), 1);
		
		for(Map.Entry<Integer, DdnnfGraph> entry : childNodes.entrySet()) {
			
			vector = embedLogicGraph(entry.getValue());
			// Get squared Euclidean distance and sum of resulting vector.
			sqrdVec = MatrixUtils.sqrdEuclidDistanceVec(vector, onesM);
			subSum = subSum.plus(sqrdVec);
		}
		return subSum;
	}
	
	/**
	 * 
	 * @param childNodes
	 * @return
	 *//*
	private Matrix andLoss(final HashMap<Integer, DdnnfGraph> childNodes) {
		
		// Generate Vk and VkT
		Matrix Vk = VertexMatrixK(childNodes);
		Matrix VkT = Vk.copy().transpose();
		Matrix matMultip = VkT.times(Vk);
		
		// Get squared Euclidean distance between Vk*VkT and diag(Vk*VkT).
		Matrix resultMat = MatrixUtils.sqrdEuclidDistanceVec(matMultip, MatrixUtils.getDiagonalMatrix(matMultip));
		
		return resultMat;
	}
	
	/**
	 * 
	 * @param childNodes
	 * @return
	 *//*
	private Matrix VertexMatrixK(HashMap<Integer, DdnnfGraph> childNodes) { // Gaat dit helemaal goed?
		Matrix Vk = new Matrix(numNodes, childNodes.size());
		
		for(int i = 0; i < childNodes.size(); i++) {
			Vk.setMatrix(0, numNodes, 
						i, i, 
						embedLogicGraph(childNodes.get(i))); // Vector verticaal (zoals nu) of horizontaal erin zetten?
		}
		return Vk;
	}*/
	
	/**
	 * 
	 * @param graph
	 * @return a vertical vector as Matrix object.
	 */
	/*
	private Matrix embedLogicGraph(final DdnnfGraph graph) { // q(.)
		HashMap<Integer, DdnnfGraph> graphMap = graph.getIntGraphMap();
		HashMap<Integer, DdnnfGraph> notSet = graph.getNotSet();
		return embedLogicGraphRec(graphMap, notSet, 0, true);
	}
	
	/**
	 * 
	 * @param input
	 * @param layerTypes
	 * @return
	 */
	private HashMap<String, ArrayList<Matrix>> feedForward(final Matrix input, final int[] layerTypes) {
		ArrayList<Matrix> activations = new ArrayList<Matrix>();
		ArrayList<Matrix> vectors = new ArrayList<Matrix>();
		Matrix activation, vector;
		
		activation = input;
		activations.add(activation);
		for (int l = 0; l < layerTypes.length; l++) {

			// Multiply D^(1/2)*A*D^(1/2) by the input.
			vector = this.DADTilde.copy();
			vector = vector.times(activation);
			
			// Depending on node type, multiply vector by
			// respective weights and plus its biases.
			switch (layerTypes[l]) {
			case 0: // AND node.
				// Multiply input by its weights, add its biases.
				vector = vector.times(Wand[l]);
				vector = vector.plus(Band[l]);
			case 1: // OR node.
				// Multiply input by its weights, add its biases.
				vector = vector.times(Wor[l]);
				vector = vector.plus(Bor[l]);
			case 2: // NOT node.
				// Multiply input by its weights, add its biases.
				vector = vector.times(Wnot[l]);
				vector = vector.plus(Bnot[l]);
			case 3: // Leaf node.
				// Multiply input by its weights, add its biases.
				vector = vector.times(Wleaf[l]);
				vector = vector.plus(Bleaf[l]);
			case 4: // Global node.
				// Multiply input by its weights, add its biases.
				vector = vector.times(Wglobal[l]);
				vector = vector.plus(Bglobal[l]);
			default: // Shouldn't arrive here.
				// Multiply input by its weights, add its biases.
				vector = vector.times(Wglobal[l]);
				vector = vector.plus(Bglobal[l]);
			}
			// Activate the output.
			activation = af.sigmoid(vector);
			// Save both the vector and its activation for back-prop.
			activations.add(activation);
			vectors.add(vector);
		}
		
		HashMap<String, ArrayList<Matrix>> avs = new HashMap<String, ArrayList<Matrix>>();
		// Add the activations and vectors to the HashMap and return the result.
		avs.put("activations", activations);
		avs.put("vectors", vectors);
		return avs;
	}
	
	
	
	
	
	/**
	 * 
	 * @param graph
	 * @return a vertical vector as Matrix object.
	 *//*
	private ArrayList<HashMap<String, ArrayList<Matrix>>> embedGraphForBackProp(final DdnnfGraph graph) { // q(.)
		HashMap<Integer, DdnnfGraph> graphMap = graph.getIntGraphMap();
		HashMap<Integer, DdnnfGraph> notSet = graph.getNotSet();

		// Returns List to store all the activations by layer and 
		//a list to store all the resulting vectors by layer.
		return embedGraphRecForBackProp(graphMap, notSet, 0, true);
	}
	
	/**
	 * 
	 * @param graphMap
	 * @param notSet
	 * @param node
	 * @param checkNegation
	 * @return
	 */
	private ArrayList<HashMap<String, ArrayList<Matrix>>> embedGraphRecForBackProp(final HashMap<Integer, DdnnfGraph> graphMap, 
																final HashMap<Integer, DdnnfGraph> notSet, final int thisRoot, 
																final int node, final boolean checkNegation) {
		
		ArrayList<HashMap<String, ArrayList<Matrix>>> typedAvs; // Typed list of activations and vectors
		typedAvs = new ArrayList<HashMap<String, ArrayList<Matrix>>>();
		for(int t = 0; t < this.typeIndices.size(); t++) { typedAvs.add(new HashMap<String, ArrayList<Matrix>>()); }
		HashMap<String, ArrayList<Matrix>> avs;
		/*
		 * GAAT DIT HIERBOVEN HELEMAAL GOED????? GEEEN LEGE MAPS?????
		 */
		
		final boolean hasLeft = graphMap.get(node).hasLeftChild();
		final boolean hasRight = graphMap.get(node).hasRightChild();
		
		final HashMap<Integer, DdnnfGraph> childGraphs = graphMap.get(node).getChildGraphs();
		
		if (node == thisRoot) {
			// Go deeper into recursion before appending the vectors of this iteration.
			typedAvs = embedGraphRecForBackProp(graphMap, notSet, thisRoot, node + 1, true);
			avs = typedAvs.get(this.typeIndices.get("Global"));
			// Add new vectors to the vectors of global and update the lists.
			avs = feedForwardTypeRecForBackProp(avs, Wglobal, Bglobal, 0);
			typedAvs.set(this.typeIndices.get("Global"), avs);
			// Return the updated lists.
			return typedAvs;
		}
		else if (checkNegation && notSet.containsKey(node)) {
			// Go deeper into recursion before appending the vectors of this iteration.
			typedAvs = embedGraphRecForBackProp(graphMap, notSet, thisRoot, node, false);
			avs = typedAvs.get(this.typeIndices.get("NOT"));
			// Add new vectors to the vectors of global and update the lists.
			avs = feedForwardTypeRecForBackProp(avs, Wnot, Bnot, 0);
			typedAvs.set(this.typeIndices.get("NOT"), avs);
			// Return the updated lists.
			return typedAvs;
			
		} // else if not a leaf node.
		else if (hasLeft & hasRight) {
			// Go deeper into recursion before appending the vectors of this iteration.
			typedAvs = embedGraphRecForBackProp(graphMap, notSet, thisRoot, node + 1, true);
			
			// Get embeddings of child graphs.
			Matrix[] childVecs = new Matrix[childGraphs.size()];
			Matrix vec;
			int counter = 0;
			for (Map.Entry<Integer, DdnnfGraph> entry : childGraphs.entrySet()) {
				childVecs[counter] = embedLogicGraphRec(graphMap, notSet, entry.getKey(), entry.getKey(), true, true);
				counter++;
			}
			
			// Conjunction nodes add up to unit vector.
			if (graphMap.get(node).getLogicType() == "AND") {
				vec = childVecs[0];
				for (int c = 1; c < childVecs.length; c++) {
					vec = vec.plus(childVecs[c]);
				}
				avs = typedAvs.get(this.typeIndices.get("AND"));
				// Add new vectors to the vectors of AND and update the lists.
				avs = feedForwardTypeRecForBackProp(avs, Wand, Band, 0);
				typedAvs.set(this.typeIndices.get("AND"), avs);
				return typedAvs;
			}
			// Disjunction nodes are deterministic.
			if (graphMap.get(node).getLogicType() == "OR") {
				vec = childVecs[0];
				for (int c = 1; c < childVecs.length; c++) {
					vec = vec.plus(childVecs[c]);
				}
				avs = typedAvs.get(this.typeIndices.get("OR"));
				// Add new vectors to the vectors of AND and update the lists.
				avs = feedForwardTypeRecForBackProp(avs, Wand, Band, 0);
				typedAvs.set(this.typeIndices.get("OR"), avs);
				return typedAvs;
			}
		}/*
		else { // if (graphMap.get(node).getLogicType() == "Leaf")
			Matrix vec = VectorUtils.getVector(graphMap.get(node).getFormula());
			ArrayList<Matrix> activations, vectors;
			activations = new ArrayList<Matrix>();
			activations.add(vec);
			vectors = new ArrayList<Matrix>();
			avs = new HashMap<String, ArrayList<Matrix>>();
			avs.put("activations", activations);
			avs.put("vectors", vectors);
			/*
			 * Dit hierboven uitbreiden in andere types en extra dimensie list toevoegen voor updaten.
			 
			ddddddddddddddddddddddddd
			avs = typedAvs.get(this.typeIndices.get("Leaf"));
			avs = feedForwardTypeRecForBackProp(avs, Wleaf, Bleaf, 0);
			typedAvs.set(this.typeIndices.get("Leaf"), avs);
			return typedAvs;
		}*/
		
		// Return below results in error.
		return typedAvs;
	}
	
	/**
	 * 
	 * @param graphMap
	 * @param notSet
	 * @param node
	 * @param checkNegation
	 * @return
	 */
	private Matrix embedLogicGraphRec(final HashMap<Integer, DdnnfGraph> graphMap, final HashMap<Integer, DdnnfGraph> notSet, 
									final int thisRoot, final int node, final boolean checkNegation, final boolean noActivation) {
		Matrix vec;
		final boolean hasLeft = graphMap.get(node).hasLeftChild();
		final boolean hasRight = graphMap.get(node).hasRightChild();
		
		final HashMap<Integer, DdnnfGraph> childGraphs = graphMap.get(node).getChildGraphs();
		Matrix[] childVecs = new Matrix[childGraphs.size()];
		int counter = 0;
		
		if (node == thisRoot) {
			vec = embedLogicGraphRec(graphMap, notSet, thisRoot, node + 1, true, noActivation);
			if(noActivation) { return feedForwardNoActivation(vec, Wglobal, Bglobal, 0); }
			else { return feedForwardTypeRec(vec, Wglobal, Bglobal, 0); }
		}
		else if (checkNegation && notSet.containsKey(node)) { 
			vec = embedLogicGraphRec(graphMap, notSet, thisRoot, node, false, noActivation);
			return feedForwardTypeRec(vec, Wnot, Bnot, 0);
			
		} // else if not a leaf node.
		else if (hasLeft & hasRight) {
			for (Map.Entry<Integer, DdnnfGraph> entry : childGraphs.entrySet()) {
				childVecs[counter] = embedLogicGraphRec(graphMap, notSet, thisRoot, entry.getKey(), true, noActivation);
				counter++;
			}
			
			// Conjunction nodes add up to unit vector.
			if (graphMap.get(node).getLogicType() == "AND") {
				vec = childVecs[0];
				for (int c = 1; c < childVecs.length; c++) {
					vec = vec.plus(childVecs[c]);
				}
				//vec1 = MatrixUtils.divideBy(vec1, childVecs.length);
				return feedForwardTypeRec(vec, Wand, Band, 0);
			}
			// Disjunction nodes are deterministic.
			if (graphMap.get(node).getLogicType() == "OR") {
				/*
				int v = -1;
				for (Map.Entry<Integer, DdnnfGraph> entry : childGraphs.entrySet()) {
					if (!notSet.containsKey(entry.getKey())) {
						v = entry.getKey();
					}
				}
				vec1 = embedLogicGraph(graphMap, notSet, v, false);
				return forwardProp(vec1, Wor, Bor, 0);*/

				vec = childVecs[0];
				for (int c = 1; c < childVecs.length; c++) {
					vec = vec.plus(childVecs[c]);
				}
				//vec1 = MatrixUtils.divideBy(vec1, childVecs.length);
				return feedForwardTypeRec(vec, Wor, Bor, 0);
			}
		}
		else { // if (graphMap.get(node).getLogicType() == "Leaf")
			vec = VectorUtils.getVector(graphMap.get(node).getFormula());
			return feedForwardTypeRec(vec, Wleaf, Bleaf, 0);
		}
		
		// Return below results in error.
		return new Matrix(2, 2);
	}
	
	/**
	 * 
	 * @param input
	 * @param answer
	 * @return
	 */
	private HashMap<String, Matrix[][]> initBackProp(final Matrix input, final Matrix answer) { // input veranderen!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		// Initialize backPropMap that holds per node type its
		// respective back-prop result.
		HashMap<String, Matrix[][]> bpMap = new HashMap<String, Matrix[][]>();
		// Fill the backPropMap with the respective values and return it.
		bpMap.put("AND", backPropTypeSpec("AND", input, answer, 0));
		bpMap.put("OR", backPropTypeSpec("OR", input, answer, 0));
		bpMap.put("NOT", backPropTypeSpec("NOT", input, answer, 0));
		bpMap.put("Leaf", backPropTypeSpec("Leaf", input, answer, 0));
		bpMap.put("Global", backPropTypeSpec("Global", input, answer, 0));
		
		return bpMap;
	}
	
	/**
	 * 
	 * @param type
	 * @param input
	 * @param answer
	 * @param layer
	 * @return
	 */
	private Matrix[][] backPropTypeSpec(final String type, final Matrix input, final Matrix answer, final int layer) {
		Matrix[][] dNablaTuple = new Matrix[2][]; // 0 = dNablaW, 1 = dNablaB
		Matrix[][] wb = selectWeightType(type);
		Matrix[] weights = wb[0];
		Matrix[] biases = wb[1];

		Matrix[] nablaW = new Matrix[weights.length];
		Matrix[] nablaB = new Matrix[biases.length];
		for (int l = 0; l < nablaW.length; l++) {
			nablaW[l] = new Matrix(weights[l].getRowDimension(), weights[l].getColumnDimension());
			nablaB[l] = new Matrix(biases[l].getRowDimension(), biases[l].getColumnDimension());
		}
		
		/*
		 * AANPASSEN NAAR LSENSR VORM!!!!!
		 * 
		 */
		// Feedforward.
		Matrix activation = input.copy();
		ArrayList<Matrix> activations = new ArrayList<Matrix>(); // List to store all the activations by layer.
		activations.add(activation);
		ArrayList<Matrix> vectors = new ArrayList<Matrix>(); // List to store all the resulting vectors by layer.

		Matrix vec;
		for (int l = 0; l < nablaW.length; l++) {
			vec = weights[l].times(activation).plus(biases[l]);
			vectors.add(vec);
			activation = MatrixUtils.sigmoid(vec);
			activations.add(activation);
		}
		/*
		 * 
		 */
		
		// Backward pass.
		Matrix deltaVec = activations.get(activations.size()-1).copy().minus(answer);
		deltaVec = deltaVec.arrayTimes( MatrixUtils.sigmoidPrime(vectors.get(vectors.size()-1)) );
		nablaW[this.nLayers-1] = deltaVec.times( activations.get(activations.size()-2).transpose() ).copy();
		nablaB[this.nLayers-1] = deltaVec.copy();
		
		Matrix deriv; // derivative of Sigmoid of the vector.
		Matrix tempMat;
		for (int l = this.nLayers-2; l >= 0; l--) {
			vec = vectors.get(l);
			deriv = MatrixUtils.sigmoidPrime(vec);
			tempMat = weights[l+1].copy().transpose();
			deltaVec = tempMat.times(deltaVec).arrayTimes(deriv);
			
			// Add result to nabla layers.
			nablaB[l] = deltaVec.copy();
			tempMat = activations.get(l-1).copy().transpose();
			nablaW[l] = deltaVec.times(tempMat);
		}
		
		// Put the resulting loss vectors in the return array and
		// return that array.
		dNablaTuple[0] = nablaW;
		dNablaTuple[1] = nablaB;
		
		return dNablaTuple;
	}
	
	/**
	 * Method for naming consistency.
	 * @param graph
	 * @return
	 */
	private Matrix feedForward(final DdnnfGraph graph) {
		//return embedLogicGraph(graph);
		return new Matrix(0,0);
	}

	/**
	 * Feed forward method for type specific embedding.
	 * @param input
	 * @return
	 */
	private Matrix feedForwardTypeSpec(final Matrix input, final String type) {
		Matrix resultMat = input.copy();
		Matrix[][] wb = selectWeightType(type);
		Matrix[] weights = wb[0];
		Matrix[] biases = wb[1];
		
		// Update the input given the weights and biases of
		// each layer in the network and return the result.
		resultMat = feedForwardTypeRec(input, weights, biases, 0);
		return resultMat;
	}
	
	/**
	 * Recursive feed forward method for type specific embedding.
	 * @param input
	 * @param typedWeights
	 * @param typedBiases
	 * @param layer
	 * @return
	 */
	private Matrix feedForwardTypeRec(final Matrix input, final Matrix[] typedWeights, final Matrix[] typedBiases, final int layer) {
		
		if (layer == typedWeights.length) {
			return input;
		}
		else {
			// Multiply D^(1/2)*A*D^(1/2) by the input.
			Matrix resultMat = this.DADTilde.copy();
			resultMat = resultMat.times(input);
			// Multiply input by its weights, add its biases and
			// Run through its activation function.
			resultMat = input.times(typedWeights[layer]);
			resultMat = resultMat.plus(typedBiases[layer]);
			resultMat = af.sigmoid(resultMat);
			// Step deeper into recursion.
			return feedForwardTypeRec(resultMat, typedWeights, typedBiases, layer+1);
		}
	}

	
	/**
	 * Recursive feed forward method for type specific embedding.
	 * @param input
	 * @param typedWeights
	 * @param typedBiases
	 * @param layer
	 * @return
	 */
	private Matrix feedForwardNoActivation(final Matrix input, final Matrix[] typedWeights, final Matrix[] typedBiases, final int layer) {
		
		if (layer == typedWeights.length-1) {
			// Multiply D^(1/2)*A*D^(1/2) by the input.
			Matrix resultMat = this.DADTilde.copy();
			resultMat = resultMat.times(input);
			// Multiply input by its weights, add its biases and
			// return it without its activation function.
			resultMat = input.times(typedWeights[layer]);
			resultMat = resultMat.plus(typedBiases[layer]);
			return resultMat;
		}
		else {
			// Multiply D^(1/2)*A*D^(1/2) by the input.
			Matrix resultMat = this.DADTilde.copy();
			resultMat = resultMat.times(input);
			// Multiply input by its weights, add its biases and
			// Run through its activation function.
			resultMat = input.times(typedWeights[layer]);
			resultMat = resultMat.plus(typedBiases[layer]);
			resultMat = af.sigmoid(resultMat);
			// Step deeper into recursion.
			return feedForwardTypeRec(resultMat, typedWeights, typedBiases, layer+1);
		}
	}
	
	/**
	 * Recursive feed forward method for type specific embedding.
	 * @param input
	 * @param typedWeights
	 * @param typedBiases
	 * @param layer
	 * @return
	 */
	private HashMap<String, ArrayList<Matrix>> feedForwardTypeRecForBackProp(HashMap<String, ArrayList<Matrix>> avs,
													final Matrix[] typedWeights, final Matrix[] typedBiases, final int layer) {
		
		if (layer == typedWeights.length) {
			return avs;
		}
		else {
			ArrayList<Matrix> activations = avs.get("activations");
			ArrayList<Matrix> vectors = avs.get("vectors");
			Matrix input = activations.get(activations.size()-1);
			
			// Multiply D^(1/2)*A*D^(1/2) by the input.
			Matrix vec = this.DADTilde.copy();
			vec = vec.times(input);
			// Multiply input by its weights, add its biases and
			// Run through its activation function.
			vec = input.times(typedWeights[layer]);
			vec = vec.plus(typedBiases[layer]);
			
			// Before activation, add to vector results.
			vectors.add(vec);
			
			// Activate the vector and add the activated vector to
			// activations.
			vec = af.sigmoid(vec);
			activations.add(vec);
			// Update avs with the updated lists and
			// step deeper into recursion.
			avs.put("activations", activations);
			avs.put("vectors", vectors);
			return feedForwardTypeRecForBackProp(avs, typedWeights, typedBiases, layer+1);
		}
	}
	
	/**
	 * 
	 * @param testSet
	 * @return
	 */
	private int evaluate(ArrayList<HashMap<DdnnfGraph, Matrix>> testSet) {
		ArrayList<HashMap<int[], int[]>> testResults = new ArrayList<HashMap<int[], int[]>>();
		int[] result = new int[2];
		int[] answer = new int[2];
		
		// Run all test cases through the network and save
		// it as a tuple with the correct answer for that case.
		int mapSize = 0;
		HashMap<int[], int[]> resMap;
		for (HashMap<DdnnfGraph, Matrix> map : testSet) {
			mapSize = 0;
			for (Map.Entry<DdnnfGraph, Matrix> data : map.entrySet()) {
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
	
	
	/*
	 * Local utility methods.
	 */
	
	/**
	 * 
	 * @param type
	 * @return
	 */
	private Matrix[][] selectWeightType(final String type) {
		Matrix[][] wb = new Matrix[2][]; // Weights-biases array to return.
		Matrix[] weights;
		Matrix[] biases;
		
		// Select the right weights and biases.
		switch(type) {
		case "AND":
			weights = Wand;
			biases = Band;
		case "OR":
			weights = Wor;
			biases = Bor;
		case "NOT":
			weights = Wnot;
			biases = Bnot;
		case "Leaf":
			weights = Wleaf;
			biases = Bleaf;
		case "Global":
			weights = Wglobal;
			biases = Bglobal;
		default:
			weights = Wglobal;
			biases = Bglobal;
		}
		
		// Fill weight-biases array and return.
		wb[0] = weights;
		wb[1] = biases;
		return wb;
	}
}
