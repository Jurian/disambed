package org.uu.nl.embedding.logic;

import org.uu.nl.embedding.lensr.DdnnfGraph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Interface class for logic CNF clauses 
 * to be used in CNF.
 * 
 * DISCLAIMER: This class is partly based 
 * on the code by Rodion "rodde" Efremov.
 * 
 * @author Euan Westenbroek
 * @version 1.0
 * @since 07-06-2020
 */
public class Clause implements CnfLogicRule {

	private boolean assignment;

	private LogicRule inNf;
	private LogicRule inCnf;
	private LogicRule inDdnnf;
	
	private String name = null;
	private String cnfName;
	private String ddnnfName;
	
	private DdnnfGraph ddnnfGraph;
	
    /**
     * The set of positive literals present in this clause.
     */
    private final Set<LogicLiteral> positiveLiteralSet = new TreeSet<>();
    private final Set<String> positiveNameSet = new TreeSet<>();
    /**
     * The set of negative literals present in this clause.
     */
    private final Set<LogicLiteral> negativeLiteralSet = new TreeSet<>();
    private final Set<String> negativeNameSet = new TreeSet<>();
    

  
	public Clause(final LogicLiteral[] orderedLiterals, final boolean[] orderedValues) {
		if(!(orderedLiterals.length == orderedValues.length)) {
			throw new IllegalArgumentException("The two arrays do not have the same length."); }
		
		for(int i = 0; i < orderedLiterals.length; i++) {
			if(orderedValues[i]) { addPositiveLiteral(orderedLiterals[i]); }
			else { addNegativeLiteral(orderedLiterals[i]); }
		}
		
		this.cnfName = this.toString();
		
	}
	
    /**
	 * Constructor method for the clause class
	 * @param name The name of the literal in this clause
	 * , e.g. "p", or "isCar"
	 * @param value The starting boolean truth value of 
	 * the literal in this clause
	 */
	public Clause(final LogicLiteral literal, boolean value) {
		if(value) { addPositiveLiteral(literal); }
		else { addNegativeLiteral(literal); }
		
		this.name = this.toString();
		this.assignment = literal.getAssignment();
		this.cnfName = literal.getCnfName();
		this.ddnnfName = literal.getDdnnfName();
	}
	
	/**
	 * Constructor method for the clause class
	 * @param name The name of the literal in this clause
	 * , e.g. "p", or "isCar"
	 * @param value The starting boolean truth value of 
	 * the literal in this clause
	 */
	public Clause(final String literalName, boolean value) {
		LogicLiteral literal = new LogicLiteral(literalName, value);
		if(value) { addPositiveLiteral(literal); }
		else { addNegativeLiteral(literal); }
		
		this.name = this.toString();
		this.assignment = literal.getAssignment();
		this.cnfName = literal.getCnfName();
		this.ddnnfName = literal.getDdnnfName();
	}

    /**
     * Adds a non-negated literal to this clause.
     * 
     * @param LogicLiteral the literal to add.
     */
    public void addPositiveLiteral(LogicLiteral literal) {
    	 positiveLiteralSet.add(literal);
    	 positiveNameSet.add(literal.getName());
    }

    /**
     * Adds a negated literal to this clause.
     * 
     * @param LogicLiteral the literal to add.
     */
    public void addNegativeLiteral(LogicLiteral literal) {
    	negativeLiteralSet.add(literal);
    	negativeNameSet.add(literal.getName());
    }

    /**
     * Removes any given literal from this clause,
     * if the literal is part of the clause.
     * 
     * @param literal The literal to remove.
     */
	public void removeLiteral(LogicLiteral literal) {
		if(positiveLiteralSet.contains(literal)) { positiveLiteralSet.remove(literal);  }
		else if(negativeLiteralSet.contains(literal)) { negativeLiteralSet.remove(literal); }
		else { throw new IllegalArgumentException("Cannot remove given literal from either *LiteralSet."); }
	}
	
    /**
     * Removes any given literal from this clause,
     * based on its name. Only if the literal is
     * part of the clause.
     * 
     * @param literalName The literal to remove.
     */
	public void removeLiteral(String literalName) {
		boolean removed = false;
		for(LogicLiteral literal : positiveLiteralSet) {
			if(literal.getName() == literalName) {
				this.positiveLiteralSet.remove(literal);
				removed = true;
				break;
			}
		}
		if(!removed) {
			for(LogicLiteral literal : negativeLiteralSet) {
				if(literal.getName() == literalName) {
					this.negativeLiteralSet.remove(literal);
					break;
				}
			}
		}
	}
	
