/**
 * 
 */
package org.uu.nl.embedding.logic.normalform;

import org.apache.commons.lang3.ArrayUtils;
import org.uu.nl.embedding.lensr.DdnnfGraph;
import org.uu.nl.embedding.logic.LogicRule;
import org.uu.nl.embedding.logic.cnf.Clause;
import org.uu.nl.embedding.logic.cnf.CnfFormula;
import org.uu.nl.embedding.logic.cnf.CnfLogicRule;
import org.uu.nl.embedding.logic.cnf.LogicLiteral;

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
public class Implication {

	protected boolean assignment;
	
	protected NormalLogicRule firstTerm;
	protected NormalLogicRule secondTerm;
	
	private CnfLogicRule inCnf;
	private LogicRule inDdnnf;
	
	private String name;
	private String cnfName;
	private String ddnnfName;

	/*

	/**
	 * Constructor method with user-given name declaration.
	 * 
	 * @param value A LogicTerm class representing the first logic formula
	 * @param secondTerm A LogicTerm class representing the second logic formula
	 * @param name The given name of this logic formula defined by the user
	 *
	protected Implication(NormalLogicRule firsTerm, NormalLogicRule secondTerm) {
		super();
		this.firstTerm = firsTerm;
		this.secondTerm = secondTerm;
		
		generateCnf();
		generateDdnnf();
		
		createAssignment();
		createNameSimple();
		createNameCNF();
		createCnfRule();
		createDdnnfRule();
		generateDdnnfGraph();
	}
	
	/**
	 * Set the Boolean finalValue of this implicative logic formula.
	 *
	private void createAssignment() {
		boolean finalVal;
		finalVal = ((!this.firstTerm.getAssignment()) || this.secondTerm.getAssignment()); // (NOT A) OR B
		
		this.assignment = finalVal;
	}
	
	/**
	 * CNF generator for the constructor.
	 *
	private void generateCnf() {
		Clause tempClause = new Clause("temp", false, false);
		CnfFormula cnfImpl = new CnfFormula(tempClause);
		
		// Check type of first term and create the CNF formula with it.
		if(this.firstTerm.getCnfRule() instanceof CnfFormula) {
			cnfImpl = new CnfFormula((CnfFormula) this.firstTerm.getCnfRule(), this);
		}
		else if(this.firstTerm.getCnfRule() instanceof Clause) {
			cnfImpl = new CnfFormula((Clause) this.firstTerm.getCnfRule(), this);
		}
		else if(this.firstTerm.getCnfRule() instanceof LogicLiteral) {
			if(this.firstTerm.getCnfRule().toValueString().contains("NOT")) {
				tempClause = new Clause((LogicLiteral) this.firstTerm.getCnfRule(),
								true, this);
			}
			else { tempClause = new Clause((LogicLiteral) this.firstTerm.getCnfRule(), false, this); 
			}
			
			cnfImpl = new CnfFormula(tempClause, this);
		}
		
		// Now for the second term.
		if(this.secondTerm.getCnfRule() instanceof CnfFormula) {
			this.inCnf = cnfImpl.mergeWith((CnfFormula) this.secondTerm.getCnfRule());
		}
		else if(this.secondTerm.getCnfRule() instanceof Clause) {
			cnfImpl.addClause((Clause) this.secondTerm.getCnfRule());
			this.inCnf = cnfImpl;
		}
		else if(this.secondTerm.getCnfRule() instanceof LogicLiteral) {
			if(this.secondTerm.getCnfRule().toValueString().contains("NOT")) {
				tempClause = new Clause((LogicLiteral) this.secondTerm.getCnfRule(), true, this);
			}
			else { tempClause = new Clause((LogicLiteral) this.secondTerm.getCnfRule(), false, this); 
			}
			
			cnfImpl.addClause(tempClause);
			this.inCnf = cnfImpl;
		}
	}
	
	/**
	 * d-DNNF generator for the constructor.
	 *
	private void generateDdnnf() {
		this.inDdnnf = this.inCnf.getDdnnfRule();
	}

	/**
	 * Sets the String represented name of the implication
	 *
	private void createNameSimple() {
		this.name = ("(IF " + this.firstTerm.getName() + " THEN " + this.secondTerm.getName() + ")");
	}
	
	/**
	 * Sets the String represented name of the implication
	 * 		in Conjunctive Normal Form (CNF)
	 *
	private void createNameCNF() {
		this.cnfName = ("(NOT " + this.firstTerm.getName() + " OR " + this.secondTerm.getName() + ")");
	}
	
	/**
	 * Convert the implication to its CNF equivalent
	 *
	private void createCnfRule() {
		Negation notPrecedent = new Negation(this.firstTerm.getCnfRule());
		Disjunction cnfImplication = new Disjunction(notPrecedent, this.secondTerm.getCnfRule());
		this.inCnf = cnfImplication;
	}

	/**
	 * Convert the implication to its d-DNNF equivalent
	 *
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
			NormalLogicRule neg1 = generateNegation(implInDisj.firstTerm.getDdnnfRule());
			NormalLogicRule neg2 = generateNegation(implInDisj.secondTerm.getDdnnfRule());
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
	 *
	private NormalLogicRule generateNegation(NormalLogicRule term) {
		
		if(term instanceof Negation) { return term.getAntecedent(); }
		else { return new Negation(term); }
	}
	
	/**
	 * Generates the d-DNNF graph of this implication.
	 *
	private void generateDdnnfGraph() {
		
		DdnnfGraph negPrecGraph = this.inDdnnf.getPrecedent().getDdnnfGraph();
		DdnnfGraph trueAntGraph = this.inDdnnf.getAntecedent().getDdnnfGraph();
		
		//ddnnfGraph = new DdnnfGraph(this.inDdnnf, negPrecGraph, trueAntGraph);
	}

	
	/*
	 * All interface methods implemented
	 *
	
	/**
	 * @return Returns the Boolean value of the logic term
	 *
	@Override
	public boolean getAssignment() {
		return this.assignment;
	}

    /**
     * Sets the a new boolean assignment of
     * this logic rule.
     *
    @Override
    public void setAssignment(boolean assignment) {
        if(assignment) { setTrue(); }
        else { setFalse(); }
    }
    
    /**
     * Sets the assignment of this logic
     * rule to false
     *
	@Override
    public void setFalse() {
    	this.firstTerm.setTrue();
    	this.secondTerm.setFalse();
        this.assignment = false;
    }
    
    /**
     * Sets the assignment of this logic
     * rule to true
     *
    @Override
    public void setTrue() {
    	this.firstTerm.setFalse();
        this.assignment = true;
    }

    /**
     * @return Returns true if the assignment
     * has value false, else it returns false
     *
    @Override
    public boolean isFalse() {
        return !this.assignment;
    }

    /**
     * @return Returns true if the assignment
     * has value true, else it returns false
     *
    @Override
    public boolean isTrue() {
        return this.assignment;
    } 
	
	/**
	 * @return Returns the name of the logic term (given or generated)
	 *
	@Override
	public String getName() {
		return this.name;
	}

	/**
	 * @return Returns the string of the logic term in CNF
	 *
	@Override
	public String getCnfName() {
		return this.cnfName;
	}

	/**
	 * @return Returns the string of the logic term in d-DNNF
	 *
	@Override
	public String getDdnnfName() {
		return this.ddnnfName;
	}
	
    /**
     * @return Returns this LogicRule
     * as a string
     *
	public String toString() {
		return this.name;
	}
    
    /**
     * @return Returns this LogicRule as a string
     * and its term values as a string.
     *
	@Override
	public String toValueString() {
		return ("(IF " + this.firstTerm.toValueString() + " THEN " + this.secondTerm.toValueString() + ")");
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
	public NormalLogicRule getPrecedent() {
		return this.firstTerm;
	}
	
	/**
	 * @return Returns this LogicRule as Antecedent
	 *
	@Override
	public NormalLogicRule getAntecedent() {
		return this.secondTerm;
	}

	/**
	 * Returns this LogicRule in its CNF
	 *
	@Override
	public CnfLogicRule getCnfRule() {
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
		return this.inDdnnf.getDdnnfGraph();
	}
	*/
	
}
