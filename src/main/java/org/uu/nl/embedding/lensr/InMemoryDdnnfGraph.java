package org.uu.nl.embedding.lensr;

import java.util.Map;
import java.util.Map.Entry;

import org.uu.nl.embedding.logic.Conjunction;
import org.uu.nl.embedding.logic.Disjunction;
import org.uu.nl.embedding.logic.LogicRule;
import org.uu.nl.embedding.logic.LogicTerm;
import org.uu.nl.embedding.logic.Negation;

import java.util.HashMap;

import grph.in_memory.InMemoryGrph;
import grph.properties.NumericalProperty;

/**
 * 
 * @author Euan Westenbroek
 *
 */
public class InMemoryDdnnfGraph {
	
	LogicRule f; // Formula as CNF 
	InMemoryDdnnfGraph leftChild =  null;
	InMemoryDdnnfGraph rightChild = null;
	
    int parent;
    int root;
	String logicType;
	HashMap<Integer, String> operatorMap = new HashMap<Integer, String>();
	HashMap<LogicRule, String> logicMap = new HashMap<LogicRule, String>();
	HashMap<Integer, LogicRule> intLogicMap = new HashMap<Integer, LogicRule>();

    int maxVertInt = -1;
    
    public InMemoryDdnnfGraph(LogicRule formula, InMemoryDdnnfGraph leftGrph, InMemoryDdnnfGraph rightGrph) {
    	f = formula;
    	this.leftChild = leftGrph;
    	this.rightChild = rightGrph;
    	generateGraph();
    	setLogicType();
    	setLogicMaps();
    }
    
    public InMemoryDdnnfGraph(LogicRule formula, InMemoryDdnnfGraph leftGrph) {
    	this(formula, leftGrph, null);
    }
    
    public InMemoryDdnnfGraph(LogicRule formula) {
    	this(formula, null, null);
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
	
	public HashMap<LogicRule, String> getLogicMap() {
		return this.logicMap;
	}
	
	public HashMap<Integer, LogicRule> getIntLogicMap() {
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
	}
	
	private void setChildGraph(InMemoryDdnnfGraph graph) {
		for(Map.Entry<Integer,String> entry : graph.getGraph().entrySet()) {
			this.operatorMap.put(entry.getKey(), entry.getValue());
			if(maxVertInt < entry.getKey()) { maxVertInt = entry.getKey(); }
		}
	}
	
	private void generateGraph() {
		if(this.leftChild != null) { setChildGraph(this.leftChild); this.leftChild.setChildInts(2); }
		if(this.rightChild != null) { setChildGraph(this.rightChild); this.rightChild.setChildInts(3); }
		addRootToGraph();
	}
	
	private void setLogicMaps() {
    	this.logicMap.put(this.f, this.logicType);
    	this.intLogicMap.put(this.root, this.f);
    	if(this.leftChild != null) {
	    	for(Map.Entry<LogicRule, String> entry : this.leftChild.getLogicMap().entrySet()) {
	    		this.logicMap.put(entry.getKey(), entry.getValue());
	    	}
	    	for(Map.Entry<Integer, LogicRule> entry : this.leftChild.getIntLogicMap().entrySet()) {
	    		this.intLogicMap.put(entry.getKey(), entry.getValue());
	    	}
    	}
    	if(this.rightChild != null) {
	    	for(Map.Entry<LogicRule, String> entry : this.rightChild.getLogicMap().entrySet()) {
	    		this.logicMap.put(entry.getKey(), entry.getValue());
	    	}
	    	for(Map.Entry<Integer, LogicRule> entry : this.rightChild.getIntLogicMap().entrySet()) {
	    		this.intLogicMap.put(entry.getKey(), entry.getValue());
	    	}
    	}
	}
    
    private String setLogicType() {
    			
    	if(this.f instanceof Conjunction) {
    		return "AND";
    	} else if(this.f instanceof Disjunction) {
    		return "OR";
    	} else if(this.f instanceof Negation) {
    		return "NOT";
    	} else if(this.f instanceof LogicTerm) {
    		return "Term";
    	} else {
    		throw new IllegalArgumentException("Invalid argument(s)."); 
    	}
    }
}
