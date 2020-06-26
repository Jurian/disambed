package org.uu.nl.embedding.lensr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeSet;

import org.apache.jena.graph.Graph;
import org.uu.nl.embedding.logic.LogicRule;
import org.uu.nl.embedding.logic.cnf.Clause;
import org.uu.nl.embedding.logic.cnf.CnfDateComparer;
import org.uu.nl.embedding.logic.cnf.CnfFormula;
import org.uu.nl.embedding.logic.cnf.CnfLogicRule;
import org.uu.nl.embedding.logic.cnf.LogicLiteral;
import org.uu.nl.embedding.logic.ddnnf.DdnnfLogicRule;
import org.uu.nl.embedding.logic.util.LogicRuleSet;
import org.uu.nl.embedding.logic.util.SimpleDate;
import org.uu.nl.embedding.util.OpsMatrix;

import Jama.Matrix;

/**
 * Class for the LENSR model
 * 
 * @author Euan Westenbroek
 * @version 0.1
 * @since 27-05-2020
 */
public class LENSRModel {
	
	Matrix qEmbedder;
	double lambda;
	
	public void LENSRModel() {
		
		/* TODO: implement constructor parameters
		 * 		and initialization methods.
		 */
	}
	
	/*
	 * Below all sub-algorithms can be found
	 * which, when combined, make up the 
	 * algorithm as a whole.
	 * 
	 */
	

	// layerWisePropagationRule variables
	int networkDepth;
	Matrix[] Z; // Z(l) = l times 1xn learnt latent node embedding at different layers
	Matrix D; // Dtilde = 2d diagonal degree matrix 
	Matrix A; // Atilde = 2d adjacency matrix
	Matrix I; // I_N = 2d identity matrix
	Matrix[] W; // W(l) = l times nx1 layer-specific trainable weight matrix
	/*
	 * Activation Function needs to get proper functions etc.
	 */
	String activationFunc; // niet echt een string?
	Map<String, Integer> activationFuncMap = new HashMap<String, Integer>();
	/*
	 * ******************************************************
	 */
	
	
	private void layerWisePropagationRule(final int layer) {
		
		Matrix poweredDTilde = OpsMatrix.fractionalPower(D, (-1), 2);
		I = Matrix.identity(A.getRowDimension(), A.getColumnDimension());
		Matrix aTilde = A.plus(I);
		
		Matrix resMatrix = poweredDTilde.times(aTilde);
		resMatrix = resMatrix.times(poweredDTilde);
		resMatrix = resMatrix.times(Z[layer]);
		resMatrix = resMatrix.times(W[layer]);
		
		//*******************************************************************
		resMatrix = resMatrix.times(activationFuncMap.get(activationFunc));
		//*******************************************************************
		
		Z[layer+1] = resMatrix;
	}
	
	private void initPropWeights() {
		/*
		 * This weight initialization takes inspiration
		 * from the paper by He et al. (2015), and is
		 * based on the Xavier initialization.
		 */
		Random rand = new Random();
		double randX;
		int layerSize;
		
		for(int l = 0; l < W.length; l++) {
			layerSize = Z[l].getColumnDimension();
			/* Get random variable based on Xavier init.
			* Here, nextGaussian() selects a number from
			* a Gaussian distribution with mean 0 and std 1
			* and 2 in 2/n_in comes from He et al.
			*/
			for(int neuron = 0; neuron < W[l].getRowDimension(); neuron++) {
				randX = rand.nextGaussian()*(2/layerSize);
				W[l].set(neuron, 0, randX);
			}
		}
	}

	// semanticRegularization variables
	ArrayList<Integer> orNodes;
	ArrayList<Integer> andNodes;
	DdnnfGraph logicGraph;
	
	private void semanticRegularization(CnfLogicRule F) {
		
		Matrix resLoss = new Matrix(Z[Z.length-1].getRowDimension(), 1);
		Matrix subSum  = new Matrix(Z[Z.length-1].getRowDimension(), 1);
		Matrix subResult = new Matrix(Z[Z.length-1].getRowDimension(), 1);
		Matrix matMultip;
		ArrayList<DdnnfLogicRule> childNodes;
		
		orNodes = getOrNodes(F);
		andNodes = getAndNodes(F);
		logicGraph = F.getDdnnfGraph();
		DdnnfLogicRule rule;
		
		
		// OR-nodes regularization
		for(int v : orNodes) {
			subSum  = new Matrix(Z[Z.length-1].getRowDimension(), 1);
			rule = logicGraph.getIntLogicMap().get(v);
			childNodes = new ArrayList<DdnnfLogicRule>(rule.getRules());
			for(LogicRule vj : childNodes) {
				subSum = subSum.plus(embedLogic(vj) - 1);//1 is een vector
			}
			// Get squared Euclidean distance
			subResult = subResult.plus(OpsMatrix.power(subSum, 2));   
			//subResult = (result.ycoord)*(result.ycoord) + (result.xcoord)*(result.xcoord);// Misschien helemaal niet nodig? // Not Math.sqrt() for squared Euclidean distance
		}
		resLoss = subResult;
		
		// AND-nodes regularization
		for(int v : orNodes) {
			subSum = new Matrix(Z[Z.length-1].getRowDimension(), 1);
			rule = logicGraph.getIntLogicMap().get(v);

			childNodes = new ArrayList<DdnnfLogicRule>(rule.getRules());
			matMultip = VertexMatrixK(childNodes).transpose().times(VertexMatrixK(childNodes));
			subSum = matMultip.minus(matMultip.arrayTimes(OpsMatrix.antiIdentity(matMultip)));
			
			// Get squared Euclidean distance
			subSum = OpsMatrix.power(subSum, 2);  
			//subResult = (result.ycoord)*(result.ycoord) + (result.xcoord)*(result.xcoord);// Misschien helemaal niet nodig? // Not Math.sqrt() for squared Euclidean distance
			subResult = subResult.plus(subSum);
		}
		resLoss = subResult;
	}
	
