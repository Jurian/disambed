
package org.uu.nl.embedding.logic;


import org.apache.commons.lang3.ArrayUtils;
import org.uu.nl.embedding.lensr.DdnnfGraph;
import org.uu.nl.embedding.logic.LogicRule;
import org.uu.nl.embedding.logic.util.SimpleDate;

/**
 * Class for time logic formulae.
 * The time logic formula defines time constraints
 * 		for the data. It represents the time 
 * 		constraints that apply in practice.
 * 
 * @author Euan Westenbroek
 * @version 1.1
 * @since 13-05-2020
 */
public class DateLogic implements LogicRule {
	
	protected SimpleDate firstDay;
	private LogicRule inCnf;
	private LogicRule inDdnnf;
	protected boolean isDate;
	
	private String name = null;
	private String str;
	private String nameCNF;
	private String nameDdnnf;
	
	private DdnnfGraph ddnnfGraph;
	
	/**
	 * Constructor method without user-given name declaration.
	 * 
	 * @param term A LogicRule class representing the logic date format 
	 * @param name The given name of the logic term defined by the user
	 */
	public DateLogic(String date, String name) {
		super();
		this.isDate = SimpleDate.isDateFormat(date);
		this.firstDay = new SimpleDate(date);
		this.name = name;
		this.str = ("DATE(" + date + ")");
		createCnfRule();
		createDdnnfRule();
		//generateDdnnfGraph();
	}
	
	/**
	 * Constructor method without user-given name declaration.
	 * 
	 * @param term A LogicRule class representing the logic date format 
	 */
	public DateLogic(String date) {
		this(date, null);
		this.name = ("DATE(" + date.toString() + ")");
	}
	
	/**
	 * Constructor method with user-given name declaration.
	 * 
	 * @param term A LogicRule class representing the logic date format 
	 * @param name The given name of the logic term defined by the user
	 */
	public DateLogic(SimpleDate date, String name) {
		super();
		this.isDate = this.firstDay.checkDateFormat(date.toString());
		
		this.firstDay = new SimpleDate(date.toString());
		this.name = name;
		this.str = ("DATE(" + date.toString() + ")");
		createCnfRule();
		createDdnnfRule();
	//	generateDdnnfGraph();
	}
	
	/**
	 * Constructor method with user-given name declaration.
	 * 
	 * @param term A LogicRule class representing the logic date format 
	 * @param term A LogicTerm class representing the negated logic term
	 */
	public DateLogic(SimpleDate date) {
		this(date, null);
		this.name = ("DATE(" + date.toString() + ")");
	}
	
	/**
	 * 
	 * @return Returns the date as an integer array as [dd, mm, yyyy]
	 */
	public int[] getDateAsIntArray() {
		return this.firstDay.getDateAsIntArray();
	}
	
	/**
	 * @return Returns the string of the date term (generated)
	 */
	@Override
	public String toString() {
		return this.str;
	}
	
	/**
	 * @return Returns only the strings of the date terms (generated),
	 * 				in the "dd-mm-yyyy" format
	 */
	public String getDateString() {
		return this.firstDay.toString();
	}
	
	/**
	 * @return Returns the SimpleDate of the date term (generated)
	 */
	public SimpleDate getSimpleDate() {
		return this.firstDay;
	}
	
