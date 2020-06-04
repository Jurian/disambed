/**
 * 
 */
package org.uu.nl.embedding.logic;

import org.apache.commons.lang3.ArrayUtils;
import org.uu.nl.embedding.lensr.DdnnfGraph;

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
	private LogicRule inCnf;
	private LogicRule inDdnnf;
	
	protected boolean finalValue;
	
	private String nameSimple;
	private String nameCNF;
	private String nameDdnnf;
	
	private DdnnfGraph ddnnfGraph;

	
	/**
	 * Constructor method.
	 * 
	 * @param firstTerm A LogicTerm class representing the first logic formula
	 * @param secondTerm A LogicTerm class representing the second logic formula
	 * @param name The given name of this logic formula defined by the user
	 */
	protected Bicondition(LogicTerm firstTerm, LogicTerm secondTerm) {
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
	 * @return Returns the standard first-order logic name of this logic 
	 * 		formula (generated)
	 */
	public String getNameSimple() {
		return this.nameSimple;
	}
	

	/**
	 * Convert the bicondition to its CNF equivalent
	 */
	private void createCnfRule() {
		
		LogicRule notPrecedent = generateNegation(this.firstTerm.getCnfRule());
		LogicRule notAntecedent = generateNegation(this.secondTerm.getCnfRule());
		Conjunction cnfBothNot = new Conjunction(notPrecedent, notAntecedent);
		Conjunction cnfBothTrue = new Conjunction(this.firstTerm.getCnfRule(), this.secondTerm.getCnfRule());
		Disjunction cnfBicondition = new Disjunction(cnfBothNot, cnfBothTrue);
		
		this.inCnf = cnfBicondition;
	}

	/**
	 * Convert the bicondition to its d-DNNF equivalent
	 */
	private void createDdnnfRule() {
		
		Conjunction resConj;
		
		Implication firstImpl = new Implication(this.firstTerm, this.secondTerm); // Gaat dit goed zonder .getDdnnfRule()?
		Implication secondImpl = new Implication(this.secondTerm, this.firstTerm);
		resConj = new Conjunction(firstImpl.getDdnnfRule(), secondImpl.getDdnnfRule());
		
		this.inDdnnf = resConj;
	}
	
	private void generateDdnnfGraph() {
		LogicRule cnfBicondition = this.inDdnnf;
		
		DdnnfGraph negPrecGraph = cnfBicondition.getPrecedent().getPrecedent().getDdnnfGraph();
		DdnnfGraph negAntGraph = cnfBicondition.getPrecedent().getAntecedent().getDdnnfGraph();
		DdnnfGraph bothNotGraph = new DdnnfGraph(cnfBicondition.getPrecedent(), negPrecGraph, negAntGraph);
		
		DdnnfGraph truePrecGraph = cnfBicondition.getAntecedent().getPrecedent().getDdnnfGraph();
		DdnnfGraph trueAntGraph = cnfBicondition.getAntecedent().getPrecedent().getDdnnfGraph();
		DdnnfGraph bothTrueGraph = new DdnnfGraph(cnfBicondition.getAntecedent(), truePrecGraph, trueAntGraph);
		
		ddnnfGraph = new DdnnfGraph(cnfBicondition, bothNotGraph, bothTrueGraph);
	}

	/**
	 * Method to correct for double negations
	 * (Solely for readability if necessary)
	 * 
	 * @param term The LogicRule to be checked
	 * @return Returns a negation if term was not a negation,
	 * 			else it returns the only the term of the negation
	 */
	private LogicRule generateNegation(LogicRule term) {
		
		if(term instanceof Negation) { return term.getAntecedent(); }
		else { return new Negation(term); }
	}
	
	
	/*
	 * All interface methods implemented
	 */
	
	/**
	 * @return Returns the Boolean value of the logic term
	 */
	@Override
	public boolean getValue() {
		return this.finalValue;
	}
	
	/**
	 * @return Returns the name of the logic term (given or generated)
	 */
	@Override
	public String getName() {
		return this.nameSimple;
	}

	/**
	 * @return Returns the string of the logic term in CNF
	 */
	@Override
	public String getNameCNF() {
		return this.nameCNF;
	}

	/**
	 * @return Returns the string of the logic term in d-DNNF
	 */
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
	 */
	public LogicRule[] getAllTerms() {
		LogicRule[] allTerms = this.firstTerm.getAllTerms(); 
		allTerms = ArrayUtils.addAll(allTerms,  this.secondTerm.getAllTerms());
		return allTerms;
	}
	
	/**
	 * @return Returns this LogicRule as Precedent
	 */
	@Override
	public LogicRule getPrecedent() {
		return this.firstTerm;
	}
	
	/**
	 * @return Returns this LogicRule as Antecedent
	 */
	@Override
	public LogicRule getAntecedent() {
		return this.secondTerm;
	}

	/**
	 * Returns this LogicRule in its CNF
	 */
	@Override
	public LogicRule getCnfRule() {
		return this.inCnf;
	}

	/**
	 * Returns this LogicRule in its d-DNNF
	 */
	@Override
	public LogicRule getDdnnfRule() {
		return this.inDdnnf;
	}

	/**
	 * Returns the logic graph of the d-DNNF
	 */
	@Override
	public DdnnfGraph getDdnnfGraph() {
		return this.ddnnfGraph;
	}
	
	
}
