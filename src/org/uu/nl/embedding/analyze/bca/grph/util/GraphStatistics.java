package org.uu.nl.embedding.analyze.bca.grph.util;

import java.util.Arrays;

import org.uu.nl.embedding.convert.util.NodeInfo;

import grph.Grph;
import grph.properties.NumericalProperty;
import grph.properties.Property;


/**
 * This class will collect all the necessary information about the graph
 * in an efficient manner by only traversing it once.
 * @author Jurian Baas
 */
public class GraphStatistics {
	
	/**
	 * A mapping between nodes and a unique index, used in the bookmark 
	 * coloring algorithm to do lookups in constant time.
	 */
	public final int[] keys;
	/**
	 * The type (URI, blank, literal, predicate) for each node in the graph
	 */
	public final byte[] types;
	/**
	 * All the unique nodes in the graph from which 
	 * we can start the bookmark coloring algorithm
	 */
	public final int[] jobs;
	/**
	 * The string representations of all the nodes in the graph
	 */
	public final String[] dict;
	
	private int predicateNodeCount;
	private int uriNodeCount;
	private int blankNodeCount;
	private int literalNodeCount;
	private int totalNodeCount;
	
	public GraphStatistics(Grph graph) {
		
		Property labelProperties = graph.getVertexLabelProperty();
		NumericalProperty typeProperties = graph.getVertexColorProperty();
		
		this.keys = graph.getVertices().toIntArray();
		this.types = new byte[keys.length];
		this.dict = new String[keys.length];
		
		int[] jobs = new int[keys.length];
		
		int job_i = 0;
		for(int i = 0; i < keys.length; i++) {
			int node = keys[i];
			types[i] = (byte) typeProperties.getValue(node);
			dict[i] = labelProperties.getValueAsString(node);
			
			if(types[i] == NodeInfo.BLANK) blankNodeCount++;
			else if(types[i] == NodeInfo.URI) {
				uriNodeCount++;
				jobs[job_i++] = node;
			}
			else if(types[i] == NodeInfo.LITERAL) literalNodeCount++;
		}
		
		this.jobs = Arrays.copyOf(jobs, job_i);
		this.totalNodeCount = uriNodeCount + blankNodeCount + literalNodeCount;
	}

	public int getTotalNodeCount() {
		return totalNodeCount;
	}
	
	public int getPredicateNodeCount() {
		return predicateNodeCount;
	}

	public void setPredicateNodeCount(int predicateNodeCount) {
		this.predicateNodeCount = predicateNodeCount;
	}

	public int getUriNodeCount() {
		return uriNodeCount;
	}

	public void setUriNodeCount(int uriNodeCount) {
		this.uriNodeCount = uriNodeCount;
	}

	public int getBlankNodeCount() {
		return blankNodeCount;
	}

	public void setBlankNodeCount(int blankNodeCount) {
		this.blankNodeCount = blankNodeCount;
	}

	public int getLiteralNodeCount() {
		return literalNodeCount;
	}

	public void setLiteralNodeCount(int literalNodeCount) {
		this.literalNodeCount = literalNodeCount;
	}

	
}