package org.uu.nl.embedding.lensr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import org.apache.jena.graph.Graph;
import org.apache.log4j.Logger;
import org.uu.nl.embedding.lensr.utils.LensrUtils;
import org.uu.nl.embedding.lensr.utils.VectorUtils;
import org.uu.nl.embedding.logic.LogicRule;
import org.uu.nl.embedding.logic.cnf.Clause;
import org.uu.nl.embedding.logic.cnf.CnfDateComparer;
import org.uu.nl.embedding.logic.cnf.CnfFormula;
import org.uu.nl.embedding.logic.cnf.CnfLogicRule;
import org.uu.nl.embedding.logic.cnf.LogicLiteral;
import org.uu.nl.embedding.logic.ddnnf.DdnnfDate;
import org.uu.nl.embedding.logic.ddnnf.DdnnfDateComparer;
import org.uu.nl.embedding.logic.ddnnf.DdnnfFormula;
import org.uu.nl.embedding.logic.ddnnf.DdnnfLiteral;
import org.uu.nl.embedding.logic.ddnnf.DdnnfLogicRule;
import org.uu.nl.embedding.logic.util.LogicRuleSet;
import org.uu.nl.embedding.logic.util.SimpleDate;
import org.uu.nl.embedding.util.ArrayUtils;
import org.uu.nl.embedding.util.MatrixUtils;

import Jama.Matrix;

/**
 * Class for the LENSR model
 * 
 * @author Euan Westenbroek
 * @version 0.1
 * @since 27-05-2020
 */
public class LensrModel {
	

    private final static Logger logger = Logger.getLogger(LensrModel.class);
	
    LensrRun run;
    
    Matrix[] defaultWeights, defaultBiases;
	Matrix qEmbedder;
	HashMap<String, Integer> literalIDMap;
	HashMap<Boolean, Integer> negationMap;

	double lambda, lambda_r, margin;
	int nHiddenLayers, nLayerNeurons, embedDim;
	
	boolean hasIndepWeights;
	ActivationFunction af;
	
	
	public void LENSRModel(final boolean hasIndepWeights, final int inFeats, final int outFeats) {
		/* TODO: implement constructor parameters
		 * 		and initialization methods.
		 * 
		 * 	1. Set activation function
		 */

		initSetMap();
		
		// Set independent weights.
		this.hasIndepWeights = hasIndepWeights;
		
		// Model specifications.
		this.nHiddenLayers = 3;
		this.nLayerNeurons = 50;
		this.embedDim = 100;
		
		// Hyper-parameters.
		this.lambda = 0.1d;
		this.lambda_r = 0.1d;
		this.margin = 1.0d;
		
		// Formulae complexity.
		int nVariables = 3; // or 6, n_v
		int maxDepth = 3; // or 6, d_m
		
		// Activation function.
		af = new ActivationFunction();
		
		// Set running model
		this.run = new LensrRun(this, this.hasIndepWeights, this.nHiddenLayers, this.nLayerNeurons, 
								this.embedDim, this.lambda, this.lambda_r, this.margin, nVariables, 
								maxDepth);
	}
	
	private void initSetMap() {

		literalIDMap = new HashMap<String, Integer>() {{
	        put("BirthDate", 1);
	        put("DeathDate", 2);
	        put("BaptisedDate", 3);
	        put("CompareTo", 4);
	    }};
	    
	    negationMap = new HashMap<Boolean, Integer>() {{
	        put(true, 1);
	        put(false, 2);
	    }};
	}
	
	/*
	 * Below all sub-algorithms can be found
	 * which, when combined, make up the 
	 * algorithm as a whole.
	 * 
	 */
	