	/**
	 * Sets a new assignment of the literal with the given name.
	 * If that literal is not part of the clause, the literal is
	 * added to the clause.
	 * 
	 * @param literalName The name of the literal to be changed.
	 * @param newValue The truth value of given literal.
	 */
	public void setLiteralAssignment(String literalName, boolean newValue) {
		if(newValue) {
			if(negativeNameSet.contains(literalName)) { 
				for(LogicLiteral lit : negativeLiteralSet) {
					if(lit.getName() == literalName) {
						negativeLiteralSet.remove(lit);
						positiveLiteralSet.add(lit);
					}
				} 
			} else {
				// If not already part of the clause, then:
				addPositiveLiteral(new LogicLiteral(literalName, newValue));
			}
			
		}
		else if(!newValue) {
			if(positiveNameSet.contains(literalName)) { 
				for(LogicLiteral lit : positiveLiteralSet) {
					if(lit.getName() == literalName) {
						positiveLiteralSet.remove(lit);
						negativeLiteralSet.add(lit);
					}
				}
			} else {
				// If not already part of the clause, then:
				addNegativeLiteral(new LogicLiteral(literalName, newValue));
			}
			
		}
	}

	
	/**
	 * Sets a new assignment of the literal.
	 * If that literal is not part of the clause, the literal is
	 * added to the clause.
	 * 
	 * @param literal The literal to be changed.
	 * @param newValue The truth value of given literal.
	 */
	public void setLiteralAssignment(LogicLiteral literal, boolean newValue) {
		if(newValue) {
			if(negativeNameSet.contains(literal.getName())) { 
				for(LogicLiteral lit : negativeLiteralSet) {
					if(lit.getName() == literal.getName()) {
						negativeLiteralSet.remove(lit);
						positiveLiteralSet.add(lit);
					}
				} 
			} else {
				// If not already part of the clause, then:
				addPositiveLiteral(literal);
			}
			
		}
		else if(!newValue) {
			if(positiveNameSet.contains(literal.getName())) { 
				for(LogicLiteral lit : positiveLiteralSet) {
					if(lit.getName() == literal.getName()) {
						positiveLiteralSet.remove(lit);
						negativeLiteralSet.add(lit);
					}
				}
			} else {
				// If not already part of the clause, then:
				addNegativeLiteral(literal);
			}
			
		}
	}

    /**
     * Checks whether any of the literals is true, and thus satisfies 
     * this clause.
     * 
     * @return {@code true} if any literal satisfies this clause, and 
     *         {@code false} otherwise.
     */
    public boolean isSatisfied() {
        for (LogicLiteral literal : positiveLiteralSet) {
            if (literal.isTrue()) {
                return true;
            }
        }

        for (LogicLiteral literal : negativeLiteralSet) {
            if (literal.isFalse()) {
                return true;
            }
        }

        return false;
    }
    
    
    private void generateNf() {
    	TODO
    }
    private void generateDdnnf() {
    	TODO
    }
	
	/*
	 * Below are all interface methods implemented
	 */

    /**
     * Sets the a new boolean assignment of
     * this logic rule
     */
    @Override
    public void setAssignment(boolean assignment) {
        this.assignment = assignment;
   	 //TODO: re-evaluate assignment of clause
   	 TODO
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
   	 //TODO: re-evaluate assignment of clause
   	 TODO
    }
    
    /**
     * Sets the assignment of this logic
     * rule to true
     */
    @Override
    public void setTrue() {
        this.assignment = true;
   	 //TODO: re-evaluate assignment of clause
   	 TODO
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
        Set<LogicLiteral> LogicLiteralSet = new TreeSet<>(positiveLiteralSet);
        LogicLiteralSet.addAll(negativeLiteralSet);
        
        return LogicLiteralSet;
    }
    
    /**
     * @return Returns an empty ArrayList
     */
    @Override
    public List<Clause> getClauses() {
    	return new ArrayList<Clause>(Arrays.asList(this));
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
