package org.uu.nl.embedding.logic.ddnnf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.uu.nl.embedding.lensr.DdnnfGraph;
import org.uu.nl.embedding.logic.LogicRule;
import org.uu.nl.embedding.logic.cnf.Clause;
import org.uu.nl.embedding.logic.cnf.CnfFormula;
import org.uu.nl.embedding.logic.cnf.CnfLogicRule;
import org.uu.nl.embedding.logic.cnf.LogicLiteral;
import org.uu.nl.embedding.util.ArrayUtils;

public class DdnnfFormula implements DdnnfLogicRule {

	private boolean assignment;

	private CnfLogicRule sourceRule;
	
	private String name = "Complex d-DNNF Rule";
	
    /**
     * The set of positive literals present in this clause.
     */
    private Set<DdnnfLiteral> positiveLiteralSet = new TreeSet<>();
    private final Set<String> positiveNameSet = new TreeSet<>();
    /**
     * The set of negative literals present in this clause.
     */
    private Set<DdnnfLiteral> negativeLiteralSet = new TreeSet<>();
    private Set<String> negativeNameSet = new TreeSet<>();
    /**
     * The set of negative literals present in this clause.
     */
    private final ArrayList<DdnnfLiteral> presentLiterals = new ArrayList<DdnnfLiteral>();
    private final ArrayList<String> literalNames = new ArrayList<String>();
    /**
     * The ordered rules that make up this clause.
     */
    private final ArrayList<DdnnfLogicRule> rules = new ArrayList<DdnnfLogicRule>();
	
    
    
	
	public DdnnfFormula(final DdnnfLogicRule[] orderedRules, final boolean[] orderedNegated, final String name) {
		super();
		if(!(orderedRules.length == orderedNegated.length)) {
			throw new IllegalArgumentException("The two arrays do not have the same length."); }
		
		for(int i = 0; i < orderedRules.length; i++) {
			if(orderedRules[i] instanceof DdnnfDateComparer) {
				if(orderedNegated[i]) { addPositiveLiteral((DdnnfDateComparer) orderedRules[i]); }
				else { addNegativeLiteral((DdnnfDateComparer) orderedRules[i]); 
				}
			} else if(orderedRules[i] instanceof DdnnfDate) {
				if(orderedNegated[i]) { addPositiveLiteral((DdnnfDate) orderedRules[i]); }
				else { addNegativeLiteral((DdnnfDate) orderedRules[i]); 
				}
			} else if(orderedRules[i] instanceof DdnnfLiteral) {
				if(orderedNegated[i]) { addPositiveLiteral((DdnnfLiteral) orderedRules[i]); }
				else { addNegativeLiteral((DdnnfLiteral) orderedRules[i]); }
			}
			else if((orderedRules[i] instanceof DdnnfClause) || (orderedRules[i] instanceof DdnnfFormula)) {
				this.presentLiterals.addAll(orderedRules[i].getPositiveLiterals());
				this.presentLiterals.addAll(orderedRules[i].getNegativeLiterals());
			}
			else {
				System.out.println("This DdnnfLogicRule is:" + orderedRules[i].toString());
				throw new IllegalArgumentException("The provided DdnnfLogicRule is not recognized."); 
				
			}
			// Add current rule to the ArrayList.
			rules.add(orderedRules[i]);
		}
		
		setAllLiteralNames();
		//this.assignment = isSatisfied();
		//this.sourceRule = sourceRule;
		this.name = name;
	}
	
