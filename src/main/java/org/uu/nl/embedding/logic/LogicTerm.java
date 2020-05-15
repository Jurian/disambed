/**
 * 
 */
package org.uu.nl.embedding.logic;

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
	private String name;
	
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
	}
	
	/**
	 * @return Returns the Boolean value of the logic term
	 */
	public boolean getValue() {
		return this.firstTerm;
	}
	
	/**
	 * @return Returns the name of the logic term
	 */
	public String getName() {
		return this.name;
	}
	
	public String toString() {
		if(firstTerm) {
			return "true";
		} else {
			return "false";
		}
	}
	
	/**
	 * @return Returns an array with the logic term itself; 
	 * 		In this case it return "[this]" (i.e. self)
	 */
	public LogicRule[] getAllTerms() {
		LogicRule[] allTerms = new LogicRule[] {this};
		return allTerms;
	}
	

}
