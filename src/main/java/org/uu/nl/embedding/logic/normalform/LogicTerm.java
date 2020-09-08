/**
 * 
 */
package org.uu.nl.embedding.logic.normalform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.ArrayUtils;
import org.uu.nl.embedding.lensr.DdnnfGraph;
import org.uu.nl.embedding.logic.LogicRule;
import org.uu.nl.embedding.logic.cnf.CnfLogicRule;
import org.uu.nl.embedding.logic.cnf.LogicLiteral;

/**
 * Class for simple logic terms.
 * The simple logic term is the most basic term class
 * Example of this are: hasName(Jan) (Boolean)
 * 
 * @author Euan Westenbroek
 * @version 1.0
 * @since 12-05-2020
 */
public class LogicTerm {

	protected boolean assignment;
	
	private NormalLogicRule inNf;
	private CnfLogicRule inCnf;
	private LogicRule inDdnnf;
	
	private String name = null;
	private String cnfName;
	private String ddnnfName;
	/*
	/**
	 * Constructor method with user-given name declaration.
	 * 
	 * @param value A Boolean value representing the logic term
	 * @param name The name of the logic term
	 *
	public LogicTerm(boolean value, String name) {
		super();
		this.assignment = value;
		this.name = name;
		this.cnfName = name;
		this.ddnnfName = name;
		
		generateCnf();
		generateDdnnf();
	}
	
	private void generateCnf() {
		this.inCnf = new LogicLiteral(this.name, this.assignment);
	}
	
	private void generateDdnnf() {
		// MOET NOG VERANDERD WORDEN!!!
		this.inDdnnf = this;
	}
	
	
	/*
	 * All interface methods implemented
	 *
	
	/**
	 * @return Returns the Boolean value of the logic term
	 *
	@Override
	public boolean getAssignment() {
		return this.assignment;
	}

    /**
     * Sets the a new boolean assignment of
     * this logic rule.
     *
    @Override
    public void setAssignment(boolean assignment) {
        this.assignment = assignment;
    }
    
    /**
     * Sets the assignment of this logic
     * rule to false
     *
	@Override
    public void setFalse() {
        this.assignment = false;
    }
    
    /**
     * Sets the assignment of this logic
     * rule to true
     *
    @Override
    public void setTrue() {
        this.assignment = true;
    }

    /**
     * @return Returns true if the assignment
     * has value false, else it returns false
     *
    @Override
    public boolean isFalse() {
        return !this.assignment;
    }

    /**
     * @return Returns true if the assignment
     * has value true, else it returns false
     *
    @Override
    public boolean isTrue() {
        return this.assignment;
    }
	
	/**
	 * @return Returns the name of the logic term (given or generated)
	 *
	@Override
	public String getName() {
		return this.name;
	}
    
    /**
     * @return Returns the name in CNF of this
     * literal
     *
    @Override
    public String getCnfName() {
    	return this.cnfName;
    }
    
    /**
     * @return Returns the name in d-DNNF of this
     * literal
     *
    @Override
    public String getDdnnfName() {
    	return this.ddnnfName;
    }

    /**
     * @return Returns this term's value
     * as a string
     *
	@Override
	public String toString() {
		return this.name;
	}
    
    /**
     * @return Returns this literal's value
     * as a string
     *
	@Override
	public String toValueString() {
    	return String.valueOf(this.assignment);
	}
	
	/**
	 * @return Returns an array with the logic term itself; 
	 * 		In this case it return "[this]" (i.e. self)
	 *
	@Override
	public LogicRule[] getAllTerms() {
		LogicRule[] allTerms = new LogicRule[] {this};
		return allTerms;
	}
	
	/**
	 * @return Returns this LogicRule as Precedent
	 *
	@Override
	public LogicRule getPrecedent() {
		return this;
	}
	
	/**
	 * Placeholder for abstract method
	 * This method shouldn't be used
	 *
	@Override
	public LogicRule getAntecedent() {
		return this;
	}
    
    /**
     * @return Returns this term as 
     * LogicRule in Normal Form
     *
    @Override
    public NormalLogicRule getNfRule() {
    	return this.inNf;
    }

	/**
	 * Returns this LogicRule in its CNF
	 *
	@Override
	public CnfLogicRule getCnfRule() {
		return this.inCnf;
	}

	/**
	 * Returns this LogicRule in its d-DNNF
	 *
	@Override
	public LogicRule getDdnnfRule() {
		return this.inDdnnf;
	}

	/**
	 * Returns the logic graph of the d-DNNF
	 *
	@Override
	public DdnnfGraph getDdnnfGraph() {
		// DIT GAAT NIET GOED
		// NOG AANPASSEN!!!
		return this.inDdnnf.getDdnnfGraph();
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

        return this.name == ((NormalLogicRule) obj).getName();
    }
    */

}
