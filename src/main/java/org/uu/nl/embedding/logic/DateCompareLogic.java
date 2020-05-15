/**
 * 
 */
package org.uu.nl.embedding.logic;

import org.uu.nl.embedding.logic.util.SimpleDate;

/**
 * Abstract class for time logic formulae.
 * The time logic formula defines time constraints
 * 		for the data. It represents the time 
 * 		constraints that apply in practice.
 * 
 * @author Euan Westenbroek
 * @version 1.0
 * @since 14-05-2020
 */
public abstract class DateCompareLogic implements LogicRule {
	
	protected DateLogic firstDay;
	protected DateLogic secondDay;
	protected boolean isDate;
	protected boolean logicValue;
	protected String name;
	protected String str;
	
	public DateCompareLogic(String firstDate, String secondDate) {
		this.firstDay = new DateLogic(firstDate);
		this.secondDay = new DateLogic(secondDate);
	}
	
	public DateCompareLogic(SimpleDate firstDate, SimpleDate secondDate) {
		this.firstDay = new DateLogic(firstDate);
		this.secondDay = new DateLogic(secondDate);
	}
	
	public DateCompareLogic(DateLogic firstDate, DateLogic secondDate) {
		super();
		this.firstDay = firstDate;
		this.secondDay = secondDate;
	}
	
	protected abstract void compareDates();
	
	/**
	 * @return Returns the Boolean value whether it is the same date (true) or not (false)
	 */
	public boolean getValue() {
		return this.logicValue;
	}
	
	/**
	 * @return Returns the name of the SameDate term (given or generated)
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * @return Returns the string of the date term (generated)
	 */
	public String toString() {
		return this.str;
	}
	
	/**
	 * @return Returns an array of only the strings of the DateLogic terms (generated),
	 * 				in the "dd-mm-yyyy" format
	 */
	public String[] getDateString() {
		return new String[] {
				this.firstDay.getDateString(),
				this.secondDay.getDateString()
		};
	}
	
	/**
	 * @return Returns an array of SimpleDates of this SameDateLogic (generated)
	 */
	public SimpleDate[] getSimpleDate() {
		return new SimpleDate[] {
				this.firstDay.getSimpleDate(),
				this.secondDay.getSimpleDate()
		};
	}
}
