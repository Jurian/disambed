package org.uu.nl.embedding.lensr.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.uu.nl.embedding.lensr.DdnnfGraph;
import org.uu.nl.embedding.logic.cnf.CnfLogicRule;
import org.uu.nl.embedding.logic.ddnnf.DdnnfDate;
import org.uu.nl.embedding.logic.ddnnf.DdnnfDateComparer;
import org.uu.nl.embedding.logic.ddnnf.DdnnfLogicRule;
import org.uu.nl.embedding.logic.util.SimpleDate;

import Jama.Matrix;


/**
 * 
 * @author Euan Westenbroek
 *
 */
public class LensrUtils {
	

	/**
	 * 
	 * @param matrix
	 * @return
	 */
	public static Matrix initPropWeights(final Matrix matrix) {
		int nRows = matrix.getRowDimension();
		int nCols = matrix.getColumnDimension();
		Matrix resultMat = new Matrix(nRows, nCols);
		/*
		 * This weight initialization takes inspiration
		 * from the paper by He et al. (2015), and is
		 * based on the Xavier initialization.
		 */
		Random rand = new Random();
		double randX;
		/* 
		 * Get random variable based on Xavier init.
		 * Here, nextGaussian() selects a number from
		 * a Gaussian distribution with mean 0 and std 1
		 * and 2 in 2/n_in comes from He et al.
		 */
		for(int r = 0; r < nRows; r++) {
			for(int c = 0; c < nCols; c++) {
				
				randX = rand.nextGaussian()*(2/(nRows*nCols));
				resultMat.set(r, c, randX);
		}}
		
		return resultMat;
	}
	
	/**
	 * 
	 * @param matrixArr
	 * @return
	 */
	public static Matrix[] initPropWeights(final Matrix[] matrixArr) {
		Matrix[] resultMats = new Matrix[matrixArr.length];
		
		for (int i = 0; i < matrixArr.length; i++) {
			resultMats[i] = initPropWeights(matrixArr[i]);
		}
		
		return resultMats;
	}
	
