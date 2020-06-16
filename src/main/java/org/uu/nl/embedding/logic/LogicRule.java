/**
 * 
 */
package org.uu.nl.embedding.logic;

import java.util.Set;
import org.uu.nl.embedding.lensr.DdnnfGraph;
import org.uu.nl.embedding.logic.cnf.CnfLogicRule;
import org.uu.nl.embedding.logic.normalform.NormalLogicRule;

/**
 * Interface class for logic rules
 * 
 * @author Euan Westenbroek
 * @version 1.3
 * @since 12-05-2020
 */
public interface LogicRule extends Comparable<LogicRule> {
	
	abstract boolean getAssignment();
	abstract void setAssignment(boolean value);
	abstract void setFalse();
	abstract void setTrue();
	abstract boolean isFalse();
	abstract boolean isTrue();
	

	abstract String getName();
	abstract String toString();
	abstract String toValueString();
	abstract String getCnfName();
	abstract String getDdnnfName();
	
	abstract LogicRule getPrecedent();
	abstract LogicRule getAntecedent();
	int compareTo(LogicRule other);
	
	//abstract NormalLogicRule getNfRule();
	//abstract CnfLogicRule getCnfRule();
	//abstract LogicRule getDdnnfRule();
}
