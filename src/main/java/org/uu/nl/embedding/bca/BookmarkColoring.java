package org.uu.nl.embedding.bca;

import me.tongfei.progressbar.ProgressBar;

import org.apache.commons.lang3.ArrayUtils;
import org.uu.nl.embedding.bca.jobs.DirectedWeighted;
import org.uu.nl.embedding.bca.jobs.UndirectedWeighted;
import org.uu.nl.embedding.bca.util.BCV;
import org.uu.nl.embedding.bca.util.PaintedNode;
import org.uu.nl.embedding.convert.util.InEdgeNeighborhoodAlgorithm;
import org.uu.nl.embedding.convert.util.NodeInfo;
import org.uu.nl.embedding.convert.util.OutEdgeNeighborhoodAlgorithm;
import org.uu.nl.embedding.logic.DateCompareLogic;
import org.uu.nl.embedding.logic.ExactSameDateLogic;
import org.uu.nl.embedding.logic.util.SimpleDate;
import org.uu.nl.embedding.util.CoOccurrenceMatrix;
import org.uu.nl.embedding.util.InMemoryRdfGraph;
import org.uu.nl.embedding.util.config.Configuration;
import org.uu.nl.embedding.util.rnd.Permutation;

import grph.properties.NumericalProperty;
import grph.properties.Property;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.*;

/**
 * @author Jurian Baas
 */
public class BookmarkColoring implements CoOccurrenceMatrix {

	private final ArrayList<Integer> coOccurrenceIdx_I;
	private final ArrayList<Integer> coOccurrenceIdx_J;
	private final ArrayList<Float> coOccurrenceValues;
	private ArrayList<Integer>  awareOccurrenceIdx_I;
	private ArrayList<Integer>  awareOccurrenceIdx_J;
	private ArrayList<Float>  awareOccurrenceValues;
	private Map<String, Double> bcvMaxVals;
	private final int vocabSize;
	private int coOccurrenceCount;
	private int awareOccurrenceCount;
	private Permutation permutation;
	private Permutation awarePermutation;
	private final InMemoryRdfGraph graph;
	private final Configuration graphConfig;

	private final int[][] inVertex;
	private final int[][] outVertex;
	private final int[][] inEdge;
	private final int[][] outEdge;

