/**
 * 
 */
package org.uu.nl.embedding.logic;

import org.apache.commons.lang3.ArrayUtils;
import org.uu.nl.embedding.lensr.DdnnfGraph;

/**
 * Class for implication logic formulae.
 * The implication logic formula is the Boolean value
 * 		returning True if the antecedent is False,
 * 		returning True if the consequent is True,
 * 		else it returns False
 * 
 * @author Euan Westenbroek
 * @version 1.2
 * @since 12-05-2020
 */
public class Implication implements LogicRule {
	
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
	 * Constructor method with user-given name declaration.
	 * 
	 * @param value A LogicTerm class representing the first logic formula
	 * @param secondTerm A LogicTerm class representing the second logic formula
	 * @param name The given name of this logic formula defined by the user
	 */
	protected Implication(LogicRule term, LogicRule secondTerm) {
		super();
		this.firstTerm = term;
		createFinalValue();
		createNameSimple();
		createNameCNF();
		createCnfRule();
		createDdnnfRule();
		generateDdnnfGraph();
	}
	
	/**
	 * Sets the Boolean finalValue of this implicative logic formula.
	 */
	private void createFinalValue() {
		boolean finalVal;
		
		finalVal = ((!this.firstTerm.getAssignment()) || this.secondTerm.getAssignment()); // (NOT A) OR B
		
		this.finalValue = finalVal;
	}

	/**
	 * Sets the String represented name of the implication
	 */
	private void createNameSimple() {
		this.nameSimple = ("(IF " + this.firstTerm.getName() + " THEN " + this.secondTerm.getName() + ")");
	}
	
	/**
	 * Sets the String represented name of the implication
	 * 		in Conjunctive Normal Form (CNF)
	 */
	private void createNameCNF() {
		this.nameCNF = ("(NOT " + this.firstTerm.getName() + " OR " + this.secondTerm.getName() + ")");
	}
	
	/**
	 * Convert the implication to its CNF equivalent
	 */
	private void createCnfRule() {
		Negation notPrecedent = new Negation(this.firstTerm.getCnfRule());
		Disjunction cnfImplication = new Disjunction(notPrecedent, this.secondTerm.getCnfRule());
		this.inCnf = cnfImplication;
	}

	/**
	 * Convert the implication to its d-DNNF equivalent
	 */
	private void createDdnnfRule() {
		Disjunction implInDisj = (Disjunction)this.inCnf;
		boolean leftNeg = false , rightNeg = false;
		
		if(implInDisj.firstTerm.getDdnnfRule() instanceof Negation) { leftNeg = true; }
		if(implInDisj.secondTerm.getDdnnfRule() instanceof Negation) { rightNeg = true; }
		Disjunction resDisj;
		
		if(leftNeg && rightNeg) {
			Conjunction conj1 = new Conjunction(implInDisj.firstTerm.getPrecedent().getDdnnfRule(), implInDisj.secondTerm.getDdnnfRule());
			Conjunction conj2 = new Conjunction(implInDisj.firstTerm.getDdnnfRule(), implInDisj.secondTerm.getPrecedent().getDdnnfRule());
			resDisj = new Disjunction(conj1, conj2);
		}
		else if(!leftNeg && !rightNeg) {
			LogicRule neg1 = generateNegation(implInDisj.firstTerm.getDdnnfRule());
			LogicRule neg2 = generateNegation(implInDisj.secondTerm.getDdnnfRule());
			Conjunction conj1 = new Conjunction(neg1, implInDisj.secondTerm.getDdnnfRule());
			Conjunction conj2 = new Conjunction(implInDisj.firstTerm.getDdnnfRule(), neg2);
			resDisj = new Disjunction(conj1, conj2);
		}
		else if(leftNeg && !rightNeg) {
			Conjunction conj1 = new Conjunction(implInDisj.firstTerm.getDdnnfRule(), implInDisj.secondTerm.getDdnnfRule());
			resDisj = new Disjunction(conj1, implInDisj.secondTerm.getPrecedent().getDdnnfRule());
		}
		else {
			Conjunction conj2 = new Conjunction(implInDisj.firstTerm.getDdnnfRule(), implInDisj.secondTerm.getDdnnfRule());
			resDisj = new Disjunction(implInDisj.firstTerm.getPrecedent().getDdnnfRule(), conj2);
		}
		this.inDdnnf = resDisj;
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
	
	/**
	 * Generates the d-DNNF graph of this implication.
	 */
	private void generateDdnnfGraph() {
		
		DdnnfGraph negPrecGraph = this.inDdnnf.getPrecedent().getDdnnfGraph();
		DdnnfGraph trueAntGraph = this.inDdnnf.getAntecedent().getDdnnfGraph();
		
		ddnnfGraph = new DdnnfGraph(this.inDdnnf, negPrecGraph, trueAntGraph);
	}

	
	/*
	 * All interface methods implemented
	 */
	
	/**
	 * @return Returns the Boolean value of the logic term
	 */
	@Override
	public boolean getAssignment() {
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