	// layerWisePropagationRule variables
	int nLayers, numNodes, numInputFeats;
	public Matrix[] Zleaf, Zglobal, Zand, Zor, Znot; // Z(l) = l times 1xn learnt latent node embedding at different layers
	public Matrix A; // A = 2d adjacency matrix
	public Matrix I; // I_N = 2d identity matrix
	public Matrix ATilde; // A tilde = 2d adjacency matrix with self-connections
	public Matrix DTilde; // 2d diagonal degree matrix with self-connections
	public Matrix poweredDTilde; // D = 2d DTilde matrix to power of -1/2
	public Matrix DADtilde; // poweredDTilde dot ATilde dot poweredDTilde
	public Matrix[] Wleaf, Wglobal, Wand, Wor, Wnot; // W(l) = l times nxn layer-specific trainable weight matrix.
	public Matrix[] Bleaf, Bglobal, Band, Bor, Bnot; // bias(l) = l times nxn layer-specific bias matrix.
	
	/*
	 * Activation Function needs to get proper functions etc.
	 */
	String activationFunc; // niet echt een string?
	Set<String> activationFuncMap = new HashSet<String>(Arrays.asList("sigmoid", "adam"));
	/*
	 * ******************************************************
	 */
	
	private void initPropRule() {
		
		double[][] arrayA = new double[][] {	{0d,1d,1d,0d,0d,0d,0d,0d,0d},
												{1d,0d,0d,0d,0d,0d,0d,0d,0d},
												{1d,0d,0d,1d,1d,0d,0d,0d,0d},
												{0d,0d,1d,0d,0d,0d,0d,0d,0d},
												{0d,0d,1d,0d,0d,1d,1d,0d,0d},
												{0d,0d,0d,0d,1d,0d,0d,0d,0d},
												{0d,0d,0d,0d,1d,0d,0d,1d,1d},
												{0d,0d,0d,0d,0d,0d,1d,0d,0d},
												{0d,0d,0d,0d,0d,0d,1d,0d,0d}		};
		A = new Matrix(arrayA);

		I = Matrix.identity(A.getRowDimension(), A.getColumnDimension());
		ATilde = A.plus(I);
		
		int degree;
		DTilde = new Matrix(A.getRowDimension(), A.getColumnDimension());
		for (int i = 0; i < A.getRowDimension(); i++) {
			degree = 0;
			for(int j = 0; j < A.getRowDimension(); j++) {
				degree += ATilde.get(i,j);
			}
			DTilde.set(i, i, degree);
		}
		poweredDTilde = MatrixUtils.fractionalPower(DTilde, (-1), 2);

		DADtilde = MatrixUtils.dot(poweredDTilde, ATilde);
		DADtilde = MatrixUtils.dot(DADtilde, poweredDTilde);
		
		// Initialize weight matrices.
		Wleaf[numInputFeats] = new Matrix(numNodes, numInputFeats);
		Wglobal[numInputFeats] = new Matrix(numNodes, numInputFeats);
		Wand[numInputFeats] = new Matrix(numNodes, numInputFeats);
		Wor[numInputFeats] = new Matrix(numNodes, numInputFeats);
		
		Wleaf = LensrUtils.initPropWeights(Wleaf);
		Wglobal = LensrUtils.initPropWeights(Wglobal);
		Wand = LensrUtils.initPropWeights(Wand);
		Wor = LensrUtils.initPropWeights(Wor);
		
		// Initialize embedding matrices.
		Zleaf[numInputFeats] = new Matrix(numNodes, numInputFeats);
		Zglobal[numInputFeats] = new Matrix(numNodes, numInputFeats);
		Zand[numInputFeats] = new Matrix(numNodes, numInputFeats);
		Zor[numInputFeats] = new Matrix(numNodes, numInputFeats);
		
		Zleaf = LensrUtils.initPropWeights(Zleaf);
		Zglobal = LensrUtils.initPropWeights(Zglobal);
		Zand = LensrUtils.initPropWeights(Zand);
		Zor = LensrUtils.initPropWeights(Zor);
		
		
	}
	
	/**
	 * 
	 * @param layer
	 * @param Z
	 * @param W
	 */
	private void layerWisePropagationRule(final int layer, Matrix[] Z, Matrix[] W) {
		
		Matrix resMatrix = DADtilde.copy();
		
		resMatrix = MatrixUtils.dot(resMatrix, Z[layer]);
		resMatrix = MatrixUtils.dot(resMatrix, W[layer]);
		
		//*******************************************************************
		resMatrix = activation(resMatrix);
		//*******************************************************************
		
		Z[layer+1] = resMatrix;
	}
	
