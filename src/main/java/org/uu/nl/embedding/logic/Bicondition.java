/**
 * 
 */
package org.uu.nl.embedding.logic;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Class for bicondition logic formulae.
 * The bicondition logic formula is the Boolean value 
 * 		returning True if and only if both terms are True,
 * 		returning True if and only if both terms are False,
 * 		else it returns False
 * 
 * @author Euan Westenbroek
 * @version 1.0
 * @since 12-05-2020
 */
public class Bicondition implements LogicRule {
	
	protected LogicTerm firstTerm;
	protected LogicTerm secondTerm;
	protected boolean finalValue;
	private String nameGiven;
	private String nameSimple;
	private String nameCNF;

	
	/**
	 * Constructor method with user-given name declaration.
	 * 
	 * @param firstTerm A LogicTerm class representing the first logic formula
	 * @param secondTerm A LogicTerm class representing the second logic formula
	 */
	protected Bicondition(LogicTerm firstTerm, LogicTerm secondTerm) {
		super();
		this.firstTerm = firstTerm;
		this.secondTerm = secondTerm;
		createFinalValue();
		determineNameGiven("None");
		createNameSimple();
		createNameCNF();
	}

	/**
	 * Constructor method with user-given name declaration.
	 * 
	 * @param firstTerm A LogicTerm class representing the first logic formula
	 * @param secondTerm A LogicTerm class representing the second logic formula
	 * @param name The given name of this logic formula defined by the user
	 */
	protected Bicondition(LogicTerm term, LogicTerm secondTerm, String name) {
		super();
		this.firstTerm = term;
		createFinalValue();
		determineNameGiven(name);
		createNameSimple();
		createNameCNF();
	}
	
	/**
	 * Sets the Boolean finalValue of this biconditional logic formula.
	 */
	private void createFinalValue() {
		boolean finalVal;
		
		boolean firstDist = (!this.firstTerm.getValue()) && (!this.secondTerm.getValue()); // NOT A AND NOT B = C
		boolean secondDist = (this.firstTerm.getValue()) && (this.secondTerm.getValue()); // A AND B = D
		finalVal = (firstDist || secondDist); // C OR D
		
		this.finalValue = finalVal;
	}
	
	/**
	 * Sets the String represented name of the bicondition 
	 * 		in standard first-order logic form
	 */
	private void createNameSimple() {
		this.nameSimple = ("(IF AND ONLY IF " + this.firstTerm.getName() + " THEN " + this.secondTerm.getName() + ")");
	}

	/**
	 * Sets the String represented name of the bicondition 
	 * 		in Conjunctive Normal Form (CNF)
	 */
	private void createNameCNF() {
		String firstDist = ("(NOT " + this.firstTerm.getName() + " AND NOT " + this.secondTerm.getName() + ")");
		String secondDist = ("( " + this.firstTerm.getName() + " AND " + this.secondTerm.getName() + ")");
		
		this.nameCNF = ("(" + firstDist + " OR " + secondDist + ")");
	}

	/**
	 * Sets the String represented name of the bicondition 
	 * 		in either the user-given name form or else 
	 * 		in standard first-order logic form
	 */
	private void determineNameGiven(String name) {
		if(name != "None") {
			this.nameGiven = name;
		}
		else {
			createNameSimple();
		}
	}
	
	/**
	 * @return Returns the Boolean value of this biconditional logic formula
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
	
}
