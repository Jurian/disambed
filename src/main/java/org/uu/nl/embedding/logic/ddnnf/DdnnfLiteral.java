package org.uu.nl.embedding.logic.ddnnf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.uu.nl.embedding.lensr.DdnnfGraph;
import org.uu.nl.embedding.logic.LogicRule;
import org.uu.nl.embedding.logic.cnf.CnfLogicRule;
import org.uu.nl.embedding.logic.cnf.LogicLiteral;

public class DdnnfLiteral implements DdnnfLogicRule {

	private boolean assignment;
	private boolean negated;

	private CnfLogicRule sourceRule;
	
	private String name = null;
	private String cnfName;
	private String ddnnfName;
	
	
	/**
	 * Constructor method for the literal class.
	 * @param name The name of this literal, e.g. "p", 
	 * or "isCar".
	 * @param value The starting boolean truth value of 
	 * this literal.
	 * @param negated The boolean value stating if this
	 * literal should, or shouldn't be negated.
	 */
	public DdnnfLiteral(final String name, boolean value) {
		super();
		this.name = name;
		this.assignment = value;
		//this.negated = isNegated;
		//this.sourceRule = sourceRule;
		
		this.cnfName = name;
		this.ddnnfName = name;
		
		//generateGraph();
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
	/*
	public DdnnfLiteral(final String name, final boolean value, final CnfLogicRule sourceRule) {
		this(name, value, !value, sourceRule);
	}
	*/
	
	public CnfLogicRule getSourceRule() {
		return this.sourceRule;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isNegated() {
		return this.negated;
	}
	
	/**
	 * 
	 * @param newVal
	 */
	public void setNegated(final boolean newVal) {
		this.negated = newVal;
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
        return !this.assignment;
    }

    /**
     * @return Returns true if the assignment
     * has value true, else it returns false
     */
    @Override
    public boolean isTrue() {
        return this.assignment;
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
		String negatedStr = "";
		if(negated) { negatedStr = "NOT "; }
		
    	return negatedStr + String.valueOf(this.assignment);
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
     * ArrayList if this literal should be positive,
     * else it returns an empty list.
     */
    @Override
    public Set<DdnnfLiteral> getPositiveLiterals() {
    	if(!negated) { new TreeSet<DdnnfLiteral>( Arrays.asList(this) ); }
    	return new TreeSet<DdnnfLiteral>(/*Empty set*/); 
    }
    
    /**
     * @return Returns this literal in an 
     * ArrayList if this literal should be negative,
     * else it returns an empty list.
     */
    @Override
    public Set<DdnnfLiteral> getNegativeLiterals() {
    	if(negated) { return new TreeSet<DdnnfLiteral>( Arrays.asList(this) ); }
    	return  new TreeSet<DdnnfLiteral>(/*Empty set*/); 
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

        return this.getName() == ((DdnnfLiteral) obj).getName();
    }

    @Override 
    public int hashCode() {
		int hash = 7;
		for (int i = 0; i < this.name.length(); i++) {
			hash = hash*31 + this.name.charAt(i);
		}
		return hash;
    }

    @Override 
    public int compareTo(LogicRule other) {

      if (this.hashCode() < other.hashCode()) {
        return -1;
      }
      return this.hashCode() == other.hashCode() ? 0 : 1;
    }

    
    /**
     * @return Returns this literal in an 
     * ArrayList
     */
	@Override
	public ArrayList<DdnnfLiteral> getLiterals() {
		return new ArrayList<DdnnfLiteral>( Arrays.asList(this) );
	}

	@Override
	public List<DdnnfLogicRule> getRules() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void negate() {
		this.negated = !this.negated;
		
	}

}