	/**
	 * Run activation function over matrix
	 * @param M matrix
	 * @return result of activation function on M
	 */
	private Matrix activation(Matrix M) {
		Matrix resultM;
		
		if (activationFunc == "sigmoid") {
			resultM = MatrixUtils.sigmoid(M);
			logger.info("Activation function set as: " + activationFunc);
			
		} else if (activationFunc == "adam") { // NOG VERANDEREN!!!!
			resultM = MatrixUtils.sigmoid(M);
			logger.info("Activation function set as: " + activationFunc);
		} else { 
			// Default activation function.
			resultM = MatrixUtils.sigmoid(M);
			logger.info("Activation function set as: (Default) Sigmoid");
		}
		
		return resultM;
	}

	// semanticRegularization variables
	ArrayList<Integer> orNodes;
	ArrayList<Integer> andNodes;
	DdnnfGraph logicGraph;
	
	private double semanticRegularization(final LogicRuleSet formulaSet) { // l_r(F)
		CnfFormula F  = formulaSet.getCnfFormula();
		DdnnfGraph graph = formulaSet.getGraph();
		
		double subSum, subResult = 0d, lossResult = 0d;
		Matrix matMultip, Vk, VkT;
		HashMap<Integer, DdnnfGraph> childNodes;
		
		orNodes = LensrUtils.getOrNodes(F);
		andNodes = LensrUtils.getAndNodes(F);
		DdnnfGraph nodeGraph;
		
		
		// OR-nodes regularization
		for(int v : orNodes) {
			nodeGraph = graph.getIntGraphMap().get(v);
			subResult += orLoss(nodeGraph.getChildGraphs());
			
		}
		lossResult += subResult;
		subResult = 0d;
		
		// AND-nodes regularization
		for(int v : andNodes) {
			nodeGraph = graph.getIntGraphMap().get(v);
			childNodes = new HashMap<Integer, DdnnfGraph>(nodeGraph.getChildGraphs());
			 
			subResult += andLoss(childNodes);
		}
		lossResult += subResult;
		
		return lossResult;
	}
	
	private double orLoss(final HashMap<Integer, DdnnfGraph> childNodes) {
		Matrix vector, normVector = new Matrix(childNodes.size(), 1);
		double subSum = 0d, matSum;
		int counter = 0;
		for(Map.Entry<Integer, DdnnfGraph> entry : childNodes.entrySet()) {
			
			vector = embedLogicGraph(entry.getValue());
			matSum = MatrixUtils.sum(vector);
			normVector.set(counter, 0, matSum);
			
			/* OR NOT?? See GitHub
			// Get squared Euclidean distance
			subSum += sqrdEuclidDistance( embedLogicGraph(entry.getValue()), onesM );
			*/
		}
		subSum = Math.pow((normVector.normF() - 1), 2);
		
		return subSum;
	}
	
	private double andLoss(final HashMap<Integer, DdnnfGraph> childNodes) {
		
		Matrix Vk = VertexMatrixK(childNodes);
		Matrix VkT = Vk.copy().transpose();
		Matrix matMultip = VkT.times(Vk);
		
		Matrix resultMat = MatrixUtils.abs(matMultip.minus(MatrixUtils.getDiagonalMatrix(matMultip)));
		
		/* OR NOT?? See GitHub
		// Get squared Euclidean distance
		return sqrdEuclidDistance(matMultip, MatrixUtils.antiIdentity(matMultip));
		*/
		
		return MatrixUtils.sum(resultMat);
	}
	
	private Matrix VertexMatrixK(HashMap<Integer, DdnnfGraph> childNodes) { // Gaat dit helemaal goed?
		Matrix Vk = new Matrix(numNodes, childNodes.size());
		
		for(int i = 0; i < childNodes.size(); i++) {
			Vk.setMatrix(0, numNodes, 
						i, i, 
						embedLogicGraph(childNodes.get(i))); // Vector verticaal (zoals nu) of horizontaal erin zetten?
		}
		return Vk;
	}

