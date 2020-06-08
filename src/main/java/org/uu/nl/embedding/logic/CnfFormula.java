package org.uu.nl.embedding.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.uu.nl.embedding.lensr.DdnnfGraph;

/**
 * Interface class for logic CNF formulae 
 * to be used in CNF.
 * 
 * DISCLAIMER: This class is partly based 
 * on the code by Rodion "rodde" Efremov.
 * 
 * @author Euan Westenbroek
 * @version 1.0
 * @since 07-06-2020
 */
public class CnfFormula implements CnfLogicRule {

	private boolean assignment;

	private LogicRule inNf;
	private LogicRule inCnf;
	private LogicRule inDdnnf;
	
	private String name = null;
	private String cnfName;
	private String ddnnfName;
	
	private DdnnfGraph ddnnfGraph;
	
	/**
	 * The list of clauses that belong to this formula.
	 */
	private final List<Clause> clauseList = new ArrayList<>();
	
    /**
     * The set of positive literals present in this clause.
     */
    private final Set<LogicLiteral> positiveLiteralSet = 
            new TreeSet<>();
    /**
     * The set of negative literals present in this clause.
     */
    private final Set<LogicLiteral> negativeLiteralSet = 
            new TreeSet<>();

    
    public CnfFormula(Clause[] clauses) {
    	
		this.name = clause.getName();
		this.assignment = clause.getAssignment();
		this.cnfName = clause.getCnfName();
		this.ddnnfName = clause.getDdnnfName();
	}
    
	public CnfFormula(Clause clause) {
		this.name = clause.getName();
		this.assignment = clause.getAssignment();
		this.cnfName = clause.getCnfName();
		this.ddnnfName = clause.getDdnnfName();
	}
	
	public void addClause(Clause clause) {
		Set<LogicLiteral> literals = clause.getLiterals();
		for(LogicLiteral literal : literals) {
			addLiteral(literal);
		}
		this.clauseList.add(clause);
	}
	
	public void removeClause(String clauseName) {
		this.positiveLiteralSet.clear();
		this.negativeLiteralSet.clear();
		
		for(Clause clause : clauseList) {
			if(clause.getName() == clauseName) {
				clauseList.remove(clause);
			}
			else {
				for(LogicLiteral literal : clause.getLiterals()) {
					addLiteral(literal);
				}
			}
		}
		
	}
	
	/**
	 * This method checks if the LogicLiteral is negated
	 * or not, and adds it to the corresponding set.
	 * 
	 * @param literal The LogicLiteral to check.
	 */
    private void addLiteral(LogicLiteral literal) {
		if(literal.isNegated()) { addNegativeLiteral(literal); }
		else { addPositiveLiteral(literal); }
    }

    /**
     * Adds a non-negated literal to this clause.
     * 
     * @param LogicLiteral the literal to add.
     */
    private void addPositiveLiteral(LogicLiteral literal) {
    	if(!literal.isNegated()) { positiveLiteralSet.add(literal); }
    	else { throw new IllegalArgumentException("Cannot add negative literals to positiveLiteralSet."); }
    }

    /**
     * Adds a negated literal to this clause.
     * 
     * @param LogicLiteral the literal to add.
     */
    private void addNegativeLiteral(LogicLiteral literal) {
    	if(literal.isNegated()) { negativeLiteralSet.add(literal); }
    	else { throw new IllegalArgumentException("Cannot add positive literals to negativeLiteralSet."); }
    }

    /**
     * Checks whether the input assignment satisfies this clause.
     * 
     * @return {@code true} if the assignment satisfies this clause, and 
     *         {@code false} otherwise.
     */
    public boolean isSatisfied() {
        for (LogicLiteral positiveLogicLiteral : positiveLiteralSet) {
            if (positiveLogicLiteral.isTrue()) {
                return true;
            }
        }

        for (LogicLiteral negativeLogicLiteral : negativeLiteralSet) {
            if (negativeLogicLiteral.isFalse()) {
                return true;
            }
        }

        return false;
    }
    
	
	/*
	 * Below are all interface methods implemented
	 */

