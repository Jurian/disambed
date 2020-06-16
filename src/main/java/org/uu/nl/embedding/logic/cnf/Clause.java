package org.uu.nl.embedding.logic.cnf;

import org.uu.nl.embedding.lensr.DdnnfGraph;
import org.uu.nl.embedding.logic.LogicRule;

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

	private String name = null;
	private String cnfName;
	private String ddnnfName;
	
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
    

   
	/**
	 * Constructor method for the clause class
	 * @param cnfRule The cnfRule that should be a clause.
	 * @param isNegated The boolean truth value if the
	 * literal in this clause should be negated or not.
	 * In some arbitrary specific order.
	 * @param nfRule The Normal Form formula this clause
	 * is generated from.
    public Clause(final CnfLogicRule cnfRule, final boolean isNegated, NormalLogicRule nfRule) {
    	super();
    	if(cnfRule instanceof CnfFormula) {
    		throw new IllegalArgumentException("CNF formula cannot implicitly be converted to a Clause."); }
    	
    	else if(cnfRule instanceof Clause) {
    		for(LogicLiteral literal : cnfRule.getPositiveLiterals()) {
        		this.addPositiveLiteral(literal);
    		}
			for(LogicLiteral literal : cnfRule.getNegativeLiterals()) {
				this.addNegativeLiteral(literal);
			}
			
			this.cnfName = this.toString();
			
			/*
			if(nfRule == null) { nfRule = generateNf(); }
			this.inNf = nfRule;
			this.inDdnnf = nfRule.getDdnnfRule(); // Checken of dit goed gaat qua compile volgorde!!!!!!
			this.ddnnfName = nfRule.getDdnnfName();
			
			this.assignment = isSatisfied();
    	}
    	// else Literal constructor is used for instantiation.
    }
    */
    
	/**
	 * Constructor method for the clause class
	 * @param orderedLiterals The literals of this clause
	 * , e.g. "p", or "isCar". In some arbitrary specific 
	 * order.
	 * @param negated The boolean truth value if the
	 * literal in this clause should be negated or not.
	 * In some arbitrary specific order.
	 * @param nfRule The Normal Form formula this clause
	 * is generated from.
	 */
	public Clause(final LogicLiteral[] orderedLiterals, final boolean[] orderedNegated) {
		super();
		if(!(orderedLiterals.length == orderedNegated.length)) {
			throw new IllegalArgumentException("The two arrays do not have the same length."); }
		
		for(int i = 0; i < orderedLiterals.length; i++) {
			if(orderedNegated[i]) { addPositiveLiteral(orderedLiterals[i]); }
			else { addNegativeLiteral(orderedLiterals[i]); }
		}
		
		this.cnfName = this.toString();
		
		/*
		if(nfRule == null) { nfRule = generateNf(); }
		this.inNf = nfRule;
		this.inDdnnf = nfRule.getDdnnfRule(); // Checken of dit goed gaat qua compile volgorde!!!!!!
		this.ddnnfName = nfRule.getDdnnfName();
		*/
		this.assignment = isSatisfied();
	}
	
	/**
	 * Constructor method for the clause class
	 * @param literal The literal in this clause
	 * , e.g. "p", or "isCar".
	 * @param negated The boolean truth value if the
	 * literal in this clause should be negated or not.
	 * @param nfRule The Normal Form formula this clause
	 * is generated from.
	 */
	public Clause(final LogicLiteral literal, boolean negated) {
		super();
		if(!negated) { addPositiveLiteral(literal); }
		else { addNegativeLiteral(literal); }

		this.cnfName = this.toString();

		/*
		if(nfRule == null) { nfRule = generateNf(); }
		this.inNf = nfRule;
		this.inDdnnf = nfRule.getDdnnfRule(); // Checken of dit goed gaat qua compile volgorde!!!!!!
		this.ddnnfName = nfRule.getDdnnfName();
		*/
		this.assignment = isSatisfied();
	}
	
	/**
	 * Constructor method for the clause class
	 * @param name The name of the literal in this clause
	 * , e.g. "p", or "isCar".
	 * @param value The starting boolean truth value of 
	 * the literal in this clause.
	 * @param negated The boolean truth value if the
	 * literal in this clause should be negated or not.
	 * @param nfRule The Normal Form formula this clause
	 * is generated from.
	 */
	public Clause(final String literalName, boolean value, boolean negated) {
		super();
		LogicLiteral literal = new LogicLiteral(literalName, value);
		if(!negated) { addPositiveLiteral(literal); }
		else { addNegativeLiteral(literal); }
		
		this.cnfName = this.toString();

		/*
		if(nfRule == null) { nfRule = generateNf(); }
		this.inNf = nfRule;
		this.inDdnnf = nfRule.getDdnnfRule(); // Checken of dit goed gaat qua compile volgorde!!!!!!
		this.ddnnfName = nfRule.getDdnnfName();
		*/
		this.assignment = isSatisfied();
	}

    /**
     * Adds a non-negated literal to this clause.
     * 
     * @param LogicLiteral the literal to add.
     */
    public void addPositiveLiteral(LogicLiteral literal) {
		positiveLiteralSet.add(literal);
		positiveNameSet.add(literal.getName());
 		this.assignment = isSatisfied();
    }

    /**
     * Adds a negated literal to this clause.
     * 
     * @param LogicLiteral the literal to add.
     */
    public void addNegativeLiteral(LogicLiteral literal) {
    	negativeLiteralSet.add(literal);
    	negativeNameSet.add(literal.getName());
		this.assignment = isSatisfied();
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
		this.assignment = isSatisfied();
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
		while(!removed) {
			
			for(LogicLiteral literal : positiveLiteralSet) {
				if(literal.getName() == literalName) {
					this.positiveLiteralSet.remove(literal);
					removed = true;
				}
			}
			for(LogicLiteral literal : negativeLiteralSet) {
				if(literal.getName() == literalName) {
					this.negativeLiteralSet.remove(literal);
					removed = true;
				}
			}
			if(!removed) {throw new IllegalArgumentException("No literal with specified name in this clause.");}
			removed = true;
		}
		this.assignment = isSatisfied();
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
        for(LogicLiteral literal : positiveLiteralSet) {
            if(literal.isTrue()) {
                return true;
            }
        }

        for(LogicLiteral literal : negativeLiteralSet) {
            if(literal.isFalse()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Method to merge two CNF clauses to one.
     * @param formula The other clause to merge this one with
     * @return Returns the resulting clause from this clause and
     * the given clause.
     */
    public Clause mergeWith(Clause clause) {
    	ArrayList<LogicLiteral> litList = new ArrayList<LogicLiteral>();
    	boolean[] boolArray = new boolean[(positiveLiteralSet.size() + negativeLiteralSet.size())];
    	int counter = 0;
    	// Loop through both positive and negative literal sets
    	// and add them to the same list, while tracking their
    	// negation value.
    	for(LogicLiteral literal : positiveLiteralSet) {
    		litList.add(literal);
    		boolArray[counter] = true;
    		counter++;
    	}
    	for(LogicLiteral literal : negativeLiteralSet) {
    		litList.add(literal);
    		boolArray[counter] = false;
    		counter++;
    	}
    	// Convert the literal list to an array.
    	LogicLiteral[] litArray = litList.toArray(new LogicLiteral[0]);
    	
    	// Return a new clause using the literal and boolean arrays
    	// as parameters.
    	return new Clause(litArray, boolArray);
    }
    
    /**
     * Method for satisfying this clause.
     */
    private void satisfy() {
    	
    	// While no new assignment is set that satisfies the clause.
    	boolean newlySatisfied = false;
		while(!newlySatisfied) {
	    	// If clause is already satisfied, satisfy clause with 
	    	// other literal.
	    	if(isSatisfied()) {
	    		
	    		boolean literalFound = false;
	        	boolean isFirst = true;
	        	LogicLiteral firstLit = new LogicLiteral("tempLit", false);
	    			// Loop through positive literals.
	                for(LogicLiteral literal : positiveLiteralSet) {
	                	// Select first literal for potential use later.
	                	if(isFirst) { 
	                		firstLit = literal; 
	                        // Set false after first iteration.
	                        isFirst = false;
	                        }
	                	// If the previous literal was found and falsified,
	                	// then set the current literal true and break loop.
	                	if(literalFound) {
	                		literal.setTrue();
	                		newlySatisfied = true;
	                	}
	                	// If current literal is true, set it false.
	                    if(literal.isTrue()) {
	                    	literalFound = true;
	                    	literal.setFalse();
	                    }
	                }
	    			// Loop through negative literals, and do same as above.
	                for(LogicLiteral literal : negativeLiteralSet) {
	                	if(literalFound) {
	                		literal.setFalse();
	                		newlySatisfied = true;
	                	}
	                    if(literal.isFalse()) {
	                    	literalFound = true;
	                    	literal.setTrue();
	                    }
	                }
	                /* When arrived here: the last negated literal
	                 * was satisfied. As such, we set the first
	                 * literal to satisfy.
	                 */
	                firstLit.setTrue();
	    	}
	    	else {
	    		// Satisfy clause by setting first positive literal true.
	            for(LogicLiteral literal : positiveLiteralSet) {
	            	literal.setTrue();
	            	newlySatisfied = true;
	            }
	    		// If no positive literal in clause: Satisfy clause by 
	            // setting first positive literal true.
                for(LogicLiteral literal : negativeLiteralSet) {
            		literal.setFalse();
            		newlySatisfied = true;
                }
	    	}
	    	// Some weird stuff is happening here...
	    	if(!newlySatisfied) { newlySatisfied = true; }
	    }
		// Re-assign clause.
		this.assignment = isSatisfied();
    }
    
    /**
     * Method for unsatisfying this clause.
     */
    private void unsatisfy() {
		// Unsatisfy clause by setting all positive literals false.
        for(LogicLiteral literal : positiveLiteralSet) {
        	literal.setFalse();
        }
		// Unsatisfy clause by setting all negative literals true.
        for(LogicLiteral literal : negativeLiteralSet) {
    		literal.setTrue();
        }
		// Re-assign clause.
		this.assignment = isSatisfied();
    }
    
    /**
     * Private method to generate a "normal form" formula if
     * this clause was not based on a normal form formula
     * already.
    private NormalLogicRule generateNf() {
    	Disjunction disj = null;
    	LogicLiteral firstLit = null, secondLit = null;
    	
        for(LogicLiteral literal : positiveLiteralSet) {
        	if(firstLit == null) { firstLit = literal; }
        	if(secondLit == null) { secondLit = literal; }
        	if(!(firstLit == null || secondLit == null)) {
        		if(disj==null) {
        			disj = new Disjunction(firstLit, secondLit);
        		}
        		else {
        			disj = new Disjunction(disj, literal);
        		}
        	}
        }
        for(LogicLiteral literal : negativeLiteralSet) {
        	if(firstLit == null) { firstLit = literal; }
        	if(secondLit == null) { secondLit = literal; }
        	if(!(firstLit == null || secondLit == null)) {
        		if(disj==null) {
        			disj = new Disjunction(firstLit, secondLit);
        		}
        		else {
        			disj = new Disjunction(disj, literal);
        		}
        	}
        }
        return disj;
    }
    */
    
    
	
	/*
	 * Below are all interface methods implemented
	 */

    /**
     * Sets the a new boolean assignment of
     * this logic rule
     */
    @Override
    public void setAssignment(boolean assignment) {
    	if(assignment) { satisfy(); }
    	else { unsatisfy(); }
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
     * @return Returns all literals of this clause 
     * in an ArrayList.
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
     * @return Returns this clause in a list.
     */
    @Override
    public List<Clause> getClauses() {
    	return new ArrayList<Clause>(Arrays.asList(this));
    }
    
    /**
     * @return Returns this clause as 
     * precedent of this logic rule
     */
    @Override
    public LogicRule getPrecedent() {
    	return this;
    }
    
    /**
     * @return Returns this clause as 
     * antecedent of this logic rule
     */
    @Override
    public LogicRule getAntecedent() {
    	return this;
    }
    
    /**
     * @return Returns this clause as 
     * LogicRule in Normal Form
    @Override
    public NormalLogicRule getNfRule() {
    	return this.inNf;
    }
    
    /**
     * @return Returns this clause as 
     * LogicRule in Conjunctive Normal Form
    @Override
    public CnfLogicRule getCnfRule() {
    	return this.inCnf;
    }
    
    /**
     * @return Returns this clause as 
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

        return this.name == ((Clause) obj).getName();
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