	/**
	 * 
	 * @param F
	 * @param node
	 * @return
	 */
	public static ArrayList<Integer> getChildNodes(final CnfLogicRule F, final int node) {
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
	public static ArrayList<Integer> getAndNodes(CnfLogicRule F) {
		List<Integer> andNodes = new ArrayList<Integer>();
		DdnnfGraph logicGraph = F.getDdnnfGraph();
		
		for(Entry<Integer, DdnnfGraph> entry : logicGraph.getIntGraphMap().entrySet()) {
			if(entry.getValue().getLogicType() == "AND") {
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
	public static ArrayList<Integer> getOrNodes(CnfLogicRule F) {
		List<Integer> orNodes = new ArrayList<Integer>();
		DdnnfGraph logicGraph = F.getDdnnfGraph();
		
		for(Entry<Integer, DdnnfGraph> entry : logicGraph.getIntGraphMap().entrySet()) {
			if(entry.getValue().getLogicType() == "OR") {
				orNodes.add(entry.getKey());
			}
		}
		// Sorting
		Collections.sort(orNodes);
		return (ArrayList<Integer>) orNodes;// Gaat dit goed?
	}
	
	/**
	 * 
	 * @param graph
	 * @return
	 */
	public static Matrix embedLiteral(final DdnnfGraph graph) {
		int inputSize = 9;
		Matrix vector;
		
		
		if (graph.getFormula() instanceof DdnnfDate) {
			vector = embedDate(graph.getFormula(), "neutral", true, false);
		}
		else /*if (graph.getFormula() instanceof DdnnfDateComparer)*/ {
			vector = embedCompareDate(graph.getFormula(), "neutral", true, false);
		}
		
		//TODO: do rest stuff
		return vector;
	}
	
	/**
	 * 
	 * @param rule
	 * @param truthVal
	 * @param isNegated
	 * @param isNeutral
	 * @return
	 */
	public static Matrix embedDate(final DdnnfLogicRule rule, final String truthVal, final boolean isNegated, final boolean isNeutral) {
		DdnnfDate literal = (DdnnfDate) rule;
		Matrix vector = VectorUtils.getVector(literal);
		

		return vector;
		
	}
	
	/**
	 * 
	 * @param rule
	 * @param truthVal
	 * @param isNegated
	 * @param isNeutral
	 * @return
	 */
	public static Matrix embedCompareDate(final DdnnfLogicRule rule, final String truthVal, final boolean isNegated, final boolean isNeutral) {
		DdnnfDateComparer literal = (DdnnfDateComparer) rule;
		String timeOrder = literal.getComparison();
		SimpleDate[] dates = literal.getDates();
		
		Matrix vector1 = VectorUtils.getVector(dates[0]);
		Matrix vector2 = VectorUtils.getVector(dates[1]);
		
		Matrix tempVec = vector1.minus(vector2);
		int rDim = vector1.getRowDimension();
		Matrix resultVec = new Matrix(rDim + 3, vector1.getColumnDimension());
		
		for (int r = 0; r < rDim; r++) {
			for (int c = 0; c < tempVec.getColumnDimension(); c++) {
				
				resultVec.set(r, c, resultVec.get(r, c));
		}}
		
		// Time order of the two dates.
		if (timeOrder == "before") { resultVec.set(rDim, 0, 0.25); }
		else if (timeOrder == "after") { resultVec.set(rDim, 0, 0.75); }
		else { resultVec.set(rDim, 0, 0.5); }
		
		// If truth value should be assigned or not.
		if (isNeutral) { resultVec.set(rDim+1, 0, 0.25); }
		else {  resultVec.set(rDim+1, 0, 0.25); }
		
		// What truth assignment should be assigned.
		if ((truthVal == "true" && !isNegated) || (truthVal == "false" && isNegated)) { 
			resultVec.set(rDim+1, 0, 0.25);
			
		} else if ((truthVal == "false" && !isNegated) || (truthVal == "true" && isNegated)) { 
			resultVec.set(rDim+1, 0, 0.75);
			
		} else { resultVec.set(rDim+1, 0, 0.5); }
		
		
		return resultVec;
	}
	/*
	private Matrix embedLiteral(/*final DdnnfGraph graph*) {
		int inputSize = 9;
		Matrix input = new Matrix(inputSize, 1);
		
		String litName;
		int[] firstDate = new int[3];
		int[] secondDate = new int[3];
		boolean isNegated;
		
		
		if (graph.getFormula() instanceof DdnnfDate) {
			DdnnfDate literal = (DdnnfDate) graph.getFormula();
			litName = literal.getName();
			firstDate = literal.getDate().getDateAsIntArray();
			secondDate = literal.getDate().getDateAsIntArray();
			isNegated = literal.isNegated();
			}
		else /*if (graph.getFormula() instanceof DdnnfDateComparer) {
			DdnnfDateComparer literal = (DdnnfDateComparer) graph.getFormula();
			litName = literal.getName();
			firstDate = literal.getDates()[0].getDateAsIntArray();
			secondDate = literal.getDates()[1].getDateAsIntArray();
			isNegated = literal.isNegated();
			}
		
		int iter = 0;
		input.set(iter++, 0, literalIDMap.get(litName));
		input.set(iter++, 0, firstDate[0]);
		input.set(iter++, 0, firstDate[1]);
		input.set(iter++, 0, firstDate[2]);
		input.set(iter++, 0, secondDate[0]);
		input.set(iter++, 0, secondDate[1]);
		input.set(iter++, 0, secondDate[2]);
		input.set(iter++, 0, negationMap.get(isNegated));
		
		for (int l = 0; l < networkDepth; l++) {
			/*
			DADTilde;
			Zleaf[l];
			Wleaf[l];
			
					
		}
	}*/

}
