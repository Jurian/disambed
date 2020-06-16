package org.uu.nl.embedding.lensr;

import java.util.Map;
import java.util.Map.Entry;

import org.uu.nl.embedding.logic.ddnnf.DdnnfClause;
import org.uu.nl.embedding.logic.ddnnf.DdnnfFormula;
import org.uu.nl.embedding.logic.ddnnf.DdnnfLiteral;
import org.uu.nl.embedding.logic.ddnnf.DdnnfLogicRule;


import java.util.HashMap;

import grph.in_memory.InMemoryGrph;
import grph.properties.NumericalProperty;

/**
 * 
 * @author Euan Westenbroek
 *
 */
public class DdnnfGraph {
	
	DdnnfLogicRule f; // Formula as d-DNNF 
	DdnnfGraph leftChild =  null;
	DdnnfGraph rightChild = null;
	
    int parent;
    int root;
	String logicType;
	HashMap<Integer, String> operatorMap = new HashMap<Integer, String>();
	HashMap<DdnnfLogicRule, String> logicMap = new HashMap<DdnnfLogicRule, String>();
	HashMap<Integer, DdnnfLogicRule> intLogicMap = new HashMap<Integer, DdnnfLogicRule>();
	HashMap<Integer, DdnnfGraph> intGraphMap = new HashMap<Integer, DdnnfGraph>();

    int maxVertInt = -1;
    
    public DdnnfGraph(final DdnnfLogicRule formula, final DdnnfGraph leftGrph, final DdnnfGraph rightGrph) {
    	f = formula;
    	this.leftChild = leftGrph;
    	this.rightChild = rightGrph;
    	generateGraph();
    	getLogicType();
    	setLogicMaps();
    }
    
    public DdnnfGraph(DdnnfLogicRule formula, final DdnnfGraph leftGrph) {
    	this(formula, leftGrph, null);
    }
    
    public DdnnfGraph(DdnnfLogicRule formula) {
    	this(formula, null, null);
    }
    
    public DdnnfLogicRule getFormula() {
    	return this.f;
    }
	
	public void setChildInts(final int vertNumber) {
		this.root = vertNumber;

		if(this.leftChild != null) {
			this.leftChild.setChildInts(vertNumber+1);
			this.leftChild.setParent(this.root);
			if(this.rightChild != null) { 
				this.rightChild.setChildInts(vertNumber+2);
				this.leftChild.setParent(this.root);
				}
			
		} else if (this.rightChild != null) {
			this.rightChild.setChildInts(vertNumber+1);
			this.leftChild.setParent(this.root);
		}
	}
	
	public void setParent(final int parentNumber) {
		this.parent = parentNumber;
	}
	
	public HashMap<Integer,String> getGraph() {
		return this.operatorMap;
	}
	
	public HashMap<DdnnfLogicRule, String> getLogicMap() {
		return this.logicMap;
	}
	
	public HashMap<Integer, DdnnfLogicRule> getIntLogicMap() {
		return this.intLogicMap;
	}
	
	public int getThisRoot() {
		return this.root;
	}
	
	public int getParent() {
		return this.parent;
	}

	private void addRootToGraph() {
		this.root = 0;
		this.parent = -1;
		this.operatorMap.put(root, this.logicType);
		this.intGraphMap.put(root, this);
	}
	
	private void setChildGraph(DdnnfGraph graph) {
		for(Map.Entry<Integer,String> entry : graph.getGraph().entrySet()) {
			this.operatorMap.put(entry.getKey(), entry.getValue());
			if(maxVertInt < entry.getKey()) { maxVertInt = entry.getKey(); }
		}
	}
	
	private void generateGraph() {
		if(this.leftChild != null) { setChildGraph(this.leftChild); this.leftChild.setChildInts(1); }
		if(this.rightChild != null) { setChildGraph(this.rightChild); this.rightChild.setChildInts(2); }
		addRootToGraph();
		this.intGraphMap = getIntGraphMap();
	}
	
