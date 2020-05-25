
package org.uu.nl.embedding.logic;

import org.uu.nl.embedding.logic.util.SimpleDate;

/**
 * Class for time logic formulae.
 * This time logic formula compares date constraints
 * 		for the data. It represents the date 
 * 		constraints that apply in practice, where
 * 		two dates should be exactly the same.
 * 
 * @author Euan Westenbroek
 * @version 1.1
 * @since 13-05-2020
 */
public class ExactSameDateLogic extends DateCompareLogic {
	
	protected DateLogic firstDay;
	protected DateLogic secondDay;
	protected boolean isDate;
	
	/**
	 * Constructor method for undefined class
	 * WARNING: Solely use static methods!
	 */
	public ExactSameDateLogic() {
		/*
		 * This is a filler constructor to use for
		 * creating an undefined class initialization
		 * which makes it possible to use this class
		 * as a logic type only to use its static methods.
		 */ 
	}
	
	/**
	 * Constructor method without user-given name declaration.
	 * 
	 * @param firstDate A String representing the logic date format 
	 * @param secondDate A String representing the logic date format
	 */
	public ExactSameDateLogic(String firstDate, String secondDate) {
		super(firstDate, secondDate);
		compareTheseDates();
		
		this.name = ("isSameDate(" + this.firstDay.getName() + ", " + this.secondDay.getName() + ")");
		this.str = ("isSameDate(" + this.firstDay.toString() + ", " + this.secondDay.toString() + ")");
	}
	
	/**
	 * Constructor method without user-given name declaration.
	 * 
	 * @param firstDate A String representing the logic date format 
	 * @param secondDate A String representing the logic date format
	 * @param name The given name of the logic term defined by the user
	 */
	public ExactSameDateLogic(String firstDate, String secondDate, String name) {
		super(firstDate, secondDate);
		compareTheseDates();
		
		this.name = name;
		this.str = ("isSameDate(" + this.firstDay.toString() + ", " + this.secondDay.toString() + ")");
	}
	
	/**
	 * Constructor method with user-given name declaration.
	 * 
	 * @param term A LogicRule class representing the logic date format 
	 * @param term A LogicTerm class representing the negated logic term
	 */
	public ExactSameDateLogic(SimpleDate firstDate, SimpleDate secondDate) {
		super(firstDate, secondDate);
		compareTheseDates();

		this.name = ("isSameDate(" + this.firstDay.getName() + ", " + this.secondDay.getName() + ")");
		this.str = ("isSameDate(" + this.firstDay.toString() + ", " + this.secondDay.toString() + ")");
	}
	
	/**
	 * Constructor method with user-given name declaration.
	 * 
	 * @param term A LogicRule class representing the logic date format 
	 * @param name The given name of the logic term defined by the user
	 */
	public ExactSameDateLogic(SimpleDate firstDate, SimpleDate secondDate, String name) {
		super(firstDate, secondDate);
		compareTheseDates();
		
		this.name = name;
		this.str = ("isSameDate(" + this.firstDay.toString() + ", " + this.secondDay.toString() + ")");
	}
	
	/**
	 * Constructor method with user-given name declaration.
	 * 
	 * @param term A LogicRule class representing the logic date format 
	 * @param term A LogicTerm class representing the negated logic term
	 */
	public ExactSameDateLogic(DateLogic firstDate, DateLogic secondDate) {
		super(firstDate, secondDate);
		compareTheseDates();

		this.name = ("isSameDate(" + this.firstDay.getName() + ", " + this.secondDay.getName() + ")");
		this.str = ("isSameDate(" + this.firstDay.toString() + ", " + this.secondDay.toString() + ")");
	}
	
	/**
	 * Constructor method with user-given name declaration.
	 * 
	 * @param term A LogicRule class representing the logic date format 
	 * @param name The given name of the logic term defined by the user
	 */
	public ExactSameDateLogic(DateLogic firstDate, DateLogic secondDate, String name) {
		super(firstDate, secondDate);
		compareTheseDates();
		
		this.name = name;
		this.str = ("isSameDate(" + this.firstDay.toString() + ", " + this.secondDay.toString() + ")");
	}
	
	/**
	 * Inherited compare method to check if:
	 * 		The two dates are exactly the same
	 */
	@Override
	protected void compareTheseDates() {
		this.logicValue = this.firstDay.isSameDateAs(this.secondDay);
	}
	
	/**
	 * Static method two compare two date Strings
	 * 
	 * @param firstDate A String representing the logic date format 
	 * @param secondDate A String representing the logic date format
	 * @return 	boolean Returns true if the two Strings are exactly
	 * 			the same date, else return false
	 */
	public static boolean compareTwoDates(String firstDate, String secondDate, int daysDifference) {
		int ignore = daysDifference;

		DateLogic tempFirstDay = new DateLogic(firstDate);
		DateLogic tempSecondDay = new DateLogic(secondDate);
		
		return tempFirstDay.isSameDateAs(tempSecondDay);
	}
	
	
	
}
