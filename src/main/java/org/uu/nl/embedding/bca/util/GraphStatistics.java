package org.uu.nl.embedding.bca.util;

import grph.Grph;
import grph.properties.NumericalProperty;
import grph.properties.Property;
import org.uu.nl.embedding.convert.util.NodeInfo;
import org.uu.nl.embedding.util.InMemoryRdfGraph;
import org.uu.nl.embedding.util.config.Configuration;

import java.util.HashMap;
import java.util.Map;


/**
 * This class will collect all the necessary information about the graph
 * in an efficient manner by only traversing it once.
 * @author Jurian Baas
 */
public class GraphStatistics {

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

	private int uriNodeCount;
	private int blankNodeCount;
	private int literalNodeCount;
	public final int nrOfVertices;
	public final int nrOfEdges;
	public final int totalNodeCount;

	public GraphStatistics(InMemoryRdfGraph graph, Configuration config) {
		
		final Property vertexLabels = graph.getVertexLabelProperty();
		final NumericalProperty typeProperties = graph.getVertexTypeProperty();

		nrOfVertices = graph.getVertices().size();
		nrOfEdges = graph.getEdges().size();

		/*
		 * A mapping between nodes and a unique index, used in the bookmark
		 * coloring algorithm to do look-ups in constant time.
		 */
		final int[] keys = graph.getVertices().toIntArray();
		this.types = new byte[nrOfVertices];
		this.dict = new String[nrOfVertices];
		this.jobs = new int[nrOfVertices];
		
		//int job_i = 0;
		for(int i = 0; i < nrOfVertices; i++) {
			int node = keys[i];
			types[i] = (byte) typeProperties.getValueAsInt(node);
			dict[i] = vertexLabels.getValueAsString(node);
			jobs[i] = node;

			if(types[i] == NodeInfo.BLANK.id) blankNodeCount++;
			else if(types[i] == NodeInfo.URI.id) {
				uriNodeCount++;
			}
			else if(types[i] == NodeInfo.LITERAL.id) literalNodeCount++;
		}

		this.totalNodeCount = uriNodeCount + blankNodeCount + literalNodeCount;
	}
}