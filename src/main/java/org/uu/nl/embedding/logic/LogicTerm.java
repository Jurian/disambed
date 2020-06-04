/**
 * 
 */
package org.uu.nl.embedding.logic;

import org.apache.commons.lang3.ArrayUtils;
import org.uu.nl.embedding.lensr.DdnnfGraph;

/**
 * Class for simple logic terms.
 * The simple logic term is the most basic term class
 * Example of this are: hasName(Jan) (Boolean)
 * 
 * @author Euan Westenbroek
 * @version 1.0
 * @since 12-05-2020
 */
public class LogicTerm implements LogicRule {

	protected boolean firstTerm;
	private LogicRule inCnf;
	private LogicRule inDdnnf;
	
	private String name = null;
	private String nameCnf;
	private String nameDdnnf;
	
	private DdnnfGraph ddnnfGraph;
	
	/**
	 * Constructor method with user-given name declaration.
	 * 
	 * @param term A Boolean value representing the logic term
	 * @param name The name of the logic term
	 */
	public LogicTerm(boolean term, String name) {
		super();
		this.firstTerm = term;
		this.name = name;
		this.nameCnf = name;
		this.nameDdnnf = name;
		generateDdnnfGraph();
	}
	
	/**
	 * Constructor method without user-given name declaration.
	 * 
	 * @param term A Boolean value representing the logic term
	 */
	public LogicTerm(boolean term) {
		this(term, String.valueOf(term));
	}
	
	public String toString() {
		if(firstTerm) {
			return "true";
		} else {
			return "false";
		}
	}
	
	private void generateDdnnfGraph() {
		ddnnfGraph = new DdnnfGraph(this);
	}

	
	/*
	 * All interface methods implemented
	 */
	
	/**
	 * @return Returns the Boolean value of the logic term
	 */
	@Override
	public boolean getValue() {
		return this.firstTerm;
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
	 * @return Returns an array with the logic term itself; 
	 * 		In this case it return "[this]" (i.e. self)
	 */
	public LogicRule[] getAllTerms() {
		LogicRule[] allTerms = new LogicRule[] {this};
		return allTerms;
	}
	
	/**
	 * @return Returns this LogicRule as Precedent
	 */
	public LogicRule getPrecedent() {
		return this;
	}
	
	/**
	 * Placeholder for abstract method
	 * This method shouldn't be used
	 */
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
