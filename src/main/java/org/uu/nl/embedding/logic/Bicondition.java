/**
 * 
 */
package org.uu.nl.embedding.logic;

import org.apache.commons.lang3.ArrayUtils;
import org.uu.nl.embedding.lensr.InMemoryDdnnfGraph;

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
	protected Bicondition(LogicTerm firstTerm, LogicTerm secondTerm, String name) {
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
	protected Bicondition(LogicTerm firstTerm, LogicTerm secondTerm) {
		this(firstTerm, secondTerm, null);
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
		if(name != null) {
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
	
	public LogicRule getPrecedent() {
		return this.firstTerm;
	}
	
	public LogicRule getAntecedent() {
		return this.secondTerm;
	}
	
	public LogicRule covertToCnfRule() {
		Negation notPrecedent = new Negation(this.firstTerm);
		Negation notAntecedent = new Negation(this.secondTerm);
		Conjunction cnfBothNot = new Conjunction(notPrecedent, notAntecedent);
		Conjunction cnfBothTrue = new Conjunction(this.firstTerm, this.secondTerm);
		Disjunction cnfBicondition = new Disjunction(cnfBothNot, cnfBothTrue);
		return cnfBicondition;
	}
	
	private void generateDdnnfGraph() {
		LogicRule cnfBicondition = covertToCnfRule();
		
		InMemoryDdnnfGraph negPrecGraph = cnfBicondition.getPrecedent().getPrecedent().getDdnnfGraph();
		InMemoryDdnnfGraph negAntGraph = cnfBicondition.getPrecedent().getAntecedent().getDdnnfGraph();
		InMemoryDdnnfGraph bothNotGraph = new InMemoryDdnnfGraph(cnfBicondition.getPrecedent(), negPrecGraph, negAntGraph);
		
		InMemoryDdnnfGraph truePrecGraph = cnfBicondition.getAntecedent().getPrecedent().getDdnnfGraph();
		InMemoryDdnnfGraph trueAntGraph = cnfBicondition.getAntecedent().getPrecedent().getDdnnfGraph();
		InMemoryDdnnfGraph bothTrueGraph = new InMemoryDdnnfGraph(cnfBicondition.getAntecedent(), truePrecGraph, trueAntGraph);
		
		ddnnfGraph = new InMemoryDdnnfGraph(cnfBicondition, bothNotGraph, bothTrueGraph);
	}
	
	public InMemoryDdnnfGraph getDdnnfGraph() {
		return this.ddnnfGraph;
	}
	
}
