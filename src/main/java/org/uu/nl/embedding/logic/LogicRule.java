/**
 * 
 */
package org.uu.nl.embedding.logic;

/**
 * Interface class for logic rules
 * 
 * @author Euan Westenbroek
 * @version 1.0
 * @since 12-05-2020
 */
public interface LogicRule {

	abstract boolean getValue();
	abstract String getName();
	abstract String toString();
}
