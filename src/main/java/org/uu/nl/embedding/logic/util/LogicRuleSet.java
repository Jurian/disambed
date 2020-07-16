package org.uu.nl.embedding.logic.util;

import java.util.ArrayList;
import java.util.HashMap;

import org.uu.nl.embedding.lensr.DdnnfGraph;
import org.uu.nl.embedding.logic.cnf.Clause;
import org.uu.nl.embedding.logic.cnf.CnfDateComparer;
import org.uu.nl.embedding.logic.cnf.CnfDateLogic;
import org.uu.nl.embedding.logic.cnf.CnfFormula;
import org.uu.nl.embedding.logic.cnf.LogicLiteral;
import org.uu.nl.embedding.logic.ddnnf.DdnnfClause;
import org.uu.nl.embedding.logic.ddnnf.DdnnfDate;
import org.uu.nl.embedding.logic.ddnnf.DdnnfDateComparer;
import org.uu.nl.embedding.logic.ddnnf.DdnnfFormula;
import org.uu.nl.embedding.logic.ddnnf.DdnnfLogicRule;

public class LogicRuleSet {
	
	final private String datesTypeRule;
	final private String comparison;
	private CnfFormula cnfFormula;
	private DdnnfLogicRule ddnnfFormulae;
	private DdnnfGraph ddnnfGraph;
	private DdnnfLogicRule mainDdnnfRule;
	
	public LogicRuleSet(final String datesTypeRule, final String datum1, final String datum2, final String comparison) {
		
		this.datesTypeRule = datesTypeRule;
		this.comparison = comparison;
		
		if (datesTypeRule == "birth-death") {
			BirthDateBeforeDeathDateRule(datum1, datum2, comparison);
			
		} else if (datesTypeRule == "birth-baptised") {
			runBirthDateBeforeBaptisedRule(datum1, datum2, comparison);
			
		} else if (datesTypeRule == "baptised-death") {
			runBaptisedBeforeDeathRule(datum1, datum2, comparison);
			
		}
	}
	
	public String getRuleType() {
		return this.datesTypeRule;
	}
	
	public String getComparison() {
		return this.comparison;
	}
	
	public CnfFormula getCnfFormula() {
		return this.cnfFormula;
	}
	
	public DdnnfLogicRule getDdnnfRule() {
		return this.ddnnfFormulae;
	}
	
	public DdnnfGraph getGraph() {
		return this.ddnnfGraph;
	}
	
	public DdnnfLogicRule getMainRule() {
		return this.mainDdnnfRule;
	}
	
	
	public ArrayList<DdnnfLogicRule> splitDdnnfRules() {

		ArrayList<DdnnfLogicRule> queue = new ArrayList<DdnnfLogicRule>();
		
		queue.add(this.ddnnfFormulae);
		for (DdnnfLogicRule rule : queue) {
			if (rule.getClass() == this.ddnnfFormulae.getClass()) {
				queue.remove(rule);
				for (DdnnfLogicRule newRule : rule.getRules()) { queue.add(newRule); }
			}
		}

		ArrayList<DdnnfLogicRule> resultList = new ArrayList<DdnnfLogicRule>(queue);
		return resultList;
	}
	

	public static ArrayList<DdnnfLogicRule> splitDdnnfRules(DdnnfLogicRule ddnnfFormulae) {

		ArrayList<DdnnfLogicRule> queue = new ArrayList<DdnnfLogicRule>();
		
		queue.add(ddnnfFormulae);
		for (DdnnfLogicRule rule : queue) {
			if (rule.getClass() == ddnnfFormulae.getClass()) {
				queue.remove(rule);
				for (DdnnfLogicRule newRule : rule.getRules()) { queue.add(newRule); }
			}
		}

		ArrayList<DdnnfLogicRule> resultList = new ArrayList<DdnnfLogicRule>(queue);
		return resultList;
	}
	
