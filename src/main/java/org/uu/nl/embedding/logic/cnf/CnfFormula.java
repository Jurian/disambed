package org.uu.nl.embedding.logic.cnf;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.uu.nl.embedding.lensr.DdnnfGraph;
import org.uu.nl.embedding.logic.LogicRule;

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

	
	private String name = null;
	private String cnfName;
	private String ddnnfName;
	
	/**
	 * The list of clauses that belong to this formula.
	 */
	private final ArrayList<Clause> clauseList = new ArrayList<>();
	
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

    
    
    /**
     * Constructor method for this class.
     * @param cnfFormula The CnfFormula that make up this formula.
	 * @param nfRule The Normal Form formula this CNF formula
	 * is generated from.
    public CnfFormula(CnfFormula cnfFormula, NormalLogicRule nfRule) {
    	super();
    	if(cnfFormula instanceof CnfFormula) {
    		ArrayList<Clause> clauses = cnfFormula.getClauses();
        	for(Clause clause : clauses) { addClause(clause); }
        	
    		this.name = this.toString();
    		this.cnfName = this.name;
    		
    		/*
    		if(nfRule == null) { nfRule = generateNf(); }
    		this.inNf = nfRule;
    		this.inCnf = this;
    		this.inDdnnf = nfRule.getDdnnfRule(); // Checken of dit goed gaat qua compile volgorde!!!!!!
    		this.ddnnfName = nfRule.getDdnnfName();
    		
    		this.assignment = isSatisfied();
    	}
    }
    */
    
    /**
     * Constructor method for this class.
     * @param clauses The clauses that make up this formula.
	 * @param nfRule The Normal Form formula this CNF formula
	 * is generated from.
	 */
    public CnfFormula(Clause[] clauses) {
		super();
    	for(Clause clause : clauses) { addClause(clause); }
    	
		this.name = this.toString();
		this.cnfName = this.name;
		
		/*
		if(nfRule == null) { nfRule = generateNf(); }
		this.inNf = nfRule;
		this.inCnf = this;
		this.inDdnnf = nfRule.getDdnnfRule(); // Checken of dit goed gaat qua compile volgorde!!!!!!
		this.ddnnfName = nfRule.getDdnnfName();
		*/
		this.assignment = isSatisfied();
	}

    /**
     * Constructor method for this class.
     * @param clauses The clause that makes up this formula.
	 * @param nfRule The Normal Form formula this CNF formula
	 * is generated from.
	 */
	public CnfFormula(Clause clause) {
		super();
		addClause(clause);
		
		this.name = this.toString();
		this.cnfName = this.name;
		
		/*
		if(nfRule == null) { nfRule = generateNf(); }
		this.inNf = nfRule;
		this.inCnf = this;
		this.inDdnnf = nfRule.getDdnnfRule(); // Checken of dit goed gaat qua compile volgorde!!!!!!
		this.ddnnfName = nfRule.getDdnnfName();
		*/
		this.assignment = isSatisfied();
	}
	
	/**
	 * Method to add a clause to the formula.
	 * @param clause The clause to be added.
	 */
	public void addClause(Clause clause) {
		this.clauseList.add(clause);
		
		this.positiveLiteralSet.addAll(
				clause.getPositiveLiterals());
		this.negativeLiteralSet.addAll(
				clause.getNegativeLiterals());
		
		this.assignment = isSatisfied();
	}

	/**
	 * Method to remove a clause from the formula.
	 * @param clause The clause to be removed.
	 */
	public void removeClause(String clauseName) {
		this.positiveLiteralSet.clear();
		this.negativeLiteralSet.clear();
		
		for(Clause clause : clauseList) {
			if(clause.getName() == clauseName) {
				clauseList.remove(clause);
			}
			else {
				
				this.positiveLiteralSet.addAll(
						clause.getPositiveLiterals());
				this.negativeLiteralSet.addAll(
						clause.getNegativeLiterals());
			}
		}
		this.assignment = isSatisfied();
		
	}

    /**
     * Adds a non-negated literal to the given clause.
     * 
     * @param LogicLiteral the literal to add.
     * @param targetClause The clause to which to add.
     */
    public void addPositiveLiteral(LogicLiteral literal, Clause targetClause) {
    	targetClause.addPositiveLiteral(literal);
 		this.assignment = isSatisfied();
    }

    /**
     * Adds a negated literal to the given clause.
     * 
     * @param LogicLiteral The literal to add.
     * @param targetClause The clause to which to add.
     */
    public void addNegativeLiteral(LogicLiteral literal, Clause targetClause) {
    	targetClause.addNegativeLiteral(literal);
		this.assignment = isSatisfied();
    }

    /**
     * Checks whether the input assignment satisfies this formula.
     * 
     * @return {@code true} if the assignment satisfies this clause, and 
     *         {@code false} otherwise.
     */
    public boolean isSatisfied() {
    	for(Clause clause : clauseList) {
    		if(!clause.isSatisfied()) { return false; }
    	}
    	return true;
    }
    
    /**
     * Method to merge two CNF formulae to one.
     * @param formula The other formula to merge this one with
     * @return Returns the resulting formula from this formula and
     * the given formula.
     */
    public CnfFormula mergeWith(CnfFormula formula) {
    	List<Clause> uniqueClauses = this.clauseList;
    	boolean samePos, sameNeg;
    	
    	// Loop through local (this formula) and 
    	// incoming clauses (other formula)
    	for(Clause localClause : getClauses()) {
    		for(Clause incClause : formula.getClauses()) {
    			samePos = localClause.getPositiveLiterals().equals(
    					incClause.getPositiveLiterals());
    			sameNeg = localClause.getNegativeLiterals().equals(
    					incClause.getNegativeLiterals());
    			
    			if(!(samePos && sameNeg)) {
    				uniqueClauses.add(incClause);
    			}
    			// else No need for duplicate clauses.
    		}
    	}
    	Clause[] resClauses = uniqueClauses.toArray(new Clause[0]);
    	CnfFormula resForm = new CnfFormula(resClauses);
    	//resForm.cleanClauses();
    	
    	return resForm;
    }

    /**
     * Method for satisfying this clause.
     */
    private void satisfy() {
    	
    	// If formula is already satisfied, re-satisfy the formula
    	// by satisfying each clause with another literal.
    	if(isSatisfied()) {
    		
			// Loop through clauses and re-satisfy each clause.
            for(Clause clause : clauseList) {
            	clause.setTrue();
            }
    	}
    	else {
    		// Loop through clauses and satisfy the unsatisfied
    		// clauses.
            for(Clause clause : clauseList) {
            	if(clause.isFalse()) { clause.setTrue(); }
            }
    	}
		// Re-assign clause.
		this.assignment = isSatisfied();
    }
    
    /**
     * Method for unsatisfying this clause.
     */
    private void unsatisfy() {
    	if(isSatisfied()) {
    		// Loop through clauses and unsatisfy the first
    		// clause.
            for(Clause clause : clauseList) {
            	clause.setFalse();
            	break;
            }
    	}
    	else {
        	boolean isFirst = true;
        	boolean clauseFound = false;
        	Clause runClause = new Clause("tempClause", false, false);
    		Clause firstClause = new Clause("tempClause2", false, false);
    		Clause lastSatClause = new Clause("tempClause3", false, false);
			// Loop through clauses and re-unsatisfy each clause.
            for(Clause clause : clauseList) {
            	runClause = clause;
            	if(isFirst) {
            		firstClause = clause; 
            		isFirst = false;
            	}
            	// If this clause is true and previous clause
            	// was unsatisfied, then falsify and break loop
            	if(clause.isTrue() && clauseFound) {
            		clause.setFalse();
            		break;
            	}
            	if(clause.isFalse() && !clauseFound) { 
            		clause.setTrue();
            		lastSatClause = clause;
            		clauseFound = true;
            	}
            }
            if(lastSatClause == runClause) { firstClause.setFalse(); } // Gaat dit goed???
    	}
		// Re-assign clause.
		this.assignment = isSatisfied();
    }
    
    /**
     * Private method to generate a "normal form" formula if
     * this clause was not based on a normal form formula
     * already.
    private NormalLogicRule generateNf() {
    	Disjunction conj = null;
    	Clause firstClause = null, secondClause = null;
    	
        for(Clause clause : clauseList) {
        	if(firstClause == null) { firstClause = clause; }
        	if(secondClause == null) { secondClause = clause; }
        	if(!(firstClause == null || secondClause == null)) {
        		if(conj==null) {
        			conj = new Disjunction(firstClause, secondClause);
        		}
        		else {
        			conj = new Disjunction(conj, clause);
        		}
        	}
        }
        return conj;
    }
    */
    
    
    private void generateDdnnf() {
    	//TODO
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
    	if(assignment) { satisfy(); }
    	else { unsatisfy(); }
    }
    
    /**
     * @return Returns the current assignment of
     * this formula
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
        unsatisfy();
    }
    
    /**
     * Sets the assignment of this logic
     * rule to true
     */
    @Override
    public void setTrue() {
        satisfy();
    }

    /**
     * @return Returns true if the assignment
     * has value false, else it returns false.
     */
    @Override
    public boolean isFalse() {
        return !this.assignment;
    }

    /**
     * @return Returns true if the assignment
     * has value true, else it returns false.
     */
    @Override
    public boolean isTrue() {
        return this.assignment;
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
     * @return Returns this formula as a
     * string, using the names of the 
     * literals.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        List<Clause> allClauses = getClauses();

        sb.append("(");
        String separator = "";

        for(Clause clause : allClauses) {
            sb.append(separator);
            sb.append(clause.getCnfName());
            separator = " AND ";
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
        List<Clause> allClauses = getClauses();

        sb.append("(");
        String separator = "";

        for(Clause clause : allClauses) {
            sb.append(separator);
            sb.append(clause.toValueString());
            separator = " AND ";
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
     * @return Returns all positive literals of this 
     * clause in an ArrayList.
     */
    @Override
    public Set<LogicLiteral> getPositiveLiterals() {
        return this.positiveLiteralSet;
    }
    
    /**
     * @return Returns all negative literals of this 
     * clause in an ArrayList.
     */
    @Override
    public Set<LogicLiteral> getNegativeLiterals() {
        return this.negativeLiteralSet;
    }
    
    /**
     * @return Returns an list with all
     * clauses of this formula.
     */
    @Override
    public ArrayList<Clause> getClauses() {
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

        return this.getName() == ((CnfFormula) obj).getName();
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