	public BookmarkColoring(final InMemoryRdfGraph graph, final Configuration config) {

		final double alpha = config.getBca().getAlpha();
		final double epsilon = config.getBca().getEpsilon();
		final int[] jobs = graph.getVertices().toIntArray();

		this.graph = graph;
		this.vocabSize = jobs.length;
		
		// Initialization standard co-occurrence matrix
		this.coOccurrenceIdx_I = new ArrayList<>(vocabSize);
		this.coOccurrenceIdx_J = new ArrayList<>(vocabSize);
		this.coOccurrenceValues = new ArrayList<>(vocabSize);
		// Initialization context aware co-occurrence matrix
		this.awareOccurrenceIdx_I = new ArrayList<>(vocabSize);
		this.awareOccurrenceIdx_J = new ArrayList<>(vocabSize);
		this.awareOccurrenceValues = new ArrayList<>(vocabSize);
		
		this.bcvMaxVals = new HashMap<String, Double>();

		final int numThreads = config.getThreads();
		this.graphConfig = config;

		final ExecutorService es = Executors.newWorkStealingPool(numThreads);

		this.inVertex = graph.getInNeighborhoods();
		this.outVertex = graph.getOutNeighborhoods();
		this.inEdge = new InEdgeNeighborhoodAlgorithm(config).compute(graph);
		this.outEdge = new OutEdgeNeighborhoodAlgorithm(config).compute(graph);

		try(ProgressBar pb = Configuration.progressBar("BCA", jobs.length, "nodes")) {

			CompletionService<BCV> completionService = new ExecutorCompletionService<>(es);
			// Choose a graph neighborhood algorithm
			for(int bookmark : jobs) {

				if(config.getBca().isDirected()) {
					completionService.submit(new DirectedWeighted(
							graph, bookmark,
							alpha, epsilon,
							this.inVertex, this.outVertex, this.inEdge, this.outEdge));
				} else {
					completionService.submit(new UndirectedWeighted(
							graph, bookmark,
							alpha, epsilon,
							this.inVertex, this.outVertex, this.inEdge, this.outEdge));
				}
			}
			
			//now retrieve the futures after computation (auto wait for it)
			int received = 0;

			while(received < jobs.length) {

				try {
					final BCV bcv = completionService.take().get();
					
					ExactSameDateLogic xSameDateLogic = new ExactSameDateLogic();
					final BCV awareBcv = addDateAwareWindow(bcv, xSameDateLogic, 0, graph);

					switch (config.getBca().getNormalizeEnum()) {
						case UNITY:
							bcv.toUnity();
							awareBcv.toUnity();
							break;
						case COUNTS:
							bcv.toCounts();
							awareBcv.toCounts();
							break;
						default:
						case NONE:
							break;
					}
					
					/*
					 * invoegen ContextAwareBCV van Euan
					 * addDateAwareWindow();
					 */
					
					//System.out.println(bcv);

					// It is possible to use this maximum value in GloVe, although in the
					// literature they set this value to 100 and leave it at that
					setMax(bcv.max());
					setMax("exactDate", awareBcv.max());
					
					// Create co-occurrence matrix for standard bcv
					for (Entry<Integer, Float> bcr : bcv.entrySet()) {
						coOccurrenceIdx_I.add(bcv.getRootNode());
						coOccurrenceIdx_J.add(bcr.getKey());
						coOccurrenceValues.add(bcr.getValue());
					}
					coOccurrenceCount += bcv.size();

					// Create co-occurrence matrix for context aware bcv
					for (Entry<Integer, Float> bcr : awareBcv.entrySet()) {
						awareOccurrenceIdx_I.add(bcv.getRootNode());
						awareOccurrenceIdx_J.add(bcr.getKey());
						awareOccurrenceValues.add(bcr.getValue());
					}
					awareOccurrenceCount += awareBcv.size();

				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				} finally {
					received ++;
					pb.step();
				}
			}

		} finally {
			es.shutdown();
		}

		permutation = new Permutation(coOccurrenceCount);
		awarePermutation = new Permutation(awareOccurrenceCount);
	}