	private double heterogeneousNodeEmbedding(final DdnnfGraph mainGraph, final DdnnfGraph graphT, final DdnnfGraph graphF) {
		double res;
		Matrix embedRule = embedLogicGraph(mainGraph);
		Matrix embedTruth = embedLogicGraph(graphT);
		Matrix embedFalse = embedLogicGraph(graphF);
		
		res = sqrdEuclidDistance(embedRule, embedFalse) - sqrdEuclidDistance(embedRule, embedTruth);
		res += margin;
		
		return Math.max(res, 0);
	}
	
	private double sqrdEuclidDistance(Matrix mat1, Matrix mat2) {
		MatrixUtils.checkMatrixDims(mat1, mat2);
		
		double res = 0d;
		double dist;
		
		for(int i = 0; i < mat1.getRowDimension(); i++) {
			dist = (mat1.get(i, 0) - mat2.get(i, 0));
			res += dist*dist;
		}
		return res;
	}

	private double embeddingTrainer(final ArrayList<LogicRuleSet> formulaeSets) { // = L_emb
		double embLoss = 0d, tripletLoss = 0d, regLoss = 0d; // L_emb, l_t, and l_r respectively.
		double subLoss;
		CnfFormula form;
		DdnnfLogicRule mainRule;
		List<HashMap<String, Boolean>> tauT, tauF;
		DdnnfGraph graphT, graphF, mainGraph;
		
		for (int F = 0; F < formulaeSets.size(); F++) { // Sum(F)
			form = formulaeSets.get(F).getCnfFormula();
			mainRule = formulaeSets.get(F).getMainRule();
			mainGraph = formulaeSets.get(F).getGraph();
			tauT = form.getTrueAssignments();
			tauF = form.getFalseAssignments();
			
			subLoss = 0;
			for (int t = 0; t < tauT.size(); t++) { // Sum(tauT)
				for (int f = 0; f < tauF.size(); f++) { // Sum(tauF)
					
					// triplet loss,  t_l(F, tauT, tauF)
					graphT = getDdnnfAssignGraph(mainRule, tauT.get(t));
					graphF = getDdnnfAssignGraph(mainRule, tauF.get(t));
					tripletLoss = heterogeneousNodeEmbedding(mainGraph, graphT, graphF);
					
					// strength of regularization * regularization loss
					
					regLoss = semanticRegularization(formulaeSets.get(F));
					// Sum(l_t + lambda_r * l_r(F))
					subLoss += ( tripletLoss + lambda_r*regLoss );
			}}
			
			// Increase loss after each formula.
			embLoss += subLoss;
		}
		
		return embLoss;
	}
	
