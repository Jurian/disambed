/**
 * 
 */
package org.uu.nl.embedding.logic.normalform;

import org.apache.commons.lang3.ArrayUtils;
import org.uu.nl.embedding.lensr.DdnnfGraph;
import org.uu.nl.embedding.logic.LogicRule;
import org.uu.nl.embedding.logic.cnf.CnfLogicRule;
import org.uu.nl.embedding.logic.cnf.LogicLiteral;

/**
 * Class for negation logic terms.
 * The negation logic term is the opposite Boolean value 
 * 		of the logic term following the negation
 * 
 * @author Euan Westenbroek
 * @version 1.0
 * @since 12-05-2020
 */
public class Negation {
	
	protected NormalLogicRule firstTerm;
	protected boolean assignment;
	
	private String name = null;
	private String cnfName;
	private String ddnnfName;
	
	private NormalLogicRule inNf;
	private CnfLogicRule inCnf;
	private LogicRule inDdnnf;
	
	/*
	/**
	 * Constructor method.
	 * 
	 * @param term A LogicTerm class representing the negated logic term
	 *
	protected Negation(NormalLogicRule term) {
		super();
		this.firstTerm = term;
		this.assignment = !this.firstTerm.getAssignment();
		this.name = ("NOT " + this.firstTerm.getName());
		
		this.inNf = this;
		generateCnf();
		generateDdnnf();
	}
	
	/**
	 * CNF generator for the constructor.
	 *
	private void generateCnf() {
		this.inCnf = new LogicLiteral(this.name, this.firstTerm.getAssignment(), true);
	}
	
	/**
	 * d-DNNF generator for the constructor.
	 *
	private void generateDdnnf() {
		// MOET NOG VERANDERD WORDEN!!!
		this.inDdnnf = this;
	}
	
	/**
	 * This method generates the d-DNNF graph
	 * of this LogicRule
	 *
	private void generateDdnnfGraph() {
		DdnnfGraph leftGraph = this.firstTerm.getDdnnfGraph();
		
		//ddnnfGraph = new DdnnfGraph(this, leftGraph);
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
        if(assignment) { setTrue(); }
        else { setFalse(); }
    }
    
    /**
     * Sets the assignment of this logic
     * rule to false
     *
	@Override
    public void setFalse() {
    	this.firstTerm.setTrue();
        this.assignment = false;
    }
    
    /**
     * Sets the assignment of this logic
     * rule to true
     *
    @Override
    public void setTrue() {
    	this.firstTerm.setFalse();
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
	 * @return Returns the string of the logic term in CNF
	 *
	@Override
	public String getCnfName() {
		return this.cnfName;
	}

	/**
	 * @return Returns the string of the logic term in d-DNNF
	 *
	@Override
	public String getDdnnfName() {
		return this.ddnnfName;
	}

    /**
     * @return Returns this negation's value
     * as a string
     *
	public String toString() {
		return this.name;
	}
    
    /**
     * @return Returns this negation's value
     * as a string
     *
	public String toValueString() {
    	return "NOT" + String.valueOf(this.firstTerm.getAssignment());
	}
	
	/**
	 * @return Returns an array of all the basic logic terms themselves,
	 * 		without any logical operator; 
	 * 			In this case it returns all the basic 
	 * 			logic terms this.firstTerm is comprised of
	 *
	@Override
	public LogicRule[] getAllTerms() {
		return this.firstTerm.getAllTerms();
	}
	
	/**
	 * @return Returns this LogicRule as Precedent.
	 *
	@Override
	public NormalLogicRule getPrecedent() {
		return this;
	}
	
	/**
	 * Placeholder for abstract method
	 * This method shouldn't be used.
	 *
	@Override
	public NormalLogicRule getAntecedent() {
		return this;
	}
    
    /**
     * @return Returns this LogicRule 
     * in Normal Form.
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