	/**
	 * 
	 * 
	 * @author Euan Westenbroek
	 * @version 1.0
	 * @since 25-05-2020
	 */
	protected BCV addDateAwareWindow(final BCV bcv, final DateCompareLogic dateRule, final int daysDifference, final InMemoryRdfGraph graph) {
		
		final int root = bcv.getRootNode();
		final BCV awareBcv = new BCV(root);
	
		//nodeTree.put(root, new PaintedNode(root, 1)); // nodig om bcv opnieuw te maken als neighbouring node wordt weggehaald

		int[] dateNeighbors = new int[] {};
		int[] dateEdges = new int[] {};
		
		// Initialize for Out-edges
		int[][] allDateVertsOut = new int[bcv.size()][];
		int[][] allDateEdgesOut = new int[bcv.size()][];
		int[][] allProxyDateVertsOut = new int[bcv.size()][];
		int[][] allProxyDateEdgesOut = new int[bcv.size()][];
		// Initialize for In-edges
		int[][] allDateVertsIn = new int[bcv.size()][];
		int[][] allDateEdgesIn = new int[bcv.size()][];
		int[][] allProxyDateVertsIn = new int[bcv.size()][];
		int[][] allProxyDateEdgesIn= new int[bcv.size()][];
		// Combine arrays in array for clean looping code
		int[][][] allDateVerts = new int[][][] {allDateVertsIn, allDateVertsOut};
		int[][][] allDateEdges = new int[][][] {allDateEdgesIn, allDateEdgesOut};
		int[][][] allProxyDateVerts = new int[][][] {allProxyDateVertsIn, allProxyDateVertsOut};
		int[][][] allProxyDateEdges= new int[][][] {allProxyDateEdgesIn, allProxyDateEdgesOut};
    	List<Map<Integer, int[]>> removedMaps = new ArrayList<>();
	
		final NumericalProperty nodeTypes = graph.getVertexTypeProperty();
		final NumericalProperty edgeWeights = graph.getEdgeWeightProperty();
		final NumericalProperty edgeTypes = graph.getEdgeTypeProperty(); // Added edges have type == 0
		final Property vertexLabels = graph.getVertexLabelProperty();
		final Property edgeLabels = graph.getEdgeLabelProperty();
		
	
		NodeInfo rootInfo = NodeInfo.fromByte((byte) nodeTypes.getValueAsInt(root));
		String rootLabel = vertexLabels.getValueAsString(root);
		
		// Keep track of the order in which the Date entries were put in
		int[] focusNodeOrder = new int[bcv.entrySet().size()];
		focusNodeOrder[0] = root;
		
		int[][] rootNeighbors = new int[][] {};
		int[][] rootEdges = new int[][] {};
		
		final int[][][] inOutVerts = new int[][][] {this.inVertex, this.outVertex};
		final int[][][] inOutEdges = new int[][][] {this.inEdge, this.outEdge};
		int[] rootProxyDateVerts;
		int[] rootProxyDateEdges;
		String neighborLabel;
		
		for(int direction = 0; direction < inOutVerts.length; direction++) {
			
			rootNeighbors[direction] = inOutVerts[direction][root];
			rootEdges[direction] = inOutEdges[direction][root];
			rootProxyDateVerts = new int[] {};
			rootProxyDateEdges = new int[] {};
			
			// Check for edges of root not in original graph
			for(int i = 0; i < rootEdges[direction].length; i++) {
				
				// Get vertex label to check if it is a date
			    neighborLabel = vertexLabels.getValueAsString(rootNeighbors[direction][i]);
		    	if(SimpleDate.isDateFormat(neighborLabel)) {
		    		// Add the found date vertex and edge to their respective arrays
					dateNeighbors = ArrayUtils.addAll(dateNeighbors, new int[] {rootNeighbors[direction][i]});
					dateEdges = ArrayUtils.addAll(dateEdges, new int[] {rootEdges[direction][i]});
		    		
					// edgeType == 0 means edge was not in original graph
		    		if(edgeTypes.getValueAsInt(rootEdges[direction][i]) == 0) { 
						// Expand the added vertex list with all vertices the proxy edges flow to
			    		// an Date Literal node
						rootProxyDateVerts = ArrayUtils.addAll(rootProxyDateVerts, new int[] {rootNeighbors[direction][i]});
						rootProxyDateEdges = ArrayUtils.addAll(rootProxyDateEdges, new int[] {rootEdges[direction][i]});
			    	}
				}
			}
			// Add the found proxyDate arrays to their respective arrays
			allDateVerts[direction][0] = dateNeighbors;
			allDateEdges[direction][0] = dateEdges;
			allProxyDateVerts[direction][0] = rootProxyDateVerts;
			allProxyDateEdges[direction][0] = rootProxyDateEdges;
		}
		
		
		int counter = 0;
		int focusNode;
		int[] neighborEdges;
		int[] neighborVerts;
		int[] neighborProxyDateVerts;
		int[] neighborProxyDateEdges;

		// Loop through BCV and check for context
		for(Map.Entry<Integer, Float> entry : bcv.entrySet()) { //START foreach-loop
			focusNode = entry.getKey();
			
			// Skip rest of loop if focusNode is root
			// counter increment is also skipped
			if(focusNode == root) {
				continue;
			}
	    	counter++;

		    NodeInfo neighborInfo = NodeInfo.fromByte((byte) nodeTypes.getValueAsInt(focusNode));
		    neighborLabel = vertexLabels.getValueAsString(focusNode);
				if(neighborInfo == NodeInfo.LITERAL) {
					// Skip this type of nodes
			    	continue;
					
			    } else if(neighborInfo == NodeInfo.URI) {
					for(int direction = 0; direction < inOutVerts.length; direction++) { // START for-loop
				    	// Initialize empty new int arrays
				    	dateNeighbors = new int[] {};
				    	dateEdges = new int[] {};
				    	neighborProxyDateVerts = new int[] {};
				    	neighborProxyDateEdges = new int[] {};
				    	// Declare focusNode's out-edges and out-nodes
				    	neighborVerts = inOutVerts[direction][focusNode];
				    	neighborEdges = inOutEdges[direction][focusNode];
	
						// Check for edges of focusNode not in original graph
				    	for(int i = 0; i < neighborEdges.length; i++) {
							// Get vertex label to check if it is a date
						    neighborLabel = vertexLabels.getValueAsString(neighborVerts[i]);
					    	if(SimpleDate.isDateFormat(neighborLabel)) {
					    		// Add the found date vertex and edge to their respective arrays
								dateNeighbors = ArrayUtils.addAll(dateNeighbors, new int[] {neighborVerts[i]});
								dateEdges = ArrayUtils.addAll(dateEdges, new int[] {neighborEdges[i]});
					    		
								// edgeType == 0 means edge was not in original graph
					    		if(edgeTypes.getValueAsInt(neighborEdges[i]) == 0) {
								
									// Expand the added vertex list with all vertices the proxy edges flow to
						    		// an Date Literal node
									neighborProxyDateVerts = ArrayUtils.addAll(neighborProxyDateVerts, new int[] {neighborVerts[i]});
									neighborProxyDateEdges = ArrayUtils.addAll(neighborProxyDateEdges, new int[] {neighborEdges[i]});
						    	}
				    		}
				    	}
						// Add the found proxyDate arrays to their respective arrays
						allDateVerts[direction][counter] = dateNeighbors;
						allDateEdges[direction][counter] = dateEdges;
						allProxyDateVerts[direction][counter] = neighborProxyDateVerts;
						allProxyDateEdges[direction][counter] = neighborProxyDateEdges;
					    
					} //END for-loop

					// Keep track of the order in which the Date entries were put in
			    	focusNodeOrder[counter] = focusNode;
			    	
			    } // END if-statement NodeInfo.URI
			    
				/* 
				 * else { NodeInfo.BLANK node }
				 * So skip this kind of nodes
				 */
		    
		} //END foreach-loop
    	
    	/* Now check if there is both a proxy date vertex and proxy date edge
    	 * of either the root or focusNode, that are part of the other vertex'
    	 * neighbors
    	 */
		removedMaps = createRemovedMaps(allProxyDateVerts,allProxyDateEdges,
				allDateVerts, allDateEdges, vertexLabels, edgeLabels,
				dateRule, daysDifference, focusNodeOrder, counter, inOutVerts.length);
    	
    	awareBcv = UndirectedWeighted.doWorkWithIgnore(graph, false, bcv, focusNodeOrder, removedMaps);
    	
		return awareBcv;
	}
	
