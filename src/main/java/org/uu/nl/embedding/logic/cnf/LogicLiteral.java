package org.uu.nl.embedding.logic.cnf;

import org.apache.log4j.Logger;
import org.uu.nl.embedding.lensr.DdnnfGraph;
import org.uu.nl.embedding.logic.LogicRule;
import org.uu.nl.embedding.logic.ddnnf.DdnnfLogicRule;

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

    private final static Logger logger = Logger.getLogger(LogicLiteral.class);
	
	private boolean assignment;
	private boolean negated;
	
	private String name = null;
	private String cnfName;
	private String ddnnfName;

	private DdnnfGraph ddnnfGraph;
	private DdnnfLogicRule ddnnfRule;
	
	
	/**
	 * Constructor method for the literal class.
	 * @param name The name of this literal, e.g. "p", 
	 * or "isCar".
	 * @param value The starting boolean truth value of 
	 * this literal.
	 * @param negated The boolean value stating if this
	 * literal should, or shouldn't be negated.
	 */
	public LogicLiteral(final String name, boolean value, boolean isNegated) {
		super();
		this.name = name;
		this.assignment = value;
		this.negated = isNegated;
		
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
        return this.assignment;
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

	@Override
	public DdnnfGraph getDdnnfGraph() {
		if(this.ddnnfRule == null) { logger.warn("No d-DNNF graph to return."); }
		return this.ddnnfGraph;
	}

	@Override
	public DdnnfLogicRule getDdnnfRule() {
		if(this.ddnnfRule == null) { logger.warn("No d-DNNF rule to return."); }
		return this.ddnnfRule;
	}

	@Override
	public void setDdnnfGraph(DdnnfGraph graph) {
		this.ddnnfGraph = graph;
		
	}

	@Override
	public void setDdnnfRule(DdnnfLogicRule rule) {
		this.ddnnfRule = rule;
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
	public boolean isSatisfied() {
		return this.assignment != this.negated;
	}
    
    /*
    /**
     * @return Returns this literal as 
     * LogicRule in Normal Form
    @Override
    public NormalLogicRule getNfRule() {
    	return this.inNf;
    }
    */
    
    /*
    /**
     * @return Returns this literal as 
     * LogicRule in Conjunctive Normal Form
    @Override
    public CnfLogicRule getCnfRule() {
    	return this.inCnf;
    }
     */
    
    /*
    /**
     * @return Returns this literal as 
     * LogicRule in Deterministic 
     * Decomposable Negation Normal Form
    @Override
    public LogicRule getDdnnfRule() {
    	return this.inDdnnf;
    }
    */
    

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

}
