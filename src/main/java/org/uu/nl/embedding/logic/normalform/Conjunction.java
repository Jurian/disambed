/**
 * 
 */
package org.uu.nl.embedding.logic.normalform;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.jena.graph.Graph;
import org.uu.nl.embedding.lensr.DdnnfGraph;
import org.uu.nl.embedding.logic.LogicRule;

/**
 * Class for conjunction logic formulae.
 * The conjunction logic formula is the Boolean value 
 * 		returning True if and only if both terms are True
 * 		else it returns False
 * 
 * @author Euan Westenbroek
 * @version 1.1
 * @since 12-05-2020
 */
public class Conjunction {

	protected LogicRule firstTerm;
	protected LogicRule secondTerm;
	private LogicRule inCnf;
	private LogicRule inDdnnf;
	
	protected boolean finalValue;
	private String nameSimple;
	private String nameCNF;
	private String nameDdnnf;
	private String strSimple;
	private DdnnfGraph ddnnfGraph;


	/*
	/**
	 * Constructor method with user-given name declaration.
	 * 
	 * @param firstTerm A LogicTerm class representing the first logic formula
	 * @param secondTerm A LogicTerm class representing the second logic formula
	 * @param name The given name of this logic formula defined by the user
	 *
	protected Conjunction(LogicRule firstTerm, LogicRule secondTerm, String name) {
		super();
		this.firstTerm = firstTerm;
		this.secondTerm = secondTerm;
		createFinalValue();
		createNameSimple();
		createNameCNF();
		createCnfRule();
		createDdnnfRule();
		generateDdnnfGraph();
	}
	
	/**
	 * Constructor method without user-given name declaration.
	 * 
	 * @param firstTerm A LogicTerm class representing the first logic formula
	 * @param secondTerm A LogicTerm class representing the second logic formula
	 *
	protected Conjunction(LogicRule firstTerm, LogicRule secondTerm) {
		this(firstTerm, secondTerm, null);
	}
	
	/**
	 * Sets the Boolean finalValue of this conjunctive logic formula.
	 */
	private void createFinalValue() {
		boolean finalVal;
		finalVal = (this.firstTerm.getAssignment() && this.secondTerm.getAssignment()); // A AND B
		
		this.finalValue = finalVal;
	}
	
	/**
	 * Sets the String represented name of the conjunction 
	 * 		in standard first-order logic form
	 *
	private void createNameSimple() {
		this.nameSimple = ("(" + this.firstTerm.getName() + " AND " + this.secondTerm.getName() + ")");
	}

	/**
	 * Sets the String represented name of the conjunction 
	 * 		in Conjunctive Normal Form (CNF)
	 *
	private void createNameCNF() {
		this.nameCNF = ("(" + this.firstTerm.getName() + "AND " + this.secondTerm.getName() + ")");
	}
	
	/**
	 * @return Returns the standard first-order logic name of this logic 
	 * 		formula (generated)
	 *
	public String getNameSimple() {
		return this.nameSimple;
	}
	/**
	 * @return Returns the simple form string of this logic 
	 * 		formula (generated)
	 *
	public String toString() {
		return this.strSimple;
	}
	
	/**
	 * Convert the conjunction to its CNF equivalent
	 *
	private void createCnfRule() {
		this.inCnf = new Conjunction(this.firstTerm.getCnfRule(), this.secondTerm.getCnfRule());
	}

	/**
	 * Convert the conjunction to its d-DNNF equivalent
	 *
	private void createDdnnfRule() {
		this.inDdnnf = new Conjunction(this.firstTerm.getDdnnfRule(), this.secondTerm.getDdnnfRule());
	}
	
	
	private void generateDdnnfGraph() {
		DdnnfGraph leftGraph = this.firstTerm.getDdnnfGraph();
		DdnnfGraph rightGraph = this.secondTerm.getDdnnfGraph();
		
		ddnnfGraph = new DdnnfGraph(this, leftGraph, rightGraph);
	}
	

	
	/*
	 * All interface methods implemented
	 *
	
	/**
	 * @return Returns the Boolean value of the logic term
	 *
	@Override
	public boolean getAssignment() {
		return this.finalValue;
	}
	
	/**
	 * @return Returns the name of the logic term (given or generated)
	 *
	@Override
	public String getName() {
		return this.nameSimple;
	}

	/**
	 * @return Returns the string of the logic term in CNF
	 *
	@Override
	public String getNameCNF() {
		return this.nameCNF;
	}

	/**
	 * @return Returns the string of the logic term in d-DNNF
	 *
	@Override
	public String getNameDdnnf() {
		return this.nameDdnnf;
	}
	
	/**
	 * @return Returns an array of all the basic logic terms themselves,
	 * 		without any logical operator; 
	 * 			In this case it returns all the basic 
	 * 			logic terms this.firstTerm is comprised of,
	 * 			as well as, all the basic logic terms
	 * 			this.secondTerm is comprised of
	 *
	public LogicRule[] getAllTerms() {
		LogicRule[] allTerms = this.firstTerm.getAllTerms(); 
		allTerms = ArrayUtils.addAll(allTerms,  this.secondTerm.getAllTerms());
		return allTerms;
	}
	
	/**
	 * @return Returns this LogicRule as Precedent
	 *
	@Override
	public LogicRule getPrecedent() {
		return this.firstTerm;
	}

	/**
	 * @return Returns this LogicRule as Antecedent
	 *
	@Override
	public LogicRule getAntecedent() {
		return this.secondTerm;
	}

	/**
	 * Returns this LogicRule in its CNF
	 *
	@Override
	public LogicRule getCnfRule() {
		return this.inCnf;
	}

	/**
	 * Returns this LogicRule in its d-DNNF
	 *
	@Override
	public LogicRule getDdnnfRule() {
		return this.inDdnnf;
	}

	/**
	 * Returns the logic graph of the d-DNNF
	 *
	@Override
	public DdnnfGraph getDdnnfGraph() {
		return this.ddnnfGraph;
	}
	*/
}
