package org.uu.nl.embedding.logic;

import org.apache.commons.lang3.ArrayUtils;
import org.uu.nl.embedding.lensr.InMemoryDdnnfGraph;

/**
 * Class for disjunction logic formulae.
 * The disjunction logic formula is the Boolean value 
 * 		returning True if one of both terms is True
 * 		else it returns False
 * 
 * @author Euan Westenbroek
 * @version 1.1
 * @since 12-05-2020
 */
public class Disjunction implements LogicRule {

	protected LogicRule firstTerm;
	protected LogicRule secondTerm;
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
	protected Disjunction(LogicRule firstTerm, LogicRule secondTerm, String name) {
		super();
		this.firstTerm = firstTerm;
		this.secondTerm = secondTerm;
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
	protected Disjunction(LogicRule firstTerm, LogicRule secondTerm) {
		this(firstTerm, secondTerm, null);
	}
	
	/**
	 * Sets the Boolean finalValue of this conjunctive logic formula.
	 */
	private void createFinalValue() {
		boolean finalVal;
		
		finalVal = (this.firstTerm.getValue() || this.secondTerm.getValue()); // A OR B
		
		this.finalValue = finalVal;
	}
	
	/**
	 * Sets the String represented name of the conjunction 
	 * 		in standard first-order logic form
	 */
	private void createNameSimple() {
		this.nameSimple = ("(" + this.firstTerm.getName() + " OR " + this.secondTerm.getName() + ")");
	}

	/**
	 * Sets the String represented name of the conjunction 
	 * 		in Conjunctive Normal Form (CNF)
	 */
	private void createNameCNF() {
		this.nameCNF = ("(" + this.firstTerm.getName() + " OR " + this.secondTerm.getName() + ")");
	}

	/**
	 * Sets the String represented name of the disjunction 
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
	 * @return Returns the Boolean value of this disjunctive logic formula
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
	
	public LogicRule getPrecedent() {
		return this.firstTerm;
	}
	
	public LogicRule getAntecedent() {
		return this.secondTerm;
	}

	private void generateDdnnfGraph() {
		InMemoryDdnnfGraph leftGraph = this.firstTerm.getDdnnfGraph();
		InMemoryDdnnfGraph rightGraph = this.secondTerm.getDdnnfGraph();
		
		ddnnfGraph = new InMemoryDdnnfGraph(this, leftGraph, rightGraph);
	}
	
	public InMemoryDdnnfGraph getDdnnfGraph() {
		return this.ddnnfGraph;
	}
	
}
