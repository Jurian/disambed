package org.uu.nl.embedding.logic;

import org.uu.nl.embedding.lensr.DdnnfGraph;
import org.uu.nl.embedding.logic.Clause;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Interface class for logic literals 
 * to be used in CNF.
 * 
 * DISCLAIMER: This class is partly based 
 * on the code by Rodion "rodde" Efremov.
 * 
 * @author Euan Westenbroek
 * @version 1.0
 * @since 07-06-2020
 */
public class LogicLiteral implements CnfLogicRule {

	private boolean assignment;
	private boolean negated;

	private LogicRule inNf;
	private LogicRule inCnf;
	private LogicRule inDdnnf;
	
	private String name = null;
	private String cnfName;
	private String ddnnfName;
	
	private DdnnfGraph ddnnfGraph;
	
	
	/**
	 * Constructor method for the literal class.
	 * @param name The name of this literal, e.g. "p", 
	 * or "isCar".
	 * @param value The starting boolean truth value of 
	 * this literal.
	 * @param negated The boolean value stating if this
	 * literal should, or shouldn't be negated.
	 */
	public LogicLiteral(final String name, boolean value, boolean negated) {
		this.name = name;
		this.assignment = value;
		this.negated = negated;
		
		this.cnfName = name;
		this.ddnnfName = name;
	}
	
	/**
	 * Constructor method for the literal class that 
	 * assumes that the literal should have its given
	 * value.
	 * @param name The name of this literal, e.g. "p", 
	 * or "isCar".
	 * @param value The starting boolean truth value of 
	 * this literal.
	 */
	public LogicLiteral(final String name, boolean value) {
		this(name, value, !value);
	}
	
	
	
	/*
	 * Below are all interface methods implemented
	 */

    /**
     * Sets the a new boolean assignment of
     * this logic rule.
     */
    @Override
    public void setAssignment(boolean assignment) {
        this.assignment = assignment;
    }
    
    /**
     * @return Returns the current assignment of
     * this logic rule
     */
    @Override
    public boolean getAssignment() {
        return assignment;
    }
    
    /**
     * Sets the assignment of this logic
     * rule to false
     */
	@Override
    public void setFalse() {
        this.assignment = false;
    }
    
    /**
     * Sets the assignment of this logic
     * rule to true
     */
    @Override
    public void setTrue() {
        this.assignment = true;
    }

    /**
     * @return Returns true if the assignment
     * has value false, else it returns false
     */
    @Override
    public boolean isFalse() {
        return !assignment;
    }

    /**
     * @return Returns true if the assignment
     * has value true, else it returns false
     */
    @Override
    public boolean isTrue() {
        return assignment;
    }
    
    /**
     * @return Returns the name of this literal
     */
    @Override
    public String getName() {
    	return this.name;
    }
    
    /**
     * @return Returns this literal's value
     * as a string
     */
    @Override
    public String toString() {
    	return this.name;
    }
    
    /**
     * @return Returns this literal's value
     * as a string
     */
	@Override
	public String toValueString() {
    	return String.valueOf(this.assignment);
	}
    
    /**
     * @return Returns the name in CNF of this
     * literal
     */
    @Override
    public String getCnfName() {
    	return this.cnfName;
    }
    
    /**
     * @return Returns the name in d-DNNF of this
     * literal
     */
    @Override
    public String getDdnnfName() {
    	return this.ddnnfName;
    }
    
    /**
     * @return Returns this literal in an 
     * ArrayList
     */
    @Override
    public Set<LogicLiteral> getLiterals() {
    	return new TreeSet<LogicLiteral>( Arrays.asList(this) );
    }
    
    /**
     * @return Returns this literal in an 
     * ArrayList if this literal should be positive,
     * else it returns an empty list.
     */
    @Override
    public Set<LogicLiteral> getPositiveLiterals() {
    	if(!negated) { new TreeSet<LogicLiteral>( Arrays.asList(this) ); }
    	return new TreeSet<LogicLiteral>(/*Empty set*/); 
    }
    
    /**
     * @return Returns this literal in an 
     * ArrayList if this literal should be negative,
     * else it returns an empty list.
     */
    @Override
    public Set<LogicLiteral> getNegativeLiterals() {
    	if(negated) { return new TreeSet<LogicLiteral>( Arrays.asList(this) ); }
    	return  new TreeSet<LogicLiteral>(/*Empty set*/); 
    }
    
    /**
     * @return Returns an empty ArrayList
     */
    @Override
    public List<Clause> getClauses() {
    	return new ArrayList<Clause>(/*Empty list*/);
    }
    
    /**
     * @return Returns this literal as 
     * precedent of this logic rule
     */
    @Override
    public LogicRule getPrecedent() {
    	return this;
    }
    
    /**
     * @return Returns this literal as 
     * antecedent of this logic rule
     */
    @Override
    public LogicRule getAntecedent() {
    	return this;
    }
    
    /**
     * @return Returns this literal as 
     * LogicRule in Normal Form
     */
    @Override
    public LogicRule getNfRule() {
    	return this.inNf;
    }
    
    /**
     * @return Returns this literal as 
     * LogicRule in Conjunctive Normal Form
     */
    @Override
    public LogicRule getCnfRule() {
    	return this.inCnf;
    }
    
    /**
     * @return Returns this literal as 
     * LogicRule in Deterministic 
     * Decomposable Negation Normal Form
     */
    @Override
    public LogicRule getDdnnfRule() {
    	return this.inDdnnf;
    }
    
    /**
     * @return Returns this literal's
     * d-DNNF graph
     */
    @Override
    public DdnnfGraph getDdnnfGraph() {
    	return this.ddnnfGraph;
    }
    

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        if (!getClass().equals(obj.getClass())) {
            return false;
        }

        return this.name == ((LogicLiteral) obj).getName();
    }

}
