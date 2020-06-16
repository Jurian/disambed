package org.uu.nl.embedding.logic.ddnnf;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.uu.nl.embedding.logic.LogicRule;
import org.uu.nl.embedding.logic.cnf.Clause;
import org.uu.nl.embedding.logic.cnf.CnfLogicRule;
import org.uu.nl.embedding.logic.cnf.LogicLiteral;

public interface DdnnfLogicRule extends LogicRule {

	abstract CnfLogicRule getSourceRule();
	abstract ArrayList<DdnnfLiteral> getLiterals();
	abstract Set<DdnnfLiteral> getPositiveLiterals();
	abstract Set<DdnnfLiteral> getNegativeLiterals();
	abstract List<DdnnfLogicRule> getRules();
	abstract String toValueString();
	abstract void negate();

}
