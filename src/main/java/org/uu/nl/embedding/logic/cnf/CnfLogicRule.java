package org.uu.nl.embedding.logic.cnf;

import java.util.List;
import java.util.Set;

import org.uu.nl.embedding.lensr.DdnnfGraph;
import org.uu.nl.embedding.logic.LogicRule;
import org.uu.nl.embedding.logic.ddnnf.DdnnfLogicRule;

public interface CnfLogicRule extends LogicRule {

	abstract Set<LogicLiteral> getLiterals();
	abstract Set<LogicLiteral> getPositiveLiterals();
	abstract Set<LogicLiteral> getNegativeLiterals();
	abstract List<Clause> getClauses();
	abstract String toValueString();
	abstract boolean isSatisfied();
	
	abstract void setDdnnfGraph(DdnnfGraph graph);
	abstract DdnnfGraph getDdnnfGraph();
	abstract void setDdnnfRule(DdnnfLogicRule rule);
	abstract DdnnfLogicRule getDdnnfRule();
}
