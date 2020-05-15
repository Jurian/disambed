
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
 * @version 1.0
 * @since 13-05-2020
 */
public class SameDateLogic extends DateCompareLogic {
	
	protected DateLogic firstDay;
	protected DateLogic secondDay;
	protected boolean isDate;
	
	/**
	 * Constructor method without user-given name declaration.
	 * 
	 * @param term A LogicRule class representing the logic date format 
	 */
	public SameDateLogic(String firstDate, String secondDate) {
		super(firstDate, secondDate);
		compareDates();
		
		this.name = ("isSameDate(" + this.firstDay.getName() + ", " + this.secondDay.getName() + ")");
		this.str = ("isSameDate(" + this.firstDay.toString() + ", " + this.secondDay.toString() + ")");
	}
	
	/**
	 * Constructor method without user-given name declaration.
	 * 
	 * @param term A LogicRule class representing the logic date format 
	 * @param name The given name of the logic term defined by the user
	 */
	public SameDateLogic(String firstDate, String secondDate, String name) {
		super(firstDate, secondDate);
		compareDates();
		
		this.name = name;
		this.str = ("isSameDate(" + this.firstDay.toString() + ", " + this.secondDay.toString() + ")");
	}
	
	/**
	 * Constructor method with user-given name declaration.
	 * 
	 * @param term A LogicRule class representing the logic date format 
	 * @param term A LogicTerm class representing the negated logic term
	 */
	public SameDateLogic(SimpleDate firstDate, SimpleDate secondDate) {
		super(firstDate, secondDate);
		compareDates();

		this.name = ("isSameDate(" + this.firstDay.getName() + ", " + this.secondDay.getName() + ")");
		this.str = ("isSameDate(" + this.firstDay.toString() + ", " + this.secondDay.toString() + ")");
	}
	
	/**
	 * Constructor method with user-given name declaration.
	 * 
	 * @param term A LogicRule class representing the logic date format 
	 * @param name The given name of the logic term defined by the user
	 */
	public SameDateLogic(SimpleDate firstDate, SimpleDate secondDate, String name) {
		super(firstDate, secondDate);
		compareDates();
		
		this.name = name;
		this.str = ("isSameDate(" + this.firstDay.toString() + ", " + this.secondDay.toString() + ")");
	}
	
	/**
	 * Constructor method with user-given name declaration.
	 * 
	 * @param term A LogicRule class representing the logic date format 
	 * @param term A LogicTerm class representing the negated logic term
	 */
	public SameDateLogic(DateLogic firstDate, DateLogic secondDate) {
		super(firstDate, secondDate);
		compareDates();

		this.name = ("isSameDate(" + this.firstDay.getName() + ", " + this.secondDay.getName() + ")");
		this.str = ("isSameDate(" + this.firstDay.toString() + ", " + this.secondDay.toString() + ")");
	}
	
	/**
	 * Constructor method with user-given name declaration.
	 * 
	 * @param term A LogicRule class representing the logic date format 
	 * @param name The given name of the logic term defined by the user
	 */
	public SameDateLogic(DateLogic firstDate, DateLogic secondDate, String name) {
		super(firstDate, secondDate);
		compareDates();
		
		this.name = name;
		this.str = ("isSameDate(" + this.firstDay.toString() + ", " + this.secondDay.toString() + ")");
	}
	
	/**
	 * Inherited compare method to check if:
	 * 		The two dates are exactly the same
	 */
	protected void compareDates() {
		this.logicValue = this.firstDay.isSameDateAs(this.secondDay);
	}
	
}
