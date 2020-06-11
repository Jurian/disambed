package org.uu.nl.embedding.logic;

import java.util.List;
import java.util.Set;

public interface CnfLogicRule extends LogicRule {

	abstract Set<LogicLiteral> getLiterals();
	abstract Set<LogicLiteral> getPositiveLiterals();
	abstract Set<LogicLiteral> getNegativeLiterals();
	abstract List<Clause> getClauses();
	abstract String toValueString();
}
