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
		this.af = af;
		
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
	
	/**
	 * 
	 * @param input
	 * @param typedLayers
	 * @param typedBiases
	 * @param layer
	 * @return
	 */
	private Matrix forwardProp(final Matrix input, final Matrix[] typedLayers, final Matrix[] typedBiases, int layer) {
		
		if (layer == typedLayers.length) {
			return input;
		}
		else {
			// Multiply D^(1/2)*A*D^(1/2) by the input.
			Matrix resultMat = this.DADTilde.copy();
			resultMat = resultMat.times(input);
			// Multiply input by its weights, add its biases and
			// Run through its activation function.
			resultMat = input.times(typedLayers[layer]);
			resultMat = resultMat.plus(typedBiases[layer]);
			resultMat = af.sigmoid(resultMat);
			// Step deeper into recursion.
			return forwardProp(resultMat, typedLayers, typedBiases, layer++);
		}
	}
	
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
			subResult = subResult.plus(orLoss(nodeGraph.getChildGraphs()));
			
		}
		lossResult = lossResult.plus(subResult);
		subResult = new Matrix(this.numNodes, 1);
		
		// AND-nodes regularization
		for(int v : this.andNodesF) {
			nodeGraph = graph.getIntGraphMap().get(v);
			childNodes = new HashMap<Integer, DdnnfGraph>(nodeGraph.getChildGraphs());
			 
			subResult = subResult.plus(andLoss(childNodes));
		}
		lossResult = lossResult.plus(subResult);
		
		return lossResult;
	}
	
	/**
	 * 
	 * @param childNodes
	 * @return
	 */
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
	 */
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
	 */
	private Matrix VertexMatrixK(HashMap<Integer, DdnnfGraph> childNodes) { // Gaat dit helemaal goed?
		Matrix Vk = new Matrix(numNodes, childNodes.size());
		
		for(int i = 0; i < childNodes.size(); i++) {
			Vk.setMatrix(0, numNodes, 
						i, i, 
						embedLogicGraph(childNodes.get(i))); // Vector verticaal (zoals nu) of horizontaal erin zetten?
		}
		return Vk;
	}
	
	/**
	 * 
	 * @param graph
	 * @return a vertical vector as Matrix object.
	 */
	private Matrix embedLogicGraph(final DdnnfGraph graph) { // q(.)
		ArrayList<HashMap<Integer, DdnnfGraph>> nodeTypeMaps = graph.getNodeTypeMaps();
		Matrix[] andVecs, orVecs, notVecs, leafVecs;
		Matrix globalVec;
		Matrix bcv;
		int mapSize, counter = 0;
		
		// Order: 0. AND, 1. OR, 2. NOT, 3. Leaf
		
		// Embed AND-nodes.
		mapSize = nodeTypeMaps.get(0).size();
		andVecs = new Matrix[mapSize];
		for (Map.Entry<Integer, DdnnfGraph> entry : nodeTypeMaps.get(0).entrySet()) {
			bcv = VectorUtils.getVector(entry.getValue().getFormula());
			andVecs[counter] = forwardProp(bcv, this.Wand, this.Band, 0);
			counter++;
		}
		// Embed OR-nodes.
		mapSize = nodeTypeMaps.get(1).size();
		orVecs = new Matrix[mapSize];
		counter = 0;
		for (Map.Entry<Integer, DdnnfGraph> entry : nodeTypeMaps.get(1).entrySet()) {
			bcv = VectorUtils.getVector(entry.getValue().getFormula());
			orVecs[counter] = forwardProp(bcv, this.Wor, this.Bor, 0);
			counter++;
		}
		// Embed NOT-nodes.
		mapSize = nodeTypeMaps.get(2).size();
		notVecs = new Matrix[mapSize];
		counter = 0;
		for (Map.Entry<Integer, DdnnfGraph> entry : nodeTypeMaps.get(2).entrySet()) {
			bcv = VectorUtils.getVector(entry.getValue().getFormula());
			notVecs[counter] = forwardProp(bcv, this.Wnot, this.Bnot, 0);
			counter++;
		}
		// Embed Leaf-nodes.
		mapSize = nodeTypeMaps.get(3).size();
		leafVecs = new Matrix[mapSize];
		counter = 0;
		for (Map.Entry<Integer, DdnnfGraph> entry : nodeTypeMaps.get(3).entrySet()) {
			bcv = VectorUtils.getVector(entry.getValue().getFormula());
			leafVecs[counter] = forwardProp(bcv, this.Wleaf, this.Bleaf, 0);
			counter++;
		}
		// Embed the global node.
		bcv = VectorUtils.getVector(graph.getFormula());
		globalVec = forwardProp(bcv, this.Wglobal, this.Bglobal, 0);
	}
	
	
	private Matrix[][] backProp(final Matrix input, final Matrix answer, final int layer) {
		
	}
}