	private void setLogicMaps() {
    	this.logicMap.put(this.f, this.logicType);
    	this.intLogicMap.put(this.root, this.f);
    	if(this.leftChild != null) {
	    	for(Map.Entry<DdnnfLogicRule, String> entry : this.leftChild.getLogicMap().entrySet()) {
	    		this.logicMap.put(entry.getKey(), entry.getValue());
	    	}
	    	for(Map.Entry<Integer, DdnnfLogicRule> entry : this.leftChild.getIntLogicMap().entrySet()) {
	    		this.intLogicMap.put(entry.getKey(), entry.getValue());
	    	}
    	}
    	if(this.rightChild != null) {
	    	for(Map.Entry<DdnnfLogicRule, String> entry : this.rightChild.getLogicMap().entrySet()) {
	    		this.logicMap.put(entry.getKey(), entry.getValue());
	    	}
	    	for(Map.Entry<Integer, DdnnfLogicRule> entry : this.rightChild.getIntLogicMap().entrySet()) {
	    		this.intLogicMap.put(entry.getKey(), entry.getValue());
	    	}
    	}
	}
	
	private HashMap<Integer, DdnnfGraph> getIntGraphMap() {
		HashMap<Integer, DdnnfGraph> resMap = new HashMap<Integer, DdnnfGraph>();
		if(this.leftChild != null) {
			for(Map.Entry<Integer, DdnnfGraph> entry : this.leftChild.getIntGraphMap().entrySet()) {
				resMap.put(entry.getKey(), entry.getValue());
			}
		}
		if(this.rightChild != null) {
			for(Map.Entry<Integer, DdnnfGraph> entry : this.rightChild.getIntGraphMap().entrySet()) {
				resMap.put(entry.getKey(), entry.getValue());
			}
		}
		resMap.put(root, this);
		return resMap;
	}
	
	/*
	public HashMap<Integer, DdnnfGraph> getIntGraphMap() {
		return this.intGraphMap;
	}*/
    
    public String getLogicType() {
    			
    	if(this.f instanceof DdnnfFormula) {
    		return "AND";
    	} else if(this.f instanceof DdnnfClause) {
    		return "OR";
    	} else if(this.f instanceof DdnnfLiteral) {
    		return "Term";
    	} else {
    		System.out.print("This formula is not a d-DNNF formula, clause, or literal.");
    		return "Non";
    	}
    }
    
    public String printString() {
    	String resString = "";
    	if(this.leftChild == null && this.rightChild == null) {
    		resString += this.f.toString();
    	}
    	else if(this.leftChild == null) {
    		resString += "\n . " + getLogicType() + " . \n";
    		resString += this.rightChild.printString();
    	}
    	else if(this.rightChild == null) {
    		resString += "\n . " + getLogicType() + " . \n";
    		resString += this.leftChild.printString();
    	}
    	else {
    		resString += ("\n . " + getLogicType() + " . \n");
    		resString += (" . | \n");
    		resString += this.leftChild.printString() + " . . " + this.rightChild.printString();
    	}
    	return resString;
    }
	
	public void printMap() {
		System.out.println("\n");
		for(Map.Entry<Integer, DdnnfGraph> entry : this.intGraphMap.entrySet()) {
			System.out.println("key=" + entry.getKey() + ", value=" + entry.getValue().getLogicType());
		}
	}
	
	public void printMapWithValues() {
		boolean rightChild = false;
		String resString = "";
		for(Map.Entry<Integer, DdnnfGraph> entry : this.intGraphMap.entrySet()) {
			if(rightChild) {
	    		resString += ("\n . " + entry.getKey() + "-" + entry.getValue().getFormula().getName()  + " .");
	    		rightChild = false;
			} else {
	    		resString += (". " + entry.getKey() + "-" + entry.getValue().getFormula().getName() + " . \n");
	    		resString += (" . |..| .\n");
				rightChild = true;
			}
		}
		System.out.println("\n");
		System.out.println(resString);
	}
}