    /**
     * Converts this formula to one of its possible
     * versions that results in the provided 
     * assignment, by changing the assignment of one
     * or several of its literals.
     */
    @Override
    public void setAssignment(boolean assignment) {
        this.assignment = assignment;
    }
    
    /**
     * @return Returns the current assignment of
     * this formula
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
        this.assignment = ;
    }
    
    /**
     * Sets the assignment of this logic
     * rule to true
     */
    @Override
    public void setTrue() {
        this.assignment = ;
    }

    /**
     * @return Returns true if the assignment
     * has value false, else it returns false.
     */
    @Override
    public boolean isFalse() {
        return !assignment;
    }

    /**
     * @return Returns true if the assignment
     * has value true, else it returns false.
     */
    @Override
    public boolean isTrue() {
        return assignment;
    }
    
    /**
     * @return Returns the name of this formula
     * (same as toString()).
     */
    @Override
    public String getName() {
    	return this.name;
    }
    
    /**
     * @return Returns this clause as a
     * string, using the names of the 
     * literals.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Set<LogicLiteral> allLiterals = getLiterals();

        sb.append("(");
        String separator = "";

        for (LogicLiteral literal : allLiterals) {
            sb.append(separator);
            separator = " OR ";

            if (negativeLiteralSet.contains(literal)) {
                sb.append("NOT ");
            }
            sb.append(literal.getName());
        }
        return sb.append(")").toString();
    }

    /**
     * @return Returns this clause as a
     * string, using the values of the 
     * literals.
     */
    @Override
    public String toValueString() {
        StringBuilder sb = new StringBuilder();
        Set<LogicLiteral> allLiterals = getLiterals();

        sb.append("(");
        String separator = "";

        for (LogicLiteral literal : allLiterals) {
            sb.append(separator);
            separator = " OR ";

            if (negativeLiteralSet.contains(literal)) {
                sb.append("NOT ");
            }
            sb.append(literal.getAssignment());
        }
        return sb.append(")").toString();
    }
    
    /**
     * @return Returns the name in CNF of this
     * formula
     */
    @Override
    public String getCnfName() {
    	return this.cnfName;
    }
    
    /**
     * @return Returns the name in d-DNNF of this
     * formula
     */
    @Override
    public String getDdnnfName() {
    	return this.ddnnfName;
    }
    
    /**
     * @return Returns all literals of this
     * formula in an ArrayList.
     */
    @Override
    public Set<LogicLiteral> getLiterals() {
        Set<LogicLiteral> LogicLiteralSet = new TreeSet<>(positiveLiteralSet);
        LogicLiteralSet.addAll(negativeLiteralSet);
        
        return LogicLiteralSet;
    }
    
    /**
     * @return Returns an list with all
     * clauses of this formula.
     */
    @Override
    public List<Clause> getClauses() {
    	return this.clauseList;
    }
    
    /**
     * @return Returns this formula as 
     * precedent of this logic rule
     */
    @Override
    public LogicRule getPrecedent() {
    	return this;
    }
    
    /**
     * @return Returns this formula as 
     * antecedent of this logic rule
     */
    @Override
    public LogicRule getAntecedent() {
    	return this;
    }
    
    /**
     * @return Returns this formula as 
     * LogicRule in Normal Form
     */
    @Override
    public LogicRule getNfRule() {
    	return this.inNf;
    }
    
    /**
     * @return Returns this formula as 
     * LogicRule in Conjunctive Normal Form
     */
    @Override
    public LogicRule getCnfRule() {
    	return this;
    }
    
    /**
     * @return Returns this formula as 
     * LogicRule in Deterministic 
     * Decomposable Negation Normal Form
     */
    @Override
    public LogicRule getDdnnfRule() {
    	return this.inDdnnf;
    }
    
    /**
     * @return Returns this formula's
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