	/**
	 * 
	 * 
	 * @author Euan Westenbroek
	 * @version 1.0
	 * @since 25-05-2020
	 * 
	 * @param allProxyDateVerts
	 * @param allProxyDateEdges
	 * @param allDateVerts
	 * @param allDateEdges
	 * @param vertexLabels
	 * @param edgeLabels
	 * @param dateRule
	 * @param daysDifference
	 * @param focusNodeOrder
	 * @param counter
	 * @param inOutSize
	 * @return removedMaps, a list of HashMap<Integer, int[]>
	 */
	private List<Map<Integer, int[]>> createRemovedMaps(final int[][][] allProxyDateVerts, final int[][][] allProxyDateEdges,
									final int[][][] allDateVerts, final int[][][] allDateEdges,
									final Property vertexLabels, final Property edgeLabels,
									final DateCompareLogic dateRule, final int daysDifference,
									final int[] focusNodeOrder, final int counter, final int inOutSize) {

		// List of HashMaps that will be returned
    	List<Map<Integer, int[]>> removedMaps = new ArrayList<>();
    	 // Map of nodes with its incoming neighboring nodes removed from original BCV
		Map<Integer, int[]> removedMapIn = new HashMap<Integer, int[]>();
		 // Map of nodes with its outgoing neighboring nodes removed from original BCV
		Map<Integer, int[]> removedMapOut = new HashMap<Integer, int[]>();
    	
    	// Initialize empty new integer arrays
    	int[] dateNeighbors;
    	int[] dateEdges;
		int[] neighborProxyDateVerts;
		int[] neighborProxyDateEdges;
		int[] removedVerts;

    	String proxyLabel, dateLabel;
		boolean validComparison;
		
    	for(int iProxy = 0; iProxy < counter; iProxy++) {
    		for(int jDate = 0; jDate < counter; jDate++) { //BEGIN outer double for-loop
    			
    			// Skip comparison with itself
    			if(iProxy == jDate) { continue; }
    			else {
					for(int direction = 0; direction < inOutSize; direction++) { // START for-loop
    				
	    				// Declare current arrays
	    				neighborProxyDateVerts = allProxyDateVerts[direction][iProxy]; 
	    				neighborProxyDateEdges = allProxyDateEdges[direction][iProxy];
	    				dateNeighbors = allDateVerts[direction][jDate];
	    				dateEdges = allDateEdges[direction][jDate];
	    				removedVerts = new int[] {};
	    				
	    				// Check first for proxyArray size to skip unnecessary steps
	    				for(int i = 0; i < neighborProxyDateVerts.length; i++) {
	    					for(int j = 0; j < dateNeighbors.length; j++) { //BEGIN inner double for-loop
	    						
	    						if(neighborProxyDateVerts[i] == dateNeighbors[j] &&
	    								neighborProxyDateEdges[i] == dateEdges[j]) {
	
	    						    proxyLabel = vertexLabels.getValueAsString(neighborProxyDateVerts[i]);
	    						    dateLabel = vertexLabels.getValueAsString(dateNeighbors[j]);
	    						    
	    						    // Get boolean value for comparing the two dates
	    							validComparison = dateRule.compareTwoDates(proxyLabel, dateLabel, daysDifference);
	    							if(!validComparison) { // validComparison == false
	    								// Add of which a neighbor is removed and the removed vertices themselves
	    								removedVerts = ArrayUtils.addAll(removedVerts, new int[] {neighborProxyDateVerts[i]});
	    								
	    								
	    							} else { // validComparison == true
	    								continue;
	    							}
	    						}
	    					}
	    				} //END inner double for-loop
	    				
		    			// Add array with removed neighbors to its focusNodes place
	    				if(direction == 0) { 
	    					removedMapIn.put(focusNodeOrder[iProxy], removedVerts);
	    				} else if(direction == 1)  {
	    					removedMapOut.put(focusNodeOrder[iProxy], removedVerts);
	    				} else { // direction > 1, throw Exception
	    		        	throw new IllegalArgumentException("More than 2 directions for neighboring vertices");
	    				}
	    			}
	    			
	    		} //END else-statement
    		}
    	} //END outer double for-loop
    	
    	// Put both the generated maps in the list and return the list
    	removedMaps.set(0, removedMapIn);
    	removedMaps.set(1, removedMapOut);
    	return removedMaps;
	}