	/**
	 * 
	 * @param datum1
	 * @param datum2
	 * @param comparison
	 * @return
	 */
	private void BirthDateBeforeDeathDateRule(final String datum1, final String datum2, final String comparison) {
		
		// Create SimpleDate classes.
		SimpleDate date1 = new SimpleDate(datum1);
		SimpleDate date2 = new SimpleDate(datum2);
		
		// CNF rule.
		CnfDateLogic cnfBirthDate = new CnfDateLogic(date1, "BirthDate", true, false);
		CnfDateLogic cnfDeathDate = new CnfDateLogic(date2, "DeathDate", true, false);
		CnfDateComparer cnfDate21Comparer = new CnfDateComparer(date1, date2, "CompareTo", true, false);
		if(comparison == "after") { cnfDate21Comparer.setComparisonAfter(); }
		else if(comparison == "before") { cnfDate21Comparer.setComparisonBefore(); }
		// CNF ArrayBuilders.
		LogicLiteral[] clauseLitArray1 = new LogicLiteral[] {cnfBirthDate, cnfDeathDate};
		LogicLiteral[] clauseLitArray2 = new LogicLiteral[] {cnfBirthDate, cnfDate21Comparer};
		boolean[] clauseBoolArray = new boolean[] {false, true};
		// CNF clauses.
		Clause cnfClause1 = new Clause(clauseLitArray1, clauseBoolArray);
		Clause cnfClause2 = new Clause(clauseLitArray2, clauseBoolArray);
		Clause[] cnfClauses = new Clause[] {cnfClause1, cnfClause2};
		// CNF formula
		this.cnfFormula = new CnfFormula(cnfClauses);
		
		// d-DNNF logic literals.
		DdnnfDate birthDate = new DdnnfDate(date1, "BirthDate", true);
		DdnnfDate deathDate = new DdnnfDate(date2, "DeathDate", true);
		DdnnfDateComparer date21Comparer = new DdnnfDateComparer(date2, date1, "CompareTo");
		
		// Array builders
		//5
		DdnnfLogicRule[] conj5LitArray = new DdnnfLogicRule[] {birthDate, deathDate, date21Comparer};
		boolean[] conj5boolArray = new boolean[] {false, false, false};
		//4
		DdnnfLogicRule[] conj4LitArray = new DdnnfLogicRule[] {birthDate, deathDate, date21Comparer};
		boolean[] conj4boolArray = new boolean[] {false, false, true};
		//3
		DdnnfLogicRule[] conj3LitArray = new DdnnfLogicRule[] {birthDate, deathDate, date21Comparer};
		boolean[] conj3boolArray = new boolean[] {false, true, false};
		//2
		DdnnfLogicRule[] conj2LitArray = new DdnnfLogicRule[] {birthDate, deathDate, date21Comparer};
		boolean[] conj2boolArray = new boolean[] {false, true, true};
		//1
		DdnnfLogicRule[] conj1LitArray = new DdnnfLogicRule[] {birthDate, deathDate, date21Comparer};
		boolean[] conj1boolArray = new boolean[] {true, true, true};
		
		
		// d-DNNF logic formulae.
		DdnnfFormula conj5 = new DdnnfFormula(conj5LitArray, conj5boolArray, "conjunction5");
		DdnnfFormula conj4 = new DdnnfFormula(conj4LitArray, conj4boolArray, "conjunction4");
		DdnnfFormula conj3 = new DdnnfFormula(conj3LitArray, conj3boolArray, "conjunction3");
		DdnnfFormula conj2 = new DdnnfFormula(conj2LitArray, conj2boolArray, "conjunction2");
		DdnnfFormula conj1 = new DdnnfFormula(conj1LitArray, conj1boolArray, "conjunction1");
		this.mainDdnnfRule = conj1;
		
		// d-DNNF logic clauses.
		boolean[] boolArray = new boolean[] {true, true};
		
		DdnnfLogicRule[] disjLits45 = new DdnnfLogicRule[] {conj5, conj4};
		DdnnfClause clause45 = new DdnnfClause(disjLits45, boolArray, "clause45");
		DdnnfLogicRule[] disjLits3V = new DdnnfLogicRule[] {conj3, clause45};
		DdnnfClause clause3V = new DdnnfClause(disjLits3V, boolArray, "clause3V");
		DdnnfLogicRule[] disjLits2V = new DdnnfLogicRule[] {conj2, clause3V};
		DdnnfClause clause2V = new DdnnfClause(disjLits2V, boolArray, "clause2V");
		DdnnfLogicRule[] disjLits1V = new DdnnfLogicRule[] {conj1, clause2V};
		this.ddnnfFormulae = new DdnnfClause(disjLits1V, boolArray, this.cnfFormula, "total_ddnnf_clause");
		
		
		// Create the leaf Graphs
		DdnnfGraph leaf5 = new DdnnfGraph(conj5);
		DdnnfGraph leaf4 = new DdnnfGraph(conj4);
		DdnnfGraph leaf3 = new DdnnfGraph(conj3);
		DdnnfGraph leaf2 = new DdnnfGraph(conj2);
		DdnnfGraph leaf1 = new DdnnfGraph(conj1);
		
		DdnnfGraph graph45 = new DdnnfGraph(clause45, leaf4, leaf5);
		DdnnfGraph graph3V = new DdnnfGraph(clause3V, leaf3, graph45);
		DdnnfGraph graph2V = new DdnnfGraph(clause2V, leaf2, graph3V);
		this.ddnnfGraph = new DdnnfGraph(this.ddnnfFormulae, leaf1, graph2V); 
		
	}
	
