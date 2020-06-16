package org.uu.nl.embedding.logic.normalform;

import org.apache.commons.lang3.ArrayUtils;
import org.uu.nl.embedding.lensr.DdnnfGraph;
import org.uu.nl.embedding.logic.LogicRule;

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
public class Disjunction  {

	protected LogicRule firstTerm;
	protected LogicRule secondTerm;
	private LogicRule inCnf;
	private LogicRule inDdnnf;
	protected boolean finalValue;
	private String nameSimple;
	private String nameCNF;
	private String nameDdnnf;
	private DdnnfGraph ddnnfGraph;

	/*
	
	/**
	 * Constructor method with user-given name declaration.
	 * 
	 * @param firstTerm A LogicTerm class representing the first logic formula
	 * @param secondTerm A LogicTerm class representing the second logic formula
	 * @param name The given name of this logic formula defined by the user
	 *
	protected Disjunction(LogicRule firstTerm, LogicRule secondTerm, String name) {
		super();
		this.firstTerm = firstTerm;
		this.secondTerm = secondTerm;
		createFinalValue();
		createNameSimple();
		createNameCNF();
		createNameDdnnf();
		createCnfRule();
		createDdnnfRule();
		generateDdnnfGraph();
	}
	
	/**
	 * Constructor method with user-given name declaration.
	 * 
	 * @param firstTerm A LogicTerm class representing the first logic formula
	 * @param secondTerm A LogicTerm class representing the second logic formula
	 *
	protected Disjunction(LogicRule firstTerm, LogicRule secondTerm) {
		this(firstTerm, secondTerm, null);
	}
	
	/**
	 * Sets the Boolean finalValue of this conjunctive logic formula.
	 *
	private void createFinalValue() {
		boolean finalVal;
		finalVal = (this.firstTerm.getAssignment() || this.secondTerm.getAssignment()); // A OR B
		
		this.finalValue = finalVal;
	}

	/**
	 * Convert the disjunction to its CNF equivalent
	 *
	private void createCnfRule() {
		this.inCnf = new Disjunction(this.firstTerm.getCnfRule(), this.secondTerm.getCnfRule());
	}
	
	private void createDdnnfRule() {
		boolean leftNeg = false , rightNeg = false;
		
		if(this.firstTerm.getDdnnfRule() instanceof Negation) { leftNeg = true; }
		if(this.secondTerm.getDdnnfRule() instanceof Negation) { rightNeg = true; }
		Disjunction resDisj;
		
		if(leftNeg && rightNeg) {
			Conjunction conj1 = new Conjunction(this.firstTerm.getPrecedent().getDdnnfRule(), this.secondTerm.getDdnnfRule());
			Conjunction conj2 = new Conjunction(this.firstTerm.getDdnnfRule(), this.secondTerm.getPrecedent().getDdnnfRule());
			resDisj = new Disjunction(conj1, conj2);
		}
		else if(!leftNeg && !rightNeg) {
			LogicRule neg1 = generateNegation(this.firstTerm.getDdnnfRule());
			LogicRule neg2 = generateNegation(this.secondTerm.getDdnnfRule());
			Conjunction conj1 = new Conjunction(neg1, this.secondTerm.getDdnnfRule());
			Conjunction conj2 = new Conjunction(this.firstTerm.getDdnnfRule(), neg2);
			resDisj = new Disjunction(conj1, conj2);
		}
		else if(leftNeg && !rightNeg) {
			Conjunction conj1 = new Conjunction(this.firstTerm.getDdnnfRule(), this.secondTerm.getDdnnfRule());
			resDisj = new Disjunction(conj1, this.secondTerm.getPrecedent().getDdnnfRule());
		}
		else {
			Conjunction conj2 = new Conjunction(this.firstTerm.getDdnnfRule(), this.secondTerm.getDdnnfRule());
			resDisj = new Disjunction(this.firstTerm.getPrecedent().getDdnnfRule(), conj2);
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
	 *
	private LogicRule generateNegation(LogicRule term) {
		
		if(term instanceof Negation) { return term.getAntecedent(); }
		else { return new Negation(term); }
	}
	
	/**
	 * Sets the String represented name of the conjunction 
	 * 		in standard first-order logic form
	 *
	private void createNameSimple() {
		this.nameSimple = ("(" + this.firstTerm.getName() + " OR " + this.secondTerm.getName() + ")");
	}

	/**
	 * Sets the String represented name of the conjunction 
	 * 		in Conjunctive Normal Form (CNF)
	 *
	private void createNameCNF() {
		this.nameCNF = ("(" + this.firstTerm.getName() + " OR " + this.secondTerm.getName() + ")");
	}

	/**
	 * Sets the String represented name of the conjunction 
	 * 		in Deterministic Decomposable Negation 
	 * 		Normal Form (d-DNNF)
	 *
	private void createNameDdnnf() {
		this.nameDdnnf = ("(" + this.inDdnnf.getName() + ")");
	}
	
	/**
	 * This method generates the d-DNNF graph
	 * of this LogicRule
	 *
	private void generateDdnnfGraph() {
		DdnnfGraph leftGraph = this.firstTerm.getDdnnfGraph();
		DdnnfGraph rightGraph = this.secondTerm.getDdnnfGraph();
		
		this.ddnnfGraph = new DdnnfGraph(this, leftGraph, rightGraph);
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
	 * 			logic terms this.firstTerm is comprised of
	 *
	@Override
	public LogicRule[] getAllTerms() {
		LogicRule[] allTerms = this.firstTerm.getAllTerms();
		allTerms = ArrayUtils.addAll(allTerms,  this.firstTerm.getAllTerms());
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