	private Matrix embedLogic(LogicRule rule) {
		Matrix placeHolder = new Matrix(Z[Z.length].getRowDimension(), 1);
		int layer = 0;
		
		
		
		return placeHolder;
	}
	
	private Matrix VertexMatrixK(ArrayList<DdnnfLogicRule> allTerms) { // Gaat dit helemaal goed?
		Matrix Vk = new Matrix(Z[Z.length].getRowDimension(), allTerms.size());
		
		for(int i = 0; i < allTerms.size(); i++) {
			Vk.setMatrix(0, Z[Z.length].getRowDimension(), 
						i, i, 
						embedLogic(allTerms.get(i)));
		}
		return Vk;
	}

	private double heterogeneousNodeEmbedding(LogicRule rule, LogicRule truthAssign, LogicRule falseAssign, double margin) {
		double res;
		Matrix embedRule = embedLogic(rule);
		Matrix embedTruth = embedLogic(truthAssign);
		Matrix embedFalse = embedLogic(falseAssign);
		
		res = sqrdEuclidDistance(embedRule, embedFalse) - sqrdEuclidDistance(embedRule, embedTruth);
		res += margin;
		
		return Math.max(res, 0);
	}

	private void embeddingTrainer() { // = L_emb
		
	}
	
	private Matrix embedderTraining(final LogicRuleSet ruleSet) {
		
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
	
	private Matrix updateEmbedder(Matrix curEmbedder, Object Theta_q) {
		
	}
	
	private void targetModelLoss() {
		
	}
	
	private void logicLoss () {
		
	}
	
	private double sqrdEuclidDistance(Matrix mat1, Matrix mat2) {
		double res = 0;
		double dist;
		
		for(int i = 0; i < mat1.getRowDimension(); i++) {
			dist = (mat1.get(i, 1) - mat2.get(i, 1));
			res += dist*dist;
		}
		return res;
	}
	
	/**
	 * 
	 * @param F
	 * @param node
	 * @return
	 */
	private ArrayList<Integer> getChildNodes(final CnfLogicRule F, final int node) {
		List<Integer> childNodes = new ArrayList<Integer>();
		TreeSet<Integer> otherNodes = new TreeSet<Integer>();
		
		DdnnfGraph nodeGraph = F.getDdnnfGraph().getNode(node);
		String nodeType = nodeGraph.getLogicType();

		otherNodes.add(node);
		int next;
		DdnnfGraph nextGraph;
		while (!otherNodes.isEmpty()) {
			next = otherNodes.first();
			nextGraph = nodeGraph.getNode(next);
			if (nextGraph.getLogicType() == nodeType) {
				if (nextGraph.hasLeftChild()) { 
					otherNodes.add(nextGraph.getLeftInt()); }
				if (nextGraph.hasRightChild()) { 
					otherNodes.add(nextGraph.getRightInt()); }
			}
			else {
				childNodes.add(next);
			}
			
			otherNodes.remove(next);
		}
		// Sorting
		Collections.sort(childNodes);
		return (ArrayList<Integer>) childNodes;// Gaat dit goed?
	}
	
	/**
	 * 
	 * @param F
	 * @return
	 */
	private ArrayList<Integer> getAndNodes(CnfLogicRule F) {
		List<Integer> andNodes = new ArrayList<Integer>();
		DdnnfGraph logicGraph = F.getDdnnfGraph();
		
		for(Map.Entry<Integer, String> entry : logicGraph.getGraph().entrySet()) {
			if(entry.getValue() == "AND") {
				andNodes.add(entry.getKey());
			}
		}
		// Sorting
		Collections.sort(andNodes);
		return (ArrayList<Integer>) andNodes;// Gaat dit goed?
	}

	/**
	 * 
	 * @param F
	 * @return
	 */
	private ArrayList<Integer> getOrNodes(CnfLogicRule F) {
		List<Integer> orNodes = new ArrayList<Integer>();
		DdnnfGraph logicGraph = F.getDdnnfGraph();
		
		for(Map.Entry<Integer, String> entry : logicGraph.getGraph().entrySet()) {
			if(entry.getValue() == "OR") {
				orNodes.add(entry.getKey());
			}
		}
		// Sorting
		Collections.sort(orNodes);
		return (ArrayList<Integer>) orNodes;// Gaat dit goed?
	}
	

}