	/*
	public DdnnfFormula(final CnfLogicRule sourceRule) {
		super();
		for(Clause clause : sourceRule.getClauses()) {
			convertCnfClause(clause);
		}
		if(sourceRule instanceof CnfFormula) {
			convertCnfFormula((CnfFormula) sourceRule);
		}

		this.assignment = isSatisfied();
		this.sourceRule = sourceRule;
		this.ddnnfGraph = generateGraph();
	}
	
    /**
     * Method to generate the list with the names
     * of all the literals.
     */
	private void setAllLiteralNames() {
		for(DdnnfLiteral literal : this.presentLiterals) {
			if(this.positiveLiteralSet.contains(literal)) {
				this.positiveNameSet.add(literal.getName());
			} else {
				this.negativeNameSet.add(literal.getName());
			}
			this.literalNames.add(literal.getName());
		}
	}
	/*
	/**
	 * Method to convert a CNF clause to a d-DNNF rule.
	 * @param clause The CNF clause to be converted.
	 
	private ArrayList<DdnnfLogicRule> convertClause(final DdnnfClause clause) {
		// clause = (p and q) or (p and r)
		ArrayList<DdnnfLogicRule> allRules = clause.getRules();
		
		// Initialize temporary sets.
	    Set<DdnnfLiteral> posLits = new TreeSet<>();
	    Set<DdnnfLiteral> negLits = new TreeSet<>();
	    // Initialize ordered list of all literals.
	    DdnnfLiteral literal;
	    ArrayList<DdnnfLiteral> allLits = new ArrayList<DdnnfLiteral>();
	    
	    // Convert CNF literals to d-DNNF literals and
	    
	    for(DdnnfLogicRule rule : clause.getRules()) {
	    	if(rule instanceof DdnnfLiteral) {
	    	    // Literals to the set and ArrayList.
	    	    if(clause.getPositiveLiterals().contains(rule)) {
	    	    	allLits.add((DdnnfLiteral) rule);
	    	    	posLits.add((DdnnfLiteral) rule);
	    	    }
	    	    if(clause.getNegativeLiterals().contains(rule)) {
	    	    	allLits.add((DdnnfLiteral) rule);
	    	    	negLits.add((DdnnfLiteral) rule);
	    	    }
	    	}
	    	else if(rule instanceof DdnnfClause) {
	    		
	    	}
	    }	

	    // Create the to-be returned list with the resulting rules.
	    ArrayList<DdnnfLogicRule> resRules = new ArrayList<DdnnfLogicRule>();
	    // Creating temporary variables.
	    DdnnfLogicRule[] finishedRules = new DdnnfLogicRule[allRules.size()];
	    DdnnfLogicRule[] litArray = new DdnnfLogicRule[allRules.size()];
		boolean[] areNegated = new boolean[allRules.size()];
		// getsNegVal states what negated value will be assigned in
		// the new d-DNNF formula. 
		boolean[] getsNegVal = new boolean[allRules.size()];
		
		for(int i = 0; i < allRules.size(); i++) {
			finishedRules[i] = allRules.get(i);
			areNegated[i] = allRules.get(i).isNegated();
			
			for(int i_f = 0; i_f < i; i_f++) {
				
			}
		}

	    
	    // Create the to-be returned list with the resulting rules.
	    ArrayList<DdnnfLogicRule> resRules = new ArrayList<DdnnfLogicRule>();
	    // Creating temporary variables.
		DdnnfLiteral[] finishedLits = new DdnnfLiteral[allLits.size()];
		DdnnfLiteral[] litArray = new DdnnfLiteral[allLits.size()];
		boolean[] areNegated = new boolean[allLits.size()];
		// getsNegVal states what negated value will be assigned in
		// the new d-DNNF formula. 
		boolean[] getsNegVal = new boolean[allLits.size()];
		
	    for(int i = 0; i < allLits.size(); i++) {
	    	finishedLits[i] = allLits.get(i);
	    	areNegated[i] = allLits.get(i).isNegated();
	    	// Fill litArray with literals which have already been passed.
	    	for(int i_f = 0; i_f <= i; i_f++) { // CHECKEN OF DIT GOED GAAT!!!!!!!!!!!!!!!!!
	    		litArray[i_f] = finishedLits[i_f];
	    		// Finished literals get their actual negation.
	    		getsNegVal[i_f] = areNegated[i_f];
	    	}
	    	// Fill litArray with remaining literals.
	    	for(int j = i+1; j < allLits.size(); j++) {
	    		litArray[j] = allLits.get(j);
	    		// Unfinished literals get the opposite of their actual 
	    		// negation.
	    		getsNegVal[j] = !areNegated[j];
	    	}
	    	// Add the newly formed d-DNNF formula to the rule list.
	    	resRules.add(new DdnnfFormula(litArray, getsNegVal, clause.getSourceRule()));
	    }
	    return resRules;
	}
	
	/**
	 * Method to convert a CNF formula to a d-DNNF rule.
	 * @param clause The CNF formula to be converted.
	 
	private void convertCnfFormula(final CnfFormula formula) {
		boolean[] clauseNoDuplicate = new boolean[formula.getClauses().size()];
		Arrays.fill(clauseNoDuplicate, true);
		
		Clause[] clauseArr = formula.getClauses().toArray(new Clause[0]);
		ArrayList<DdnnfLogicRule> deduplicatedRules;
		
		// Check for each CNF clause if it has duplicates with any other clause,
		// and resolve this issue in creating new d-DNNF clauses.
		for(int i = 0; i < clauseArr.length; i++) {
			for(int j = i+1; j < clauseArr.length; j++) {

				if(!Collections.disjoint(clauseArr[i].getLiterals(), clauseArr[j].getLiterals())) {
					clauseNoDuplicate[i] = false;
					clauseNoDuplicate[j] = false;
					deduplicatedRules = resolveFormulaDuplicates(clauseArr[i], clauseArr[j], formula);
					for(DdnnfLogicRule form : deduplicatedRules) {
						
					}
					this.rules.addAll(
				}
			}
		}
		
		LogicLiteral[] posLiterals, negLiterals, literals;
		boolean[] areNegatedPos, areNegatedNeg, areNegated;
		
		// For the CNF clauses without duplicates with any other clause, create a
		// d-DNNF clause from this CNF clause.
		for(int i = 0; i < clauseArr.length; i++) {
			if(clauseNoDuplicate[i]) {
				posLiterals = clauseArr[i].getPositiveLiterals().toArray(new LogicLiteral[0]);
				areNegatedPos = new boolean[posLiterals.length];
				negLiterals = clauseArr[i].getNegativeLiterals().toArray(new LogicLiteral[0]);
				areNegatedNeg = new boolean[negLiterals.length];
				
				literals = ArrayUtils.concatenate(posLiterals, negLiterals);
				areNegated = ArrayUtils.concatenate(areNegatedPos, areNegatedNeg);
				
				this.rules.add(new DdnnfClause(literals, areNegated, clauseArr[i]));
			}
		}
	    // Add all literals to the ArrayList and set the names.
	    this.presentLiterals.addAll(posLits);
	    this.presentLiterals.addAll(negLits);
	    setAllLiteralNames();
	}
	
	/**
	 * 
	 * @param clauseL
	 * @param clauseR
	 * @param sourceFormula
	 * @return
	 
	private ArrayList<DdnnfLogicRule> resolveFormulaDuplicates(final Clause clauseL, final Clause clauseR, final CnfFormula sourceFormula) {
		
		// Convert all CNF literals to d-DNNF literals.
		Set<DdnnfLiteral> leftPos = new TreeSet<DdnnfLiteral>();
		for(LogicLiteral literal : clauseL.getPositiveLiterals()) {
			leftPos.add(new DdnnfLiteral(literal.getName(), 
									literal.getAssignment(), literal.isNegated(), 
									literal));
		}
		Set<DdnnfLiteral> leftNeg = new TreeSet<DdnnfLiteral>();
		for(LogicLiteral literal : clauseL.getNegativeLiterals()) {
			leftNeg.add(new DdnnfLiteral(literal.getName(), 
									literal.getAssignment(), literal.isNegated(), 
									literal));
		}
		Set<DdnnfLiteral> rightPos = new TreeSet<DdnnfLiteral>();
		for(LogicLiteral literal : clauseR.getPositiveLiterals()) {
			rightPos.add(new DdnnfLiteral(literal.getName(), 
									literal.getAssignment(), literal.isNegated(), 
									literal));
		}
		Set<DdnnfLiteral> rightNeg = new TreeSet<DdnnfLiteral>();
		for(LogicLiteral literal : clauseR.getNegativeLiterals()) {
			rightNeg.add(new DdnnfLiteral(literal.getName(), 
									literal.getAssignment(), literal.isNegated(), 
									literal));
		}
		
		// Initialize variables.
		ArrayList<DdnnfLogicRule> resClauses = new ArrayList<DdnnfLogicRule>();
		DdnnfLogicRule formula;
		DdnnfLiteral[] literals;
		boolean[] areNegated;
		
		// For each combination of literals, make new d-DNNF formula.
		for(DdnnfLiteral leftLit : leftPos) {
			for(DdnnfLiteral rightLit : rightPos) {
				// Declare objects.
				literals = new DdnnfLiteral[] {leftLit, rightLit};
				areNegated = new boolean[] {true, true};
				// Create new d-DNNF formula, and resolve if it's
				// a duplicate conjunction, e.g. (p AND p).
				formula = resolveDuplicateConjunction(new DdnnfFormula(literals, areNegated, sourceFormula));
				
				resClauses.add(formula);
			}
			for(DdnnfLiteral rightLit : rightNeg) {
				// Declare objects.
				literals = new DdnnfLiteral[] {leftLit, rightLit};
				areNegated = new boolean[] {true, false};
				// Create new d-DNNF formula, and resolve if it's
				// a duplicate conjunction, e.g. (p AND p).
				formula = resolveDuplicateConjunction(new DdnnfFormula(literals, areNegated, sourceFormula));
				
				resClauses.add(formula);
			}
		}
		for(DdnnfLiteral leftLit : leftNeg) {
			for(DdnnfLiteral rightLit : rightPos) {
				// Declare objects.
				literals = new DdnnfLiteral[] {leftLit, rightLit};
				areNegated = new boolean[] {false, true};
				// Create new d-DNNF formula, and resolve if it's
				// a duplicate conjunction, e.g. (p AND p).
				formula = resolveDuplicateConjunction(new DdnnfFormula(literals, areNegated, sourceFormula));
				
				resClauses.add(formula);
			}
			for(DdnnfLiteral rightLit : rightNeg) {
				// Declare objects.
				literals = new DdnnfLiteral[] {leftLit, rightLit};
				areNegated = new boolean[] {false, false};
				// Create new d-DNNF formula, and resolve if it's
				// a duplicate conjunction, e.g. (p AND p).
				formula = resolveDuplicateConjunction(new DdnnfFormula(literals, areNegated, sourceFormula));
				
				resClauses.add(formula);
			}
		}
		return resClauses;
	}
	
	/**
	 * If given formula has (p AND p), then it 
	 * resolves to only the literal (p).
	 * @param formula
	 * @return
	 
	private DdnnfLogicRule resolveDuplicateConjunction(DdnnfFormula formula) {
		if(formula.getAntecedent() == formula.getPrecedent()) {
			return (DdnnfLogicRule) formula.getAntecedent();
		}
		return formula;
	}
	*/
	
