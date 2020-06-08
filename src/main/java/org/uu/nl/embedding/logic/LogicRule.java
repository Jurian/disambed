/**
 * 
 */
package org.uu.nl.embedding.logic;

import java.util.Set;
import org.uu.nl.embedding.lensr.DdnnfGraph;

/**
 * Interface class for logic rules
 * 
 * @author Euan Westenbroek
 * @version 1.3
 * @since 12-05-2020
 */
public interface LogicRule {

	abstract boolean getAssignment();
	abstract void setAssignment(boolean value);
	abstract void setFalse();
	abstract void setTrue();
	abstract boolean isFalse();
	abstract boolean isTrue();
	

	abstract String getName();
	abstract String toString();
	abstract String getCnfName();
	abstract String getDdnnfName();
	
	abstract LogicRule getPrecedent();
	abstract LogicRule getAntecedent();
	
	abstract LogicRule getNfRule();
	abstract LogicRule getCnfRule();
	abstract LogicRule getDdnnfRule();

	abstract DdnnfGraph getDdnnfGraph();
}
