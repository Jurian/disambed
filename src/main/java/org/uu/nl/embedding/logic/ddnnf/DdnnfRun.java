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
		//runBirthDateBeforeDeathDateRule("01-01-1800", "03-03-1880");
		//printCnf("01-01-1800", "01-03-1880", new boolean[] {false, true, true});
		//printCnf("03-01-1880", "01-03-1800",  new boolean[] {true, true, true});
		//printCnf("01-01-1800", "01-03-1801", new boolean[] {true, true, true});
		
		runBirthDateBeforeDeathDateRule("01-01-1800", "01-03-1880", "after");
		runBirthDateBeforeBaptisedRule("01-01-1800", "06-01-1800", "after");
		runBaptisedBeforeDeathRule("06-01-1800", "01-03-1880", "after");
		System.out.println();
		System.out.println("False rules:");
		runBirthDateBeforeDeathDateRule("01-03-1880", "01-01-1800", "after");
		runBirthDateBeforeBaptisedRule("06-01-1800", "01-01-1800", "after");
		runBaptisedBeforeDeathRule("01-03-1880", "06-01-1800", "after");
		System.out.println();
		System.out.println("Reversed true rules:");
		runBirthDateBeforeDeathDateRule("01-03-1880", "01-01-1800", "before");
		runBirthDateBeforeBaptisedRule("06-01-1800", "01-01-1800", "before");
		runBaptisedBeforeDeathRule("01-03-1880", "06-01-1800", "before");
		
	}
	
	// A d-DNNF formula stating the following in NF:
	// IF( BirthDate(Date(01-01-1800)) )THEN( DeathDate(Date(03-03-1880) )AND( Date(01-01-1800) >= Date(03-03-1880)) )
	public static void runBirthDateBeforeDeathDateRule(final String datum1, final String datum2, final String comparison) {
		
		// Create SimpleDate classes.
		SimpleDate date1 = new SimpleDate(datum1);
		SimpleDate date2 = new SimpleDate(datum2);
		
		// CNF rule.
		CnfDateLogic cnfBirthDate = new CnfDateLogic(date1, "BirthDate", true, false);
		CnfDateLogic cnfDeathDate = new CnfDateLogic(date2, "DeathDate", true, false);
		CnfDateComparer cnfDate21Comparer = new CnfDateComparer(date1, date2, "date1_compareto_date2", true, false);
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
		CnfFormula cnfFormula1 = new CnfFormula(cnfClauses);
		
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
		DdnnfClause totalClause = new DdnnfClause(disjLits1V, boolArray, cnfFormula1, "total_ddnnf_clause");
		
		
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
		
		System.out.println("CNF Formula:");
		System.out.println(cnfFormula1.toString());
		System.out.println(cnfFormula1.toValueString());
		System.out.println(String.valueOf(cnfFormula1.isSatisfied()));
		System.out.println();
		/*
		System.out.println("d-DNNF graph:");
		System.out.print(totalGraph.printString());
		totalGraph.printMap();
		totalGraph.printMapWithValues();*/

		System.out.print(cnfFormula1.trueAssignmentsToString());
		System.out.print(cnfFormula1.falseAssignmentsToString());
	}
	
	// A d-DNNF formula stating the following in NF:
	// IF( BirthDate(Date(01-01-1800)) )THEN( DeathDate(Date(03-03-1880) )AND( Date(01-01-1800) >= Date(03-03-1880)) )
	public static void runBirthDateBeforeBaptisedRule(final String datum1, final String datum2, final String comparison) {
		
		// Create SimpleDate classes.
		SimpleDate date1 = new SimpleDate(datum1);
		SimpleDate date2 = new SimpleDate(datum2);
		
		// CNF rule.
		CnfDateLogic cnfBirthDate = new CnfDateLogic(date1, "BirthDate", true, false);
		CnfDateLogic cnfBaptisedDate = new CnfDateLogic(date2, "BaptisedDate", true, false);
		CnfDateComparer cnfDate21Comparer = new CnfDateComparer(date1, date2, "date1_compareto_date2", true, false);
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
		DdnnfDateComparer date21Comparer = new DdnnfDateComparer(date2, date1);
		
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
		
		// d-DNNF logic clauses.
		boolean[] boolArray = new boolean[] {true, true};
		
		DdnnfLogicRule[] disjLits45 = new DdnnfLogicRule[] {conj5, conj4};
		DdnnfClause clause45 = new DdnnfClause(disjLits45, boolArray, "clause45");
		DdnnfLogicRule[] disjLits3V = new DdnnfLogicRule[] {conj3, clause45};
		DdnnfClause clause3V = new DdnnfClause(disjLits3V, boolArray, "clause3V");
		DdnnfLogicRule[] disjLits2V = new DdnnfLogicRule[] {conj2, clause3V};
		DdnnfClause clause2V = new DdnnfClause(disjLits2V, boolArray, "clause2V");
		DdnnfLogicRule[] disjLits1V = new DdnnfLogicRule[] {conj1, clause2V};
		DdnnfClause totalClause = new DdnnfClause(disjLits1V, boolArray, cnfFormula1, "total_ddnnf_clause");
		
		
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
		
		System.out.println("CNF Formula:");
		System.out.println(cnfFormula1.toString());
		System.out.println(cnfFormula1.toValueString());
		System.out.println(String.valueOf(cnfFormula1.isSatisfied()));
		System.out.println();
		
		/*
		System.out.println("d-DNNF graph:");
		System.out.print(totalGraph.printString());
		totalGraph.printMap();
		totalGraph.printMapWithValues();*/

		System.out.print(cnfFormula1.trueAssignmentsToString());
		System.out.print(cnfFormula1.falseAssignmentsToString());
	}
	
	// A d-DNNF formula stating the following in NF:
	// IF( BirthDate(Date(01-01-1800)) )THEN( DeathDate(Date(03-03-1880) )AND( Date(01-01-1800) >= Date(03-03-1880)) )
	public static void runBaptisedBeforeDeathRule(final String datum1, final String datum2, final String comparison) {

		// Create SimpleDate classes.
		SimpleDate date1 = new SimpleDate(datum1);
		SimpleDate date2 = new SimpleDate(datum2);
		
		// CNF rule.
		CnfDateLogic cnfBaptisedDate = new CnfDateLogic(date1, "BaptisedDate", true, false);
		CnfDateLogic cnfDeathDate = new CnfDateLogic(date2, "DeathDate", true, false);
		CnfDateComparer cnfDate21Comparer = new CnfDateComparer(date1, date2, "date1_compareto_date2", true, false);
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
		DdnnfDateComparer date21Comparer = new DdnnfDateComparer(date2, date1);
		
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
		
		// d-DNNF logic clauses.
		boolean[] boolArray = new boolean[] {true, true};
		
		DdnnfLogicRule[] disjLits45 = new DdnnfLogicRule[] {conj5, conj4};
		DdnnfClause clause45 = new DdnnfClause(disjLits45, boolArray, "clause45");
		DdnnfLogicRule[] disjLits3V = new DdnnfLogicRule[] {conj3, clause45};
		DdnnfClause clause3V = new DdnnfClause(disjLits3V, boolArray, "clause3V");
		DdnnfLogicRule[] disjLits2V = new DdnnfLogicRule[] {conj2, clause3V};
		DdnnfClause clause2V = new DdnnfClause(disjLits2V, boolArray, "clause2V");
		DdnnfLogicRule[] disjLits1V = new DdnnfLogicRule[] {conj1, clause2V};
		DdnnfClause totalClause = new DdnnfClause(disjLits1V, boolArray, cnfFormula1, "total_ddnnf_clause");
		
		
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
		
		System.out.println("CNF Formula:");
		System.out.println(cnfFormula1.toString());
		System.out.println(cnfFormula1.toValueString());
		System.out.println(String.valueOf(cnfFormula1.isSatisfied()));
		System.out.println();
		/*
		System.out.println("d-DNNF graph:");
		System.out.print(totalGraph.printString());
		totalGraph.printMap();
		totalGraph.printMapWithValues();*/
		
		System.out.print(cnfFormula1.trueAssignmentsToString());
		System.out.print(cnfFormula1.falseAssignmentsToString());
	}
	
	public static void printCnf(final String datum1, final String datum2, final boolean[] assignments) {
		
		// Create SimpleDate classes.
		SimpleDate date1 = new SimpleDate(datum1);
		SimpleDate date2 = new SimpleDate(datum2);
		
		// CNF rule.
		CnfDateLogic cnfBirthDate = new CnfDateLogic(date1, "BirthDate", assignments[0], false);
		CnfDateLogic cnfDeathDate = new CnfDateLogic(date2, "DeathDate", assignments[1], false);
		CnfDateComparer cnfDate21Comparer = new CnfDateComparer(date1, date2, "date1_compareto_date2", assignments[2], false);
		cnfDate21Comparer.setComparisonAfter();

		/*
		System.out.println();
		System.out.println("Difference in days: " + cnfDate21Comparer.differenceWith("dd"));
		System.out.println("Difference in months: " + cnfDate21Comparer.differenceWith("mm"));
		System.out.println("Difference in years: " + cnfDate21Comparer.differenceWith("yyyy"));
		System.out.println();
		*/
		// CNF ArrayBuilders.
		LogicLiteral[] clauseLitArray1 = new LogicLiteral[] {cnfBirthDate, cnfDeathDate};
		LogicLiteral[] clauseLitArray2 = new LogicLiteral[] {cnfBirthDate, cnfDate21Comparer};
		boolean[] clauseBoolArray = new boolean[] {false, true};
		// CNF clauses.
		Clause cnfClause1 = new Clause(clauseLitArray1, clauseBoolArray);
		Clause cnfClause2 = new Clause(clauseLitArray2, clauseBoolArray);
		Clause[] cnfClauses = new Clause[] {cnfClause1, cnfClause2};
		// CNF formula
		CnfFormula cnfFormula = new CnfFormula(cnfClauses);
		cnfFormula.setTrue();

		System.out.println(cnfFormula.toString());
		System.out.println(cnfFormula.toValueString());
		System.out.println(String.valueOf(cnfFormula.isSatisfied()));
		

		cnfFormula.setFalse();
		System.out.println(cnfFormula.toString());
		System.out.println(cnfFormula.toValueString());
		System.out.println(String.valueOf(cnfFormula.isSatisfied()));
		
		System.out.print(cnfFormula.trueAssignmentsToString());
		System.out.print(cnfFormula.falseAssignmentsToString());
	}
}