	private Matrix embedderTraining(final LogicRuleSet ruleSet) {
		/*
		 * q(.) is trained using only f
		 */
		
		String comparison = ruleSet.getComparison();
		CnfFormula cnfFormula = ruleSet.getCnfFormula();
		List<HashMap<String, Boolean>> allTrueAssignments = new ArrayList<HashMap<String, Boolean>>();
		List<HashMap<String, Boolean>> allFalseAssignments = new ArrayList<HashMap<String, Boolean>>();
		
		SimpleDate firstDate;
		firstDate = new SimpleDate("01-01-2000");
		for (LogicLiteral literal : cnfFormula.getLiterals()) {
			if (literal instanceof CnfDateComparer) {
				firstDate = new SimpleDate(((CnfDateComparer) literal).getDates()[0].toString());
			}
		}
		
		LogicRuleSet curRuleSet;
		CnfFormula curCnfFormula;
		ArrayList<DdnnfLogicRule> curDdnnfRules;
		ArrayList<DdnnfLogicRule> foundDdnnfRules;
		ArrayList<LogicRuleSet> allRuleSets = new ArrayList<LogicRuleSet>();
		allRuleSets.add(ruleSet);
		
		boolean notConverged = true;
		double lossDifference = 0; // To check for convergence.
		LogicRule[] fi; // intermediate formula.
		int[] nodes;
		
		String[] literalVals = new String[10]; // TEMP
		int[][] images = new int[10][]; // TEMP variable for 'training images'.
		// wrs een holder voor de huidige rdf.
		
		qEmbedder = qInit();
		// q(h(x)) = relation prediction van GloVe if(personX == personY).
		// q(G) = prediction obv d-DNNF graph if(personX == personY).
		
		while(notConverged) {
			for (int i = 0; i < images.length; i++) {
				// Create intermediate formula
				foundDdnnfRules = new ArrayList<DdnnfLogicRule>(); // = fi in algo paper
				nodes = images[i];
				for (int j = 0; j < nodes.length; j++) { // for all ci in f do
					if (SimpleDate.isDateFormat(literalVals[j])) {
						
						allRuleSets = new ArrayList<LogicRuleSet>(allRuleSets);
						curRuleSet = new LogicRuleSet(ruleSet.getRuleType(), firstDate.toString(), 
																		literalVals[j], comparison);
						allRuleSets.add(curRuleSet); // fi = fi U ci.
						curCnfFormula = curRuleSet.getCnfFormula();
						
						/* OLD
						curDdnnfRules = curRuleSet.splitDdnnfRules();
						for (DdnnfLogicRule childRule : curDdnnfRules) { // for all ci in f do
							if (childRule.getAssignment()) {
								foundDdnnfRules.add(childRule); // fi = fi U ci.
						}}*/
						
						/* Append constraints? --> constraint zitten al in logic rules.
						 * fi = append_constraints(fi).
						 */
						
						allTrueAssignments.addAll(curCnfFormula.getTrueAssignments());
						allFalseAssignments.addAll(curCnfFormula.getFalseAssignments());
						// Theta_q = argmin(Theta_q, L_emb)
						qEmbedder = updateEmbedder(qEmbedder, Theta_q);
				}}
				
			}
			if (lossDifference < 0.01) { notConverged = false; } // So there was convergence.
		}
		
		// Return the resulting embedder.
		return qEmbedder;
	}
	
	private double targetModelLoss(final LogicRuleSet ruleSet, final DdnnfFormula relationPrediction) {
		Matrix graphEmbedding = embedLogicGraph(ruleSet.getGraph()); // q(G)
		Matrix relationPredEmbedding = embedLogicGraph(formulaToGraph(relationPrediction)); // q(h(x))
		
		// l = l_c + lambda * l_logic
		double lossResult = crossEntropyLoss(graphEmbedding, relationPredEmbedding) + (lambda * logicLoss(graphEmbedding, relationPredEmbedding));
		
		return lossResult;
	}
	
	private double logicLoss(final Matrix graphEmbedding, final Matrix relationPredEmbedding) {
		MatrixUtils.checkMatrixDims(graphEmbedding, relationPredEmbedding);
		
		/* Relation prediction h(x) = GloVe classification if (personX == personY) (NB. No MLP is used!
		 * ----- So: BirthDate(x) AND DeathDate(y) AND CompareDates(x < y), where
		 * ---------- x = date PersonX, and y = date PersonY.
		 *
		 * q(G) = q(F_x) = q(f) = logic date rule is true
		 * ----- So: Whole knowledge graph of BirthDateBeforeDeathDate rule. = G
		 *
		 */
		
		double lossResult = sqrdEuclidDistance(graphEmbedding, relationPredEmbedding);
		
		return lossResult;
	}
	
	private double crossEntropyLoss(final Matrix graphEmbedding, final Matrix relationPredEmbedding) { // MOETEN HIER WEL DE EMBEDDINGS IN?????
		MatrixUtils.checkMatrixDims(graphEmbedding, relationPredEmbedding);
		
		double resultLoss = 0d;
		for (int r = 0; r < graphEmbedding.getRowDimension(); r++) {
			for(int c = 0; c < graphEmbedding.getColumnDimension(); c++) {
				
				resultLoss += graphEmbedding.get(r, c) - Math.log(relationPredEmbedding.get(r, c));
		}}
		
		return -1*resultLoss;
	}
	
