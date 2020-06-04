/**
 * 
 */
package org.uu.nl.embedding.logic;

import org.uu.nl.embedding.lensr.DdnnfGraph;

/**
 * Interface class for logic rules
 * 
 * @author Euan Westenbroek
 * @version 1.3
 * @since 12-05-2020
 */
public interface LogicRule {

	abstract boolean getValue();

	abstract String getName();
	abstract String toString();
	abstract String getNameCNF();
	abstract String getNameDdnnf();

	abstract LogicRule[] getAllTerms();
	abstract LogicRule getPrecedent();
	abstract LogicRule getAntecedent();
	abstract LogicRule getCnfRule();
	abstract LogicRule getDdnnfRule();

	abstract DdnnfGraph getDdnnfGraph();
}
