package org.uu.nl.embedding.logic.ddnnf;

import org.uu.nl.embedding.lensr.DdnnfGraph;
import org.uu.nl.embedding.logic.cnf.Clause;
import org.uu.nl.embedding.logic.cnf.CnfDateComparer;
import org.uu.nl.embedding.logic.cnf.CnfDateLogic;
import org.uu.nl.embedding.logic.cnf.CnfFormula;
import org.uu.nl.embedding.logic.cnf.LogicLiteral;
import org.uu.nl.embedding.logic.util.SimpleDate;

public class DdnnfRun {
	

	public static void main(String[] args) {
		runBirthDateBeforeDeathDateRule();
	}
	
	// A d-DNNF formula stating the following in NF:
	// IF( BirthDate(Date(01-01-1800)) )THEN( DeathDate(Date(03-03-1880) )AND( Date(01-01-1800) >= Date(03-03-1880)) )
	public static void runBirthDateBeforeDeathDateRule() {
		
		// Create SimpleDate classes.
		SimpleDate date1 = new SimpleDate("01-01-1800");
		SimpleDate date2 = new SimpleDate("03-03-1880");
		
		// CNF rule.
		CnfDateLogic cnfBirthDate = new CnfDateLogic(date1, "BirthDate", true, false);
		CnfDateLogic cnfDeathDate = new CnfDateLogic(date2, "DeathDate", true, false);
		CnfDateComparer cnfDate21Comparer = new CnfDateComparer(date1, date2, "date1_compareto_date2", true, false);
		// CNF ArrayBuilders.
		LogicLiteral[] clauseLitArray = new LogicLiteral[] {cnfBirthDate, cnfDeathDate};
		boolean[] clauseBoolArray = new boolean[] {false, true};
		// CNF clauses.
		Clause cnfClause = new Clause(clauseLitArray, clauseBoolArray);
		Clause literalClause = new Clause(cnfDate21Comparer, true);
		Clause[] cnfClauses = new Clause[] {cnfClause, literalClause};
		// CNF formula
		CnfFormula cnfFormula = new CnfFormula(cnfClauses);
		
		// d-DNNF logic literals.
		DdnnfDate birthDate = new DdnnfDate(date1, "BirthDate", true);
		DdnnfDate deathDate = new DdnnfDate(date2, "DeathDate", true);
		DdnnfDateComparer date21Comparer = new DdnnfDateComparer(date2, date1);
		
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
		
		// d-DNNF logic clauses.
		boolean[] boolArray = new boolean[] {true, true};
		
		DdnnfLogicRule[] disjLits45 = new DdnnfLogicRule[] {conj5, conj4};
		DdnnfClause clause45 = new DdnnfClause(disjLits45, boolArray, "clause45");
		DdnnfLogicRule[] disjLits3V = new DdnnfLogicRule[] {conj3, clause45};
		DdnnfClause clause3V = new DdnnfClause(disjLits3V, boolArray, "clause3V");
		DdnnfLogicRule[] disjLits2V = new DdnnfLogicRule[] {conj2, clause3V};
		DdnnfClause clause2V = new DdnnfClause(disjLits2V, boolArray, "clause2V");
		DdnnfLogicRule[] disjLits1V = new DdnnfLogicRule[] {conj1, clause2V};
		DdnnfClause totalClause = new DdnnfClause(disjLits1V, boolArray, cnfFormula, "total_ddnnf_clause");
		
		
		// Create the leaf Graphs
		DdnnfGraph leaf5 = new DdnnfGraph(conj5);
		DdnnfGraph leaf4 = new DdnnfGraph(conj4);
		DdnnfGraph leaf3 = new DdnnfGraph(conj3);
		DdnnfGraph leaf2 = new DdnnfGraph(conj2);
		DdnnfGraph leaf1 = new DdnnfGraph(conj1);
		
		DdnnfGraph graph45 = new DdnnfGraph(clause45, leaf4, leaf5);
		DdnnfGraph graph3V = new DdnnfGraph(clause3V, leaf3, graph45);
		DdnnfGraph graph2V = new DdnnfGraph(clause2V, leaf2, graph3V);
		DdnnfGraph totalGraph = new DdnnfGraph(totalClause, leaf1, graph2V);
		
		System.out.print(totalGraph.printString());
		totalGraph.printMap();
		totalGraph.printMapWithValues();
	}
	
	
}