	@Override
	public void shuffle() {
		permutation.shuffle();
	}
	
	public int cIdx_I(int i) {
		return this.coOccurrenceIdx_I.get(permutation.randomAccess(i));
	}
	
	public int cIdx_J(int j) {
		return this.coOccurrenceIdx_J.get(permutation.randomAccess(j));
	}
	
	public float cIdx_C(int i) {
		return this.coOccurrenceValues.get(permutation.randomAccess(i));
	}
	
	public byte getType(int index) {
		return (byte) this.graph.getVertexTypeProperty().getValueAsInt(index);
	}
	
	public int coOccurrenceCount() {
		return this.coOccurrenceCount;
	}
	
	@Override
	public int vocabSize() {
		return this.vocabSize;
	}
	
	@Override
	public double max() {
		return this.bcvMaxVals.get("stanard");
	}
	
	public double max(String bcvName) {
		return this.bcvMaxVals.get(bcvName);
	}
	
	@Override
	public String getKey(int index) {
		return this.graph.getVertexLabelProperty().getValueAsString(index);
	}
	

	private void setMax(double newMax) {
		this.bcvMaxVals.put("standard", Math.max(this.bcvMaxVals.get("standard"), newMax));
	}
	
	private void setMax(String bcvName, double newMax) {
		this.bcvMaxVals.put(bcvName, Math.max(this.bcvMaxVals.get(bcvName), newMax));
	}

}
