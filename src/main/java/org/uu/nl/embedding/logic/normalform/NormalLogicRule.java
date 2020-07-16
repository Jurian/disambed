package org.uu.nl.embedding.logic.normalform;

import org.uu.nl.embedding.logic.LogicRule;

public interface NormalLogicRule extends LogicRule {

	abstract LogicRule[] getAllTerms();
}
