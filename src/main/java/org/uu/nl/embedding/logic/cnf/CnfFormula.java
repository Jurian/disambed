package org.uu.nl.embedding.logic.cnf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.uu.nl.embedding.lensr.DdnnfGraph;
import org.uu.nl.embedding.logic.LogicRule;
import org.uu.nl.embedding.logic.ddnnf.DdnnfLogicRule;

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

    private final static Logger logger = Logger.getLogger(CnfFormula.class);

	private boolean assignment;
	private List<HashMap<LogicLiteral, Boolean>> trueAssignments, falseAssignments;
	
	private String name = null;
	private String cnfName;
	private String ddnnfName;

	private DdnnfGraph ddnnfGraph;
	private DdnnfLogicRule ddnnfRule;
	
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
		this.trueAssignments = fetchTrueAssignments();
		this.falseAssignments = fetchFalseAssignments();
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
		this.trueAssignments = fetchTrueAssignments();
		this.falseAssignments = fetchFalseAssignments();
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
    
    public String trueAssignmentsToString() {
    	String result = " - TRUE ASSIGNMENTS - \n";
    	List<HashMap<LogicLiteral, Boolean>> assignList = (ArrayList) getTrueAssignments();
    	HashMap<LogicLiteral, Boolean> map;
    	
    	for (int i = 0; i < assignList.size(); i++) {

    		result += "No. of assignment: " + i + "\n";
    		map = assignList.get(i);
    		for (Map.Entry<LogicLiteral, Boolean> entry : map.entrySet()) {
    			result += (entry.getKey().toString() + ": " + entry.getValue().toString() + "\n");
    		}
    		result += "\n\n";
    	}
    	
    	return result;
    }
    
    public List<HashMap<LogicLiteral, Boolean>> getTrueAssignments() {
    	return this.trueAssignments;
    }

    private List<HashMap<LogicLiteral, Boolean>> fetchTrueAssignments() {
    	List<HashMap<LogicLiteral, Boolean>> list1, list2;
    	HashMap<LogicLiteral, Boolean> intersectContra, intersectContraAll;
    	intersectContraAll = new HashMap<LogicLiteral, Boolean>();
    	
    	Clause clause1, clause2;
    	
    	// Check for contradicting literals in the different clauses.
    	for (int i = 0; i < this.clauseList.size(); i++) {

    		clause1 = this.clauseList.get(i);
	    	list1 = clause1.getTrueAssignments();
    		for (int j = i+1; j < this.clauseList.size(); j++) {
        		clause2 = this.clauseList.get(j);
    			// Skip checks if clause2 is clause1.
    			if (clause1 == clause2) { continue; }

    	    	list2 = clause2.getTrueAssignments();
    	    	
    	    	// Check if there is no intersection of literals between clauses.
    	    	intersectContra = new HashMap<LogicLiteral, Boolean>(list1.get(0));
    	    	intersectContra.keySet().retainAll(list2.get(0).keySet());
    	    	boolean allContra = true;
    	    	
    			if (intersectContra.size() != 0) {
    				for (Map.Entry<LogicLiteral, Boolean> entry : intersectContra.entrySet()) {

    					if (clause1.getPositiveLiterals().contains(entry.getKey()) && 
    							clause2.getNegativeLiterals().contains(entry.getKey())) {
    						intersectContra.put(entry.getKey(), true);
    						intersectContraAll.put(entry.getKey(), true);
    						
    					} else if (clause1.getNegativeLiterals().contains(entry.getKey()) && 
    							clause2.getPositiveLiterals().contains(entry.getKey())) {
    						intersectContra.put(entry.getKey(), true);
    						intersectContraAll.put(entry.getKey(), true);
    						
    					} else { 
    						intersectContra.put(entry.getKey(), false);
    						if (!intersectContraAll.containsKey(entry.getKey())) { intersectContraAll.put(entry.getKey(), false); }
    						allContra = false; }
    				}

        			// Check for all contradictions and if sizes are the same as any of the literal sets
        			if (allContra && (intersectContra.size() == list1.get(0).size() || intersectContra.size() == list2.get(0).size())) {
        				return new ArrayList<HashMap<LogicLiteral, Boolean>>(); 
        			}
    			}
    		}
    	}
    	
    	/*
    	 * Add all literals unless they are an intersecting literal.
    	 */
    	List<HashMap<LogicLiteral, Boolean>> oldList, newList, curMapList;
    	oldList = new ArrayList<HashMap<LogicLiteral, Boolean>>(); 
    	HashMap<LogicLiteral, Boolean> oldLitsMap, newLitsMap, desectMap;
    	for (int i = 0; i < this.clauseList.size(); i++) {
    		
    		clause1 = this.clauseList.get(i);
    		// if it is the first clause, get the assignments and
    		// skip the rest to the next iteration.
    		if (i == 0) {
    			for (HashMap<LogicLiteral, Boolean> map : clause1.getTrueAssignments()) {

    				desectMap = new HashMap<LogicLiteral, Boolean>();
    				for (Map.Entry<LogicLiteral, Boolean> entry : map.entrySet()) {
    					if (!intersectContraAll.containsKey(entry.getKey())) {
    	    				desectMap.put(entry.getKey(), entry.getValue());
    					} 
    				}
    			}
    			// Skip over rest of loop; go to next iteration.
    			continue;
    		}
    		
			curMapList = clause1.getTrueAssignments();
			newList = new ArrayList<HashMap<LogicLiteral, Boolean>>();
    		
    		for (int j = 0; j < oldList.size(); j++) {
    			oldLitsMap = oldList.get(j);
    			
    			for (int s = 0; s < curMapList.size(); s++) {
        			newLitsMap = oldLitsMap;
        			for (Map.Entry<LogicLiteral, Boolean> entry : curMapList.get(s).entrySet()) {
        				if (!intersectContraAll.containsKey(entry.getKey())) {
            				newLitsMap.put(entry.getKey(), entry.getValue());
        				}
        			}
        			newList.add(newLitsMap);
    			}
    			// newList has all combinations of oldMaps looped over new maps
    		}
    		// Update the oldList with all expanded maps.
    		oldList = new ArrayList<HashMap<LogicLiteral, Boolean>>(newList);
    	}

    	/*
    	 * Add all intersecting literals and possibly their different values.
    	 */
    	newList = new ArrayList<HashMap<LogicLiteral, Boolean>>();
    	boolean addSecondMap;
		for (Map.Entry<LogicLiteral, Boolean> entry : intersectContraAll.entrySet()) {

	    	addSecondMap = false;
	    	// Add intersecting literals and their potentially first value
			for (int i = 0; i < oldList.size(); i++) {
				newLitsMap = oldList.get(i);
				
				if(intersectContraAll.get(entry.getKey())) {
					newLitsMap.put(entry.getKey(), true);
					addSecondMap = true;
					
				} else { //intersecting literal != contradicting between clauses.
					for (int j = 0; j < this.clauseList.size(); j++) {
			    		
			    		if(this.clauseList.get(j).getPositiveLiterals().contains(entry.getKey())) {
			    			newLitsMap.put(entry.getKey(), true);
			    			break;
			    			
			    		} else if(this.clauseList.get(j).getNegativeLiterals().contains(entry.getKey())) {
			    			newLitsMap.put(entry.getKey(), false);
			    			break;
			    		}
					}
				}
				// Add newly generated map to the list.
				newList.add(newLitsMap);
			}
	    	
			// Redo this iteration but then for the other value.
			if(addSecondMap) { 
				for (int i = 0; i < oldList.size(); i++) {
					newLitsMap = oldList.get(i);
					
					if(intersectContraAll.get(entry.getKey())) {
						newLitsMap.put(entry.getKey(), false);
						
					} else { //intersecting literal != contradicting between clauses.
						for (int j = 0; j < this.clauseList.size(); j++) {
				    		
				    		if(this.clauseList.get(j).getPositiveLiterals().contains(entry.getKey())) {
				    			newLitsMap.put(entry.getKey(), true);
				    			break;
				    			
				    		} else if(this.clauseList.get(j).getNegativeLiterals().contains(entry.getKey())) {
				    			newLitsMap.put(entry.getKey(), false);
				    			break;
				    		}
						}
					}
					// Add newly generated map to the list.
					newList.add(newLitsMap);
				}
			}
    		// Update the oldList with all expanded maps.
    		oldList = new ArrayList<HashMap<LogicLiteral, Boolean>>(newList);
		}
		// Finally return the generated complete list of maps.
    	return oldList;
    	
    }

    
    public String falseAssignmentsToString() {
    	String result = " - FALSE ASSIGNMENTS - \n";
    	List<HashMap<LogicLiteral, Boolean>> assignList = (ArrayList) getFalseAssignments();
    	HashMap<LogicLiteral, Boolean> map;
    	
    	for (int i = 0; i < assignList.size(); i++) {

    		result += "No. of assignment: " + i + "\n";
    		map = assignList.get(i);
    		for (Map.Entry<LogicLiteral, Boolean> entry : map.entrySet()) {
    			result += (entry.getKey().toString() + ": " + entry.getValue().toString() + "\n");
    		}
    		result += "\n\n";
    	}
    	
    	return result;
    }
    
    public List<HashMap<LogicLiteral, Boolean>> getFalseAssignments() {
    	return this.falseAssignments;
    }

    /**
     * 
     * @return
     */
    private List<HashMap<LogicLiteral, Boolean>> fetchFalseAssignments() {
    	// An ArrayList of Lists of Mappings for both passed clauses, as well as,
    	// clauses currently looping through.
    	List<List<HashMap<LogicLiteral, Boolean>>> passedClauseAssignmentsLists = 
    			new ArrayList<List<HashMap<LogicLiteral, Boolean>>>();
    	List<List<HashMap<LogicLiteral, Boolean>>> iterationClauseAssignmentsLists = 
    			new ArrayList<List<HashMap<LogicLiteral, Boolean>>>();
    	// The resulting list with all possible assignments and 
    	// the current clause's assignment lists, old and new.
    	List<HashMap<LogicLiteral, Boolean>> resultList = 
    			new ArrayList<HashMap<LogicLiteral, Boolean>>();
    	List<HashMap<LogicLiteral, Boolean>> clauseAssignMapsListOld = 
    			new ArrayList<HashMap<LogicLiteral, Boolean>>();
    	List<HashMap<LogicLiteral, Boolean>> clauseAssignMapsListNew = 
    			new ArrayList<HashMap<LogicLiteral, Boolean>>();
    	// Lists for the current two clauses.
		List<HashMap<LogicLiteral, Boolean>> clause1MapList, clause2MapList;
    	// The currently constructed assignment mapping.
    	HashMap<LogicLiteral, Boolean> clause1Map, clause2Map, subResultMap;
    	// Counter for the first true clause in current iteration and
    	// a variable to hold the listSize.
    	int firstTrueClause, listSize;
    	
    	for (int i = 0; i < this.clauseList.size(); i++) {
    		
    		passedClauseAssignmentsLists.add(this.clauseList.get(i).getFalseAssignments());
    		firstTrueClause = getLiterals().size();
    		
			while (firstTrueClause > i) {
				// Firstly, get the lists of assignments for current iteration.
				for (int j = i+1; j < this.clauseList.size(); j++) {
    				
    				if (j >= firstTrueClause) {
    					iterationClauseAssignmentsLists.set(j-1, this.clauseList.get(j).getTrueAssignments());
        				
        			} else {
        				if (iterationClauseAssignmentsLists.size() >= j) {
        					iterationClauseAssignmentsLists.set(j-1, this.clauseList.get(j).getFalseAssignments());
        				} else {
        					iterationClauseAssignmentsLists.add(this.clauseList.get(j).getFalseAssignments()); }

        			}
    			}
				
				// Secondly, iterate through the different assignments.
				for (int j = 0; j < this.clauseList.size(); j++) {
					
					// Get appropriate mappings list.
					if (j <= i) {
						clause1MapList = passedClauseAssignmentsLists.get(j);
					} else {
						clause1MapList = iterationClauseAssignmentsLists.get(j-1); }
					
					// Reset the previous new list to fill it with new values.
					clauseAssignMapsListNew.clear();
					listSize = clause1MapList.size();
					// Loop through old mappings list if not empty and
					// add all mappings of the current clause to the list.
					for (int jc = 0; jc < listSize; jc++) {
						clause1Map = clause1MapList.get(jc);
						if (clauseAssignMapsListOld.size() == 0) {
							clauseAssignMapsListNew.add(clause1Map);
							
						} else {
							for (int floeps = 0; floeps < clauseAssignMapsListOld.size(); floeps++) {
								subResultMap = clauseAssignMapsListOld.get(floeps);
								subResultMap.putAll(clause1Map);
								clauseAssignMapsListNew.add(subResultMap);
								
							}
						}
					}
					// Save the new list in the old one.
					clauseAssignMapsListOld = new ArrayList<HashMap<LogicLiteral, Boolean>>(clauseAssignMapsListNew);

					for (int k = j+1; k < this.clauseList.size(); k++) {

						// Get appropriate mappings list.
						if (k <= i) {
							clause2MapList = passedClauseAssignmentsLists.get(j);
						} else {
							clause2MapList = iterationClauseAssignmentsLists.get(j); }
						
						clauseAssignMapsListNew.clear();
						listSize = clause2MapList.size();
						for (int kc = 0; kc < listSize; kc++) {
							clause2Map = clause2MapList.get(kc);
							for (int jc = 0; jc < clauseAssignMapsListOld.size(); jc++) {

								subResultMap = clauseAssignMapsListOld.get(jc);
								subResultMap.putAll(clause2Map);
								clauseAssignMapsListNew.add(subResultMap);
								
							}
						}
						// Save the new list in the old one.
						clauseAssignMapsListOld = new ArrayList<HashMap<LogicLiteral, Boolean>>(clauseAssignMapsListNew);
					}
				}
				// Decrement the first true clause and go to next iteration if
				// firstTrueClause > i.
				firstTrueClause--;
    			
    		}
    		
    		// Remove the false assignments and add true assignments.
    		passedClauseAssignmentsLists.set(i, this.clauseList.get(i).getTrueAssignments());
    		
    	}
    	
    	resultList = new ArrayList<HashMap<LogicLiteral, Boolean>>(clauseAssignMapsListOld);
    	return resultList;
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