	/**
	 * 
	 * @param graph
	 * @return a vertical vector as Matrix object.
	 */
	private Matrix embedLogicGraph(final DdnnfGraph graph) { // q(.)
		HashMap<Integer, DdnnfGraph> intGraphMap = graph.getIntGraphMap();
		HashMap<Integer, String> nodeTypeMap = graph.getOperatorMap();

		int node;
		String type;
		for (Map.Entry<Integer, String> entry : nodeTypeMap.entrySet()) {
			node = entry.getKey();
			type = entry.getValue();
			
			if (type == "AND") {}
			else if (type == "OR") {}
			else if (type == "Literal") {}
			else if (type == "Global") {}
		}
		
	}
	
	
	private Matrix updateEmbedder(Matrix curEmbedder, Object Theta_q) {
		
	}
	
	private void backwardsPropagation() { // ??
		
	}
	
	
	
	/*
	 * Utils
	 */
	private DdnnfGraph getDdnnfAssignGraph(final DdnnfLogicRule ddnnfRule, final HashMap<String, Boolean> tauA) {
		boolean[] orderedAssign = new boolean[tauA.size()];
		DdnnfLiteral[] literals = new DdnnfLiteral[tauA.size()];
		ArrayList<DdnnfLogicRule> subRules = LogicRuleSet.splitDdnnfRules(ddnnfRule);

		int counter = 0;
		for (int i = 0; i < subRules.size(); i++) { // GAAT DIT GOED MET DE COUNTER ENZO??????	
			for (Map.Entry<String, Boolean> entry : tauA.entrySet()) {
				
				if (subRules.get(i).getName() == entry.getKey()) {
					if (subRules.get(i) instanceof DdnnfDate) { literals[counter] = (DdnnfDate) subRules.get(i); counter++; }
					if (subRules.get(i) instanceof DdnnfDateComparer) { literals[counter] = (DdnnfDateComparer) subRules.get(i); counter++; }
					orderedAssign[i] = entry.getValue();
				}
		}}
		DdnnfFormula tempForm, resultForm = new DdnnfFormula(literals, orderedAssign, ddnnfRule.getCnfName());
		DdnnfGraph graph, graph2;
		
		graph = new DdnnfGraph(literals[0]);
		if (literals.length > 2) { 
			graph2 = new DdnnfGraph(literals[1]); 
			tempForm = new DdnnfFormula(new DdnnfLiteral[] {literals[0], literals[1]}, 
										new boolean[] {orderedAssign[0],orderedAssign[0]}, 
										literals[0].getName() + " AND " + literals[0].getName());
			graph = new DdnnfGraph(tempForm, graph, graph2);
		
			for (int i = 3; i < literals.length; i++) {
				graph2 = graph;
				graph = new DdnnfGraph(literals[i]);
				
				tempForm = new DdnnfFormula(new DdnnfLogicRule[] {literals[i], tempForm},
											new boolean[] {orderedAssign[i], tempForm.getAssignment()},
											literals[i].getName() + " AND " + literals[0].getName());
				graph = new DdnnfGraph(tempForm, graph, graph2);
				
			}
		}
		
		return graph;
	}
	
	private DdnnfGraph formulaToGraph(final DdnnfFormula formula) {
		ArrayList<DdnnfLogicRule> separatedRules = new ArrayList<DdnnfLogicRule>();
		ArrayList<DdnnfLogicRule> queue = new ArrayList<DdnnfLogicRule>(formula.getRules());
		
		// Get all rules
		while(queue.size() > 0) {
			for (DdnnfLogicRule rule : queue) {
				separatedRules.add(rule);
				if (!(rule instanceof DdnnfLiteral)) {
					queue.addAll(rule.getRules());
				}
				queue.remove(rule);
			}
		}
		
		DdnnfGraph graph, graph2;
		int numRules = separatedRules.size();
		graph = new DdnnfGraph(separatedRules.get(numRules-1));
		if (separatedRules.size() > 2) { 
			graph2 = new DdnnfGraph(separatedRules.get(numRules-2));

			graph = new DdnnfGraph(separatedRules.get(numRules-3), graph, graph2);
			for (int i = 4; i < numRules; i++) {
				graph2 = graph;
				graph = new DdnnfGraph(separatedRules.get(numRules-i));
				
				// Increment to get combined rule.
				i++;
				graph = new DdnnfGraph(separatedRules.get(numRules-i), graph, graph2);
			}
		}
		
		return graph;
	}
	

}