	/**
	 * 
	 * @param datum1
	 * @param datum2
	 * @param comparison
	 * @return
	 */
	private void runBirthDateBeforeBaptisedRule(final String datum1, final String datum2, final String comparison) {
		
		// Create SimpleDate classes.
		SimpleDate date1 = new SimpleDate(datum1);
		SimpleDate date2 = new SimpleDate(datum2);
		
		// CNF rule.
		CnfDateLogic cnfBirthDate = new CnfDateLogic(date1, "BirthDate", true, false);
		CnfDateLogic cnfBaptisedDate = new CnfDateLogic(date2, "BaptisedDate", true, false);
		CnfDateComparer cnfDate21Comparer = new CnfDateComparer(date1, date2, "CompareTo", true, false);
		if(comparison == "after") { cnfDate21Comparer.setComparisonAfter(); }
		else if(comparison == "before") { cnfDate21Comparer.setComparisonBefore(); }
		// CNF ArrayBuilders.
		LogicLiteral[] clauseLitArray1 = new LogicLiteral[] {cnfBirthDate, cnfBaptisedDate};
		LogicLiteral[] clauseLitArray2 = new LogicLiteral[] {cnfBirthDate, cnfDate21Comparer};
		boolean[] clauseBoolArray = new boolean[] {false, true};
		// CNF clauses.
		Clause cnfClause1 = new Clause(clauseLitArray1, clauseBoolArray);
		Clause cnfClause2 = new Clause(clauseLitArray2, clauseBoolArray);
		Clause[] cnfClauses = new Clause[] {cnfClause1, cnfClause2};
		// CNF formula
		CnfFormula cnfFormula1 = new CnfFormula(cnfClauses);
		
		// d-DNNF logic literals.
		DdnnfDate birthDate = new DdnnfDate(date1, "BirthDate", true);
		DdnnfDate baptisedDate = new DdnnfDate(date2, "BaptisedDate", true);
		DdnnfDateComparer date21Comparer = new DdnnfDateComparer(date2, date1, "CompareTo");
		
		// Array builders
		//5
		DdnnfLogicRule[] conj5LitArray = new DdnnfLogicRule[] {birthDate, baptisedDate, date21Comparer};
		boolean[] conj5boolArray = new boolean[] {false, false, false};
		//4
		DdnnfLogicRule[] conj4LitArray = new DdnnfLogicRule[] {birthDate, baptisedDate, date21Comparer};
		boolean[] conj4boolArray = new boolean[] {false, false, true};
		//3
		DdnnfLogicRule[] conj3LitArray = new DdnnfLogicRule[] {birthDate, baptisedDate, date21Comparer};
		boolean[] conj3boolArray = new boolean[] {false, true, false};
		//2
		DdnnfLogicRule[] conj2LitArray = new DdnnfLogicRule[] {birthDate, baptisedDate, date21Comparer};
		boolean[] conj2boolArray = new boolean[] {false, true, true};
		//1
		DdnnfLogicRule[] conj1LitArray = new DdnnfLogicRule[] {birthDate, baptisedDate, date21Comparer};
		boolean[] conj1boolArray = new boolean[] {true, true, true};
		
		
		// d-DNNF logic formulae.
		DdnnfFormula conj5 = new DdnnfFormula(conj5LitArray, conj5boolArray, "conjunction5");
		DdnnfFormula conj4 = new DdnnfFormula(conj4LitArray, conj4boolArray, "conjunction4");
		DdnnfFormula conj3 = new DdnnfFormula(conj3LitArray, conj3boolArray, "conjunction3");
		DdnnfFormula conj2 = new DdnnfFormula(conj2LitArray, conj2boolArray, "conjunction2");
		DdnnfFormula conj1 = new DdnnfFormula(conj1LitArray, conj1boolArray, "conjunction1");
		this.mainDdnnfRule = conj1;
		
		// d-DNNF logic clauses.
		boolean[] boolArray = new boolean[] {true, true};
		
		DdnnfLogicRule[] disjLits45 = new DdnnfLogicRule[] {conj5, conj4};
		DdnnfClause clause45 = new DdnnfClause(disjLits45, boolArray, "clause45");
		DdnnfLogicRule[] disjLits3V = new DdnnfLogicRule[] {conj3, clause45};
		DdnnfClause clause3V = new DdnnfClause(disjLits3V, boolArray, "clause3V");
		DdnnfLogicRule[] disjLits2V = new DdnnfLogicRule[] {conj2, clause3V};
		DdnnfClause clause2V = new DdnnfClause(disjLits2V, boolArray, "clause2V");
		DdnnfLogicRule[] disjLits1V = new DdnnfLogicRule[] {conj1, clause2V};
		this.ddnnfFormulae = new DdnnfClause(disjLits1V, boolArray, this.cnfFormula, "total_ddnnf_clause");
		
		
		// Create the leaf Graphs
		DdnnfGraph leaf5 = new DdnnfGraph(conj5);
		DdnnfGraph leaf4 = new DdnnfGraph(conj4);
		DdnnfGraph leaf3 = new DdnnfGraph(conj3);
		DdnnfGraph leaf2 = new DdnnfGraph(conj2);
		DdnnfGraph leaf1 = new DdnnfGraph(conj1);
		
		DdnnfGraph graph45 = new DdnnfGraph(clause45, leaf4, leaf5);
		DdnnfGraph graph3V = new DdnnfGraph(clause3V, leaf3, graph45);
		DdnnfGraph graph2V = new DdnnfGraph(clause2V, leaf2, graph3V);
		this.ddnnfGraph = new DdnnfGraph(this.ddnnfFormulae, leaf1, graph2V); 
	}
	
