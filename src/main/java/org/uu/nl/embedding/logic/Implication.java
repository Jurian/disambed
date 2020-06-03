/**
 * 
 */
package org.uu.nl.embedding.logic;

import org.apache.commons.lang3.ArrayUtils;
import org.uu.nl.embedding.lensr.InMemoryDdnnfGraph;

/**
 * Class for implication logic formulae.
 * The implication logic formula is the Boolean value
 * 		returning True if the antecedent is False,
 * 		returning True if the consequent is True,
 * 		else it returns False
 * 
 * @author Euan Westenbroek
 * @version 1.1
 * @since 12-05-2020
 */
public class Implication implements LogicRule {
	
	protected LogicTerm firstTerm;
	protected LogicTerm secondTerm;
	protected boolean finalValue;
	private String nameGiven = null;
	private String nameSimple;
	private String nameCNF;
	private InMemoryDdnnfGraph ddnnfGraph;

	
	/**
	 * Constructor method with user-given name declaration.
	 * 
	 * @param firstTerm A LogicTerm class representing the first logic formula
	 * @param secondTerm A LogicTerm class representing the second logic formula
	 * @param name The given name of this logic formula defined by the user
	 */
	protected Implication(LogicTerm term, LogicTerm secondTerm, String name) {
		super();
		this.firstTerm = term;
		createFinalValue();
		determineNameGiven(name);
		createNameSimple();
		createNameCNF();
		generateDdnnfGraph();
	}

	/**
	 * Constructor method with user-given name declaration.
	 * 
	 * @param firstTerm A LogicTerm class representing the first logic formula
	 * @param secondTerm A LogicTerm class representing the second logic formula
	 */
	protected Implication(LogicTerm firstTerm, LogicTerm secondTerm) {
		this(firstTerm, secondTerm, null);
	}
	
	/**
	 * Sets the Boolean finalValue of this implicative logic formula.
	 */
	private void createFinalValue() {
		boolean finalVal;
		
		finalVal = ((!this.firstTerm.getValue()) || this.secondTerm.getValue()); // (NOT A) OR B
		
		this.finalValue = finalVal;
	}
	
	/**
	 * Sets the String represented name of the implication 
	 * 		in standard first-order logic form
	 */
	private void createNameSimple() {
		this.nameSimple = ("(" + this.firstTerm.getName() + " THEN " + this.secondTerm.getName() + ")");
	}

	/**
	 * Sets the String represented name of the implication
	 * 		in Conjunctive Normal Form (CNF)
	 */
	private void createNameCNF() {
		this.nameCNF = ("(NOT " + this.firstTerm.getName() + " OR " + this.secondTerm.getName() + ")");
	}

	/**
	 * Sets the String represented name of the implication 
	 * 		in either the user-given name form or else 
	 * 		in standard first-order logic form
	 */
	private void determineNameGiven(String name) {
		if(name != null) {
			this.nameGiven = name;
		}
		else {
			createNameSimple();
		}
	}
	
	/**
	 * @return Returns the Boolean value of this implicative logic formula
	 */
	public boolean getValue() {
		return this.finalValue;
	}
	
	/**
	 * @return Returns the name of this logic formula (given or generated)
	 */
	public String getName() {
		return this.nameGiven;
	}
	
	/**
	 * @return Returns the standard first-order logic name of this logic 
	 * 		formula (generated)
	 */
	public String getNameSimple() {
		return this.nameSimple;
	}
	
	/**
	 * @return Returns the Conjunctive Normal Form (CNF) name of this logic 
	 * 		formula (generated)
	 */
	public String getNameCNF() {
		return this.nameCNF;
	}
	
	/**
	 * @return Returns an array of all the basic logic terms themselves,
	 * 		without any logical operator; 
	 * 			In this case it returns all the basic 
	 * 			logic terms this.firstTerm is comprised of,
	 * 			as well as, all the basic logic terms
	 * 			this.secondTerm is comprised of
	 */
	public LogicRule[] getAllTerms() {
		LogicRule[] allTerms = this.firstTerm.getAllTerms(); 
		allTerms = ArrayUtils.addAll(allTerms,  this.secondTerm.getAllTerms());
		return allTerms;
	}
	
	/**
	 * @return Returns the precedent of this implication
	 */
	public LogicRule getPrecedent() {
		return this.firstTerm;
	}
	
	/**
	 * @return Returns the antecedent of this implication
	 */
	public LogicRule getAntecedent() {
		return this.secondTerm;
	}
	
	/**
	 * Convert the implication to its CNF equivalent
	 * @return Returns the newly created CNF LogicRule
	 */
	public LogicRule covertToCnfRule() {
		Negation notPrecedent = new Negation(this.firstTerm);
		Disjunction cnfImplication = new Disjunction(notPrecedent, this.secondTerm);
		return cnfImplication;
	}
	
	/**
	 * Generates the d-DNNF graph of this implication,
	 * To be able to create this graph, the implication
	 * should first be converted to CNF.
	 */
	private void generateDdnnfGraph() {
		LogicRule cnfImplication = covertToCnfRule();
		
		InMemoryDdnnfGraph negPrecGraph = cnfImplication.getPrecedent().getDdnnfGraph();
		InMemoryDdnnfGraph trueAntGraph = cnfImplication.getAntecedent().getDdnnfGraph();
		
		ddnnfGraph = new InMemoryDdnnfGraph(cnfImplication, negPrecGraph, trueAntGraph);
	}
	
	/**
	 * @return Returns the d-DNNF graph of this implication
	 * based on its CNF/d-DNNF.
	 */
	public InMemoryDdnnfGraph getDdnnfGraph() {
		return this.ddnnfGraph;
	}
	
}