    /**
     * Adds a non-negated literal to this clause.
     * 
     * @param LogicLiteral the literal to add.
     */
    public void addPositiveLiteral(DdnnfLiteral literal) {
		this.positiveLiteralSet.add(literal);
		this.positiveNameSet.add(literal.getName());
		
		this.presentLiterals.add(literal);
		this.literalNames.add(literal.getName());
		
 		//this.assignment = isSatisfied();
    }

    /**
     * Adds a negated literal to this clause.
     * 
     * @param LogicLiteral the literal to add.
     */
    public void addNegativeLiteral(DdnnfLiteral literal) {
    	negativeLiteralSet.add(literal);
    	negativeNameSet.add(literal.getName());
		
		this.presentLiterals.add(literal);
		this.literalNames.add(literal.getName());
		
		//this.assignment = isSatisfied();
    }
	
	
    /**
     * Removes any given literal from this clause,
     * if the literal is part of the clause. Only for 
     * literals exclusive to this d-DNNF clause.
     * 
     * @param literal The literal to remove.
     
	public void removeLiteral(DdnnfLiteral literal) {
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
     
	public void removeLiteral(String literalName) {
		boolean removed = false;
		while(!removed) {
			
			for(DdnnfLiteral literal : positiveLiteralSet) {
				if(literal.getName() == literalName) {
					this.positiveLiteralSet.remove(literal);
					removed = true;
				}
			}
			for(DdnnfLiteral literal : negativeLiteralSet) {
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
	 
	public void setLiteralAssignment(String literalName, boolean newValue) {
		if(newValue) {
			if(negativeNameSet.contains(literalName)) { 
				for(DdnnfLiteral lit : negativeLiteralSet) {
					if(lit.getName() == literalName) {
						negativeLiteralSet.remove(lit);
						positiveLiteralSet.add(lit);
					}
				} 
			} else {
				throw new IllegalArgumentException("No literal with that name present in this clause.");
			}
			
		}
		else if(!newValue) {
			if(positiveNameSet.contains(literalName)) { 
				for(DdnnfLiteral lit : positiveLiteralSet) {
					if(lit.getName() == literalName) {
						positiveLiteralSet.remove(lit);
						negativeLiteralSet.add(lit);
					}
				}
			} else {
				throw new IllegalArgumentException("No literal with that name present in this clause.");
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
	 
	public void setLiteralAssignment(DdnnfLiteral literal, boolean newValue) {
		if(newValue) {
			if(negativeNameSet.contains(literal.getName())) { 
				for(DdnnfLiteral lit : negativeLiteralSet) {
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
				for(DdnnfLiteral lit : positiveLiteralSet) {
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
        return this.sourceRule.isSatisfied();
    }
    /*
    /**
     * @return Returns all literals of this clause 
     * specifically in an ArrayList. It does not
     * return any of the literals that are present 
     * in one of the clause or formula children of 
     * this clause.
     
    public Set<DdnnfLiteral> getOwnLiterals() {
        Set<DdnnfLiteral> LogicLiteralSet = new TreeSet<>(positiveLiteralSet);
        LogicLiteralSet.addAll(negativeLiteralSet);
        
        return LogicLiteralSet;
    }
    
    private DdnnfGraph generateGraph() {
		DdnnfGraph graph;
		graph = this.rules.get(0).getDdnnfGraph();
		
    	for(int i = 1; i < this.rules.size(); i++) {
    		graph = new DdnnfGraph(this, graph, this.rules.get(i).getDdnnfGraph());
    	}
		return graph;
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
    	this.sourceRule.setAssignment(assignment);
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
    	this.sourceRule.setFalse();
    }
    
    /**
     * Sets the assignment of this logic
     * rule to true
     */
    @Override
    public void setTrue() {
    	this.sourceRule.setTrue();
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
    
    @Override
    public void negate() {
    	Set<DdnnfLiteral> posLitSet, negLitSet;
    	posLitSet = this.positiveLiteralSet;
    	negLitSet = this.negativeLiteralSet;
    	
    	this.positiveLiteralSet = negLitSet;
    	this.negativeLiteralSet = posLitSet;
    	setAllLiteralNames();
    	
    	for(DdnnfLogicRule rule : this.rules) {
    		rule.negate();
    	}
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

        sb.append("(");
        String separator = "";

        for (DdnnfLogicRule rule : this.rules) {
            sb.append(separator);
            separator = " AND ";

            if(negativeLiteralSet.contains(rule)) {
                sb.append("NOT ");
            }
            sb.append(rule.toString());
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

        sb.append("(");
        String separator = "";

        for (DdnnfLogicRule rule : this.rules) {
            sb.append(separator);
            separator = " OR ";

            if(negativeLiteralSet.contains(rule)) {
                sb.append("NOT ");
            }
            if(rule instanceof DdnnfLiteral) {
                sb.append(rule.getAssignment());
            } else {
                sb.append(rule.toString());
            }
        }
        return sb.append(")").toString();
    }
    
    /**
     * @return Returns the name in CNF of this
     * literal
     */
    @Override
    public String getCnfName() {
    	return this.sourceRule.getName();
    }
    
    /**
     * @return Returns the name in d-DNNF of this
     * literal
     */
    @Override
    public String getDdnnfName() {
    	return this.name;
    }
    
    /**
     * @return Returns all literals of this clause 
     * in an ArrayList.
     */
    @Override
    public ArrayList<DdnnfLiteral> getLiterals() {
        return this.presentLiterals;
    }
    
    /**
     * @return Returns all positive literals of this 
     * clause in an ArrayList.
     */
    @Override
    public Set<DdnnfLiteral> getPositiveLiterals() {
        return this.positiveLiteralSet;
    }
    
    /**
     * @return Returns all negative literals of this 
     * clause in an ArrayList.
     */
    @Override
    public Set<DdnnfLiteral> getNegativeLiterals() {
        return this.negativeLiteralSet;
    }
    
    
    /**
     * @return Returns the rules in this clause.
     */
    @Override
    public List<DdnnfLogicRule> getRules() {
    	return this.rules;
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

        return this.getName() == ((DdnnfFormula) obj).getName();
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
    
	@Override
	public CnfLogicRule getSourceRule() {
		return this.sourceRule;
	}
    

}