	// A d-DNNF formula stating the following in NF:
	// IF( BirthDate(Date(01-01-1800)) )THEN( DeathDate(Date(03-03-1880) )AND( Date(01-01-1800) >= Date(03-03-1880)) )
	public void runBaptisedBeforeDeathRule(final String datum1, final String datum2, final String comparison) {

		// Create SimpleDate classes.
		SimpleDate date1 = new SimpleDate(datum1);
		SimpleDate date2 = new SimpleDate(datum2);
		
		// CNF rule.
		CnfDateLogic cnfBaptisedDate = new CnfDateLogic(date1, "BaptisedDate", true, false);
		CnfDateLogic cnfDeathDate = new CnfDateLogic(date2, "DeathDate", true, false);
		CnfDateComparer cnfDate21Comparer = new CnfDateComparer(date1, date2, "CompareTo", true, false);
		if(comparison == "after") { cnfDate21Comparer.setComparisonAfter(); }
		else if(comparison == "before") { cnfDate21Comparer.setComparisonBefore(); }
		// CNF ArrayBuilders.
		LogicLiteral[] clauseLitArray1 = new LogicLiteral[] {cnfBaptisedDate, cnfDeathDate};
		LogicLiteral[] clauseLitArray2 = new LogicLiteral[] {cnfBaptisedDate, cnfDate21Comparer};
		boolean[] clauseBoolArray = new boolean[] {false, true};
		// CNF clauses.
		Clause cnfClause1 = new Clause(clauseLitArray1, clauseBoolArray);
		Clause cnfClause2 = new Clause(clauseLitArray2, clauseBoolArray);
		Clause[] cnfClauses = new Clause[] {cnfClause1, cnfClause2};
		// CNF formula
		CnfFormula cnfFormula1 = new CnfFormula(cnfClauses);
		
		// d-DNNF logic literals.
		DdnnfDate baptisedDate = new DdnnfDate(date1, "BaptisedDate", true);
		DdnnfDate deathDate = new DdnnfDate(date2, "DeathDate", true);
		DdnnfDateComparer date21Comparer = new DdnnfDateComparer(date2, date1, "CompareTo");
		
		// Array builders
		//5
		DdnnfLogicRule[] conj5LitArray = new DdnnfLogicRule[] {baptisedDate, deathDate, date21Comparer};
		boolean[] conj5boolArray = new boolean[] {false, false, false};
		//4
		DdnnfLogicRule[] conj4LitArray = new DdnnfLogicRule[] {baptisedDate, deathDate, date21Comparer};
		boolean[] conj4boolArray = new boolean[] {false, false, true};
		//3
		DdnnfLogicRule[] conj3LitArray = new DdnnfLogicRule[] {baptisedDate, deathDate, date21Comparer};
		boolean[] conj3boolArray = new boolean[] {false, true, false};
		//2
		DdnnfLogicRule[] conj2LitArray = new DdnnfLogicRule[] {baptisedDate, deathDate, date21Comparer};
		boolean[] conj2boolArray = new boolean[] {false, true, true};
		//1
		DdnnfLogicRule[] conj1LitArray = new DdnnfLogicRule[] {baptisedDate, deathDate, date21Comparer};
		boolean[] conj1boolArray = new boolean[] {true, true, true};
		
		
		// d-DNNF logic formulae.
		DdnnfFormula conj5 = new DdnnfFormula(conj5LitArray, conj5boolArray, "conjunction5");
		DdnnfFormula conj4 = new DdnnfFormula(conj4LitArray, conj4boolArray, "conjunction4");
		DdnnfFormula conj3 = new DdnnfFormula(conj3LitArray, conj3boolArray, "conjunction3");
		DdnnfFormula conj2 = new DdnnfFormula(conj2LitArray, conj2boolArray, "conjunction2");
		DdnnfFormula conj1 = new DdnnfFormula(conj1LitArray, conj1boolArray, "conjunction1");
		this.mainDdnnfRule = conj1;
		
		// d-DNNF logic clauses.
		boolean[] boolArray = new boolean[] {true, true};
		
		DdnnfLogicRule[] disjLits45 = new DdnnfLogicRule[] {conj5, conj4};
		DdnnfClause clause45 = new DdnnfClause(disjLits45, boolArray, "clause45");
		DdnnfLogicRule[] disjLits3V = new DdnnfLogicRule[] {conj3, clause45};
		DdnnfClause clause3V = new DdnnfClause(disjLits3V, boolArray, "clause3V");
		DdnnfLogicRule[] disjLits2V = new DdnnfLogicRule[] {conj2, clause3V};
		DdnnfClause clause2V = new DdnnfClause(disjLits2V, boolArray, "clause2V");
		DdnnfLogicRule[] disjLits1V = new DdnnfLogicRule[] {conj1, clause2V};
		this.ddnnfFormulae = new DdnnfClause(disjLits1V, boolArray, this.cnfFormula, "total_ddnnf_clause");
		
		
		// Create the leaf Graphs
		DdnnfGraph leaf5 = new DdnnfGraph(conj5);
		DdnnfGraph leaf4 = new DdnnfGraph(conj4);
		DdnnfGraph leaf3 = new DdnnfGraph(conj3);
		DdnnfGraph leaf2 = new DdnnfGraph(conj2);
		DdnnfGraph leaf1 = new DdnnfGraph(conj1);
		
		DdnnfGraph graph45 = new DdnnfGraph(clause45, leaf4, leaf5);
		DdnnfGraph graph3V = new DdnnfGraph(clause3V, leaf3, graph45);
		DdnnfGraph graph2V = new DdnnfGraph(clause2V, leaf2, graph3V);
		this.ddnnfGraph = new DdnnfGraph(this.ddnnfFormulae, leaf1, graph2V); 
	}

}
