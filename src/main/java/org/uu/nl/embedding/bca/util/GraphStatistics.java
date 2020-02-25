package org.uu.nl.embedding.bca.util;

import grph.Grph;
import grph.properties.NumericalProperty;
import grph.properties.Property;
import org.uu.nl.embedding.convert.util.NodeInfo;
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

	public final double[] weights;

	private int uriNodeCount;
	private int blankNodeCount;
	private int literalNodeCount;
	public final int nrOfVertices;
	public final int nrOfEdges;
	public final int totalNodeCount;
	public final int nrOfEdgeTypes;
	
	public GraphStatistics(Grph graph, Configuration config) {
		
		final Property labelProperties = graph.getVertexLabelProperty();
		final NumericalProperty typeProperties = graph.getVertexColorProperty();
		final Property edgeLabels = graph.getEdgeLabelProperty();

		nrOfVertices = graph.getVertices().size();
		nrOfEdges = graph.getEdges().size();

		final Map<Integer, Integer> edgeTypes = new HashMap<>();
		for(int i = 0; i < nrOfEdges; i++) {
			if(graph.getEdgeColorProperty().isSetted(i)) {
				int type = graph.getEdgeColorProperty().getValueAsInt(i);
				edgeTypes.putIfAbsent(type, i);
			}
		}

		nrOfEdgeTypes = edgeTypes.size();

		int vocabSize = config.getBca().isPredicates() ? nrOfVertices + nrOfEdgeTypes : nrOfVertices;

		/*
		 * A mapping between nodes and a unique index, used in the bookmark
		 * coloring algorithm to do look-ups in constant time.
		 */
		final int[] keys = graph.getVertices().toIntArray();
		this.types = new byte[vocabSize];
		this.dict = new String[vocabSize];
		this.jobs = new int[nrOfVertices];
		
		//int job_i = 0;
		for(int i = 0; i < nrOfVertices; i++) {
			int node = keys[i];
			types[i] = (byte) typeProperties.getValue(node);
			dict[i] = labelProperties.getValueAsString(node);
			jobs[i] = node;

			if(types[i] == NodeInfo.BLANK.id) blankNodeCount++;
			else if(types[i] == NodeInfo.URI.id) {
				uriNodeCount++;
			}
			else if(types[i] == NodeInfo.LITERAL.id) literalNodeCount++;
		}

		weights = new double[nrOfEdgeTypes];
		for(Map.Entry<Integer, Integer> entry : edgeTypes.entrySet()) {

			int type = entry.getKey();
			int edge = entry.getValue();

			String label = edgeLabels.getValueAsString(edge);

			if(!config.usingWeights())
				weights[type] = 1;
			else if(config.usingWeights() && config.getWeights().containsKey(label))
				weights[type] = config.getWeights().get(label);

			if(config.getBca().isPredicates()) {
				dict[type + nrOfVertices] = label;
				types[type + nrOfVertices] = NodeInfo.PREDICATE.id;
			}
		}

		this.totalNodeCount = uriNodeCount + blankNodeCount + literalNodeCount;
	}
}