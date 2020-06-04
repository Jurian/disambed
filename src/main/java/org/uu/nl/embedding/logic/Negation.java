/**
 * 
 */
package org.uu.nl.embedding.logic;

import org.apache.commons.lang3.ArrayUtils;
import org.uu.nl.embedding.lensr.DdnnfGraph;

/**
 * Class for negation logic terms.
 * The negation logic term is the opposite Boolean value 
 * 		of the logic term following the negation
 * 
 * @author Euan Westenbroek
 * @version 1.0
 * @since 12-05-2020
 */
public class Negation implements LogicRule {
	
	protected LogicRule firstTerm;
	protected boolean finalValue;
	private String name = null;
	private String nameCnf;
	private String nameDdnnf;
	private String str;
	private LogicRule inCnf;
	private LogicRule inDdnnf;
	private DdnnfGraph ddnnfGraph;
	
	/**
	 * Constructor method.
	 * 
	 * @param term A LogicTerm class representing the negated logic term
	 */
	protected Negation(LogicRule term) {
		super();
		this.firstTerm = term;
		this.finalValue = !this.firstTerm.getValue();
		this.name = ("NOT " + this.firstTerm.getName());
		this.str = ("NOT " + this.firstTerm.toString());
		generateDdnnfGraph();
	}
	
	/**
	 * @return Returns the string of the logic term
	 */
	public String toString() {
		return this.str;
	}
	
	/**
	 * This method generates the d-DNNF graph
	 * of this LogicRule
	 */
	private void generateDdnnfGraph() {
		DdnnfGraph leftGraph = this.firstTerm.getDdnnfGraph();
		
		ddnnfGraph = new DdnnfGraph(this, leftGraph);
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
		return this.name;
	}

	/**
	 * @return Returns the string of the logic term in CNF
	 */
	@Override
	public String getNameCNF() {
		return this.nameCnf;
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
	 * 			logic terms this.firstTerm is comprised of
	 */
	@Override
	public LogicRule[] getAllTerms() {
		LogicRule[] allTerms = new LogicRule[] {};
		allTerms = ArrayUtils.addAll(allTerms,  this.firstTerm.getAllTerms());
		return allTerms;
	}
	
	/**
	 * @return Returns this LogicRule as Precedent
	 */
	@Override
	public LogicRule getPrecedent() {
		return this;
	}
	
	/**
	 * Placeholder for abstract method
	 * This method shouldn't be used
	 */
	@Override
	public LogicRule getAntecedent() {
		return this;
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
