package org.uu.nl.embedding.logic.cnf;

import java.util.List;
import java.util.Set;

import org.uu.nl.embedding.logic.LogicRule;

public interface CnfLogicRule extends LogicRule {

	abstract Set<LogicLiteral> getLiterals();
	abstract Set<LogicLiteral> getPositiveLiterals();
	abstract Set<LogicLiteral> getNegativeLiterals();
	abstract List<Clause> getClauses();
	abstract String toValueString();
	abstract boolean isSatisfied();
}
