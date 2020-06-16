
package org.uu.nl.embedding.logic;

import org.apache.commons.lang3.ArrayUtils;
import org.uu.nl.embedding.lensr.DdnnfGraph;
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

	protected boolean isDate;
	
	private String nameSimple;
	private String nameCNF;
	private String nameDdnnf;
	
	protected DateLogic firstDay;
	protected DateLogic secondDay;
	private LogicRule inCnf;
	private LogicRule inDdnnf;
	
	private DdnnfGraph ddnnfGraph;
	
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
	 * @param name The given name of the logic term defined by the user
	 */
	public ExactSameDateLogic(String firstDate, String secondDate, String name) {
		super(firstDate, secondDate);
		compareTheseDates();
		
		this.name = name;
		this.str = ("isSameDate(" + this.firstDay.toString() + ", " + this.secondDay.toString() + ")");
		//generateDdnnfGraph();
	}
	
	/**
	 * Constructor method without user-given name declaration.
	 * 
	 * @param firstDate A String representing the logic date format 
	 * @param secondDate A String representing the logic date format
	 */
	public ExactSameDateLogic(String firstDate, String secondDate) {
		this(firstDate, secondDate, null);
		this.name = ("isSameDate(" + this.firstDay.getName() + ", " + this.secondDay.getName() + ")");
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
		//generateDdnnfGraph();
	}

	/**
	 * Constructor method with user-given name declaration.
	 * 
	 * @param term A LogicRule class representing the logic date format 
	 * @param term A LogicTerm class representing the negated logic term
	 */
	public ExactSameDateLogic(SimpleDate firstDate, SimpleDate secondDate) {
		this(firstDate, secondDate, null);
		this.name = ("isSameDate(" + this.firstDay.getName() + ", " + this.secondDay.getName() + ")");
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
		//generateDdnnfGraph();
	}
	
	/**
	 * Constructor method with user-given name declaration.
	 * 
	 * @param term A LogicRule class representing the logic date format 
	 * @param term A LogicTerm class representing the negated logic term
	 */
	public ExactSameDateLogic(DateLogic firstDate, DateLogic secondDate) {
		this(firstDate, secondDate, null);
		this.name = ("isSameDate(" + this.firstDay.getName() + ", " + this.secondDay.getName() + ")");
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
		// int ignore = daysDifference;

		DateLogic tempFirstDay = new DateLogic(firstDate);
		DateLogic tempSecondDay = new DateLogic(secondDate);
		
		return tempFirstDay.isSameDateAs(tempSecondDay);
	}
	/**
	 * Static method two compare two date Strings
	 * 
	 * @param firstDate A String representing the logic date format 
	 * @param secondDate A String representing the logic date format
	 * @return 	boolean Returns true if the two Strings are exactly
	 * 			the same date, else return false
	 */
	public static boolean compareTwoDates(DateLogic firstDate, DateLogic secondDate, int daysDifference) {
		// int ignore = daysDifference;
		
		return firstDate.isSameDateAs(secondDate);
	}
	/*
	private void generateDdnnfGraph() {
		DdnnfGraph leftGraph = this.firstDay.getDdnnfGraph();
		DdnnfGraph rightGraph = this.secondDay.getDdnnfGraph();
		
		ddnnfGraph = new DdnnfGraph(this, leftGraph, rightGraph);
	}


	
	/*
	 * All interface methods implemented
	 */
	
	/**
	 * @return Returns the Boolean value of the logic term
	 */
	@Override
	public boolean getAssignment() {
		return this.isDate;
	}
	
	/**
	 * @return Returns the name of the logic term (given or generated)
	 */
	@Override
	public String getName() {
		return this.nameSimple;
	}

	/**
	 * @return Returns an array of all the basic logic terms themselves,
	 * 		without any logical operator; 
	 * 			In this case it returns all the basic 
	 * 			logic terms this.firstTerm is comprised of,
	 * 			as well as, all the basic logic terms
	 * 			this.secondTerm is comprised of
	 */
	public LogicRule[] getAllTerms() {
		LogicRule[] allTerms = this.firstDay.getAllTerms(); 
		allTerms = ArrayUtils.addAll(allTerms,  this.secondDay.getAllTerms());
		return allTerms;
	}
	
	/**
	 * @return Returns this LogicRule as Precedent
	 */
	@Override
	public LogicRule getPrecedent() {
		return this.firstDay;
	}
	
	/**
	 * @return Returns this LogicRule as Antecedent
	 */
	@Override
	public LogicRule getAntecedent() {
		return this.secondDay;
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