	/**
	 * @param secondDate The other date this date should be compared to
	 * @return true when the dates are exactly the same
	 */
	public boolean isSameDateAs(final SimpleDate secondDate) {
		long days = this.firstDay.daysToDate(secondDate);
		if(days == (long)0) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * @param secondDate The other date this date should be compared to
	 * @return true when the dates are exactly the same
	 */
	public boolean isSameDateAs(final DateLogic secondDate) {
		long days = this.firstDay.daysToDate(secondDate.getSimpleDate());
		if(days == (long)0) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 
	 * @param secondDate The other date this date should be compared to
	 * @param maxDifferenceDays The maximal difference in number of days
	 * @return true when the dates are maximally maxDifferenceDays apart
	 */
	public boolean isApproxSameDateAs(final SimpleDate secondDate, final long maxDifferenceDays) {
		long days = Math.abs(this.firstDay.daysToDate(secondDate));
		if(days <= maxDifferenceDays) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * @param secondDate The other date this date should be compared to
	 * @param maxDifferenceDays The maximal difference in number of days
	 * @return true when the dates are maximally maxDifferenceDays apart
	 */
	public boolean isSameDateAs(final DateLogic secondDate, final long maxDifferenceDays) {
		long days = this.firstDay.daysToDate(secondDate.getSimpleDate());
		if(days <= maxDifferenceDays) {
			return true;
		} else {
			return false;
		}
	}
	
	
	/**
	 * 
	 * @param secondDate The other date this date should be compared to
	 * @param differenceDays The exact difference in number of days
	 * @return true when the dates are exactly differenceDays apart
	 */
	public boolean isExactDaysFrom(final SimpleDate secondDate, final long differenceDays) {
		long days = Math.abs(this.firstDay.daysToDate(secondDate));
		if(days == differenceDays) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * @param secondDate The other date this date should be compared to
	 * @param maxDifferenceDays The maximal difference in number of days
	 * @return true when the dates are maximally maxDifferenceDays apart
	 */
	public boolean isExactDaysFrom(final DateLogic secondDate, final long differenceDays) {
		long days = this.firstDay.daysToDate(secondDate.getSimpleDate());
		if(days == differenceDays) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 
	 * @param secondDate The other date this date should be compared to
	 * @return true when this date is before secondDate
	 */
	public boolean isBeforeDate(final SimpleDate secondDate) {
		long days = this.firstDay.daysToDate(secondDate);
		if(days > 0) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 
	 * @param secondDate The other date this date should be compared to
	 * @return true when this date is before secondDate
	 */
	public boolean isBeforeDate(final DateLogic secondDate) {
		long days = this.firstDay.daysToDate(secondDate.getSimpleDate());
		if(days > 0) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 
	 * @param secondDate The other date this date should be compared to
	 * @param maxDifferenceDays The maximal difference in number of days 
	 * @return true when the dates are maximally maxDifferenceDays apart
	 */
	public boolean isApproxBeforeDate(final SimpleDate secondDate, final long maxDifferenceDays) {
		long days = this.firstDay.daysToDate(secondDate);
		if(days > 0 && days <= maxDifferenceDays) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 
	 * @param secondDate The other date this date should be compared to
	 * @param maxDifferenceDays The maximal difference in number of days 
	 * @return true when the dates are maximally maxDifferenceDays apart
	 */
	public boolean isApproxBeforeDate(final DateLogic secondDate, final long maxDifferenceDays) {
		long days = this.firstDay.daysToDate(secondDate.getSimpleDate());
		if(days > 0 && days <= maxDifferenceDays) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 
	 * @param secondDate The other date this date should be compared to
	 * @param differenceDays The exact difference in number of days
	 * @return true when the dates are exactly differenceDays apart
	 */
	public boolean isExactBeforeDate(final SimpleDate secondDate, final long differenceDays) {
		long days = this.firstDay.daysToDate(secondDate);
		if(days == differenceDays) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 
	 * @param secondDate The other date this date should be compared to
	 * @param differenceDays The exact difference in number of days
	 * @return true when the dates are exactly differenceDays apart
	 */
	public boolean isExactBeforeDate(final DateLogic secondDate, final long differenceDays) {
		long days = this.firstDay.daysToDate(secondDate.getSimpleDate());
		if(days == differenceDays) {
			return true;
		} else {
			return false;
		}
	}
	/*
	private void generateDdnnfGraph() {
		ddnnfGraph = new DdnnfGraph(this);
	}

	/**
	 * Convert the implication to its CNF equivalent
	 */
	private void createCnfRule() {
		this.inCnf = this;
	}
	
	/**
	 * Convert the implication to its d-DNNF equivalent
	 */
	private void createDdnnfRule() {
		this.inDdnnf = this;
	}
	

	
	/*
	 * All interface methods implemented
	 */

	/**
	 * @return Returns the Boolean value if this term has a valid date format
	 */
	@Override
	public boolean getAssignment() {
		return this.isDate;
	}
	
	/**
	 * @return Returns the name of the date term (given or generated)
	 */
	@Override
	public String getName() {
		return this.name;
	}

	/**
	 * @return Returns an array with the logic date itself; 
	 * 		In this case it return "[this]" (i.e. self)
	 */
	public LogicRule[] getAllTerms() {
		LogicRule[] allTerms = new LogicRule[] {this};
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


	@Override
	public void setAssignment(boolean value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setFalse() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTrue() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isFalse() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isTrue() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String toValueString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCnfName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDdnnfName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int compareTo(LogicRule o) {
		// TODO Auto-generated method stub
		return 0;
	}

}