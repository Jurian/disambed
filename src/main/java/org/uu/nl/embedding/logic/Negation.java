/**
 * 
 */
package org.uu.nl.embedding.logic;

import org.apache.commons.lang3.ArrayUtils;

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
	
	protected LogicTerm firstTerm;
	protected boolean finalValue;
	private String name;
	private String str;
	
	/**
	 * Constructor method without user-given name declaration.
	 * 
	 * @param term A LogicTerm class representing the negated logic term 
	 */
	protected Negation(LogicTerm term) {
		super();
		this.firstTerm = term;
		this.finalValue = !this.firstTerm.getValue(); // NOT A
		this.name = ("NOT " + this.firstTerm.getName());
		this.str =  ("NOT " + this.firstTerm.toString());
	}
	
	/**
	 * Constructor method with user-given name declaration.
	 * 
	 * @param term A LogicTerm class representing the negated logic term
	 * @param name The given name of the logic term defined by the user
	 */
	protected Negation(LogicTerm term, String name) {
		super();
		this.firstTerm = term;
		this.finalValue = !this.firstTerm.getValue();
		this.name = name;
		this.str =  ("NOT " + this.firstTerm.toString());
	}
	
	/**
	 * @return Returns the Boolean value of the logic term
	 */
	public boolean getValue() {
		return this.finalValue;
	}
	
	/**
	 * @return Returns the name of the logic term (given or generated)
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * @return Returns the string of the logic term
	 */
	public String toString() {
		return this.str;
	}
	
	/**
	 * @return Returns an array of all the basic logic terms themselves,
	 * 		without any logical operator; 
	 * 			In this case it returns all the basic 
	 * 			logic terms this.firstTerm is comprised of
	 */
	public LogicRule[] getAllTerms() {
		LogicRule[] allTerms = new LogicRule[] {};
		allTerms = ArrayUtils.addAll(allTerms,  this.firstTerm.getAllTerms());
		return allTerms;
	}

}
