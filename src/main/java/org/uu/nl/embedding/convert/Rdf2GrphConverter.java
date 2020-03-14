package org.uu.nl.embedding.convert;

import grph.properties.Property;
import info.debatty.java.stringsimilarity.JaroWinkler;
import info.debatty.java.stringsimilarity.interfaces.StringSimilarity;
import me.tongfei.progressbar.ProgressBar;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.log4j.Logger;
import org.uu.nl.embedding.convert.util.NodeInfo;
import org.uu.nl.embedding.util.InMemoryRdfGraph;
import org.uu.nl.embedding.util.config.Configuration;
import org.uu.nl.embedding.util.similarity.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Converts an RDF graph to the Grph form
 *
 * @author Jurian Baas
 */
public class Rdf2GrphConverter implements Converter<Model, InMemoryRdfGraph> {

	private static final Logger logger = Logger.getLogger(Rdf2GrphConverter.class);

	private final Map<String, Float> predicateWeights;
	private final Map<String, SimilarityGroup> similarityGroups;
	private final Configuration config;
	private final boolean doSimilarityMatching, doPredicateWeighting;

	public Rdf2GrphConverter(Configuration config) {

		this.config = config;
		this.predicateWeights = config.getWeights();
		this.similarityGroups = new HashMap<>();
		if(config.getSimilarity() != null)
		config.getSimilarity().forEach(
				sim -> similarityGroups.put(
						sim.getPredicate(), new SimilarityGroup(createSimilarityMetric(sim), sim.getThreshold())));
		this.doSimilarityMatching = !similarityGroups.isEmpty();
		this.doPredicateWeighting = predicateWeights != null && !predicateWeights.isEmpty();
	}

	@Override
	public InMemoryRdfGraph convert(Model model) {

		logger.info("Converting RDF data into fast graph representation");

		final InMemoryRdfGraph g = new InMemoryRdfGraph();

		final Map<Node, Integer> vertexMap = new HashMap<>();
		final Map<Node, Byte> edgeTypes = new HashMap<>();

		long skippedTriples = 0;
		int s_i, o_i;

		String predicateString;
		Node s, p, o;
		Triple t ;

		final ExtendedIterator<Triple> triples = model.getGraph().find();

		try(ProgressBar pb = Configuration.progressBar("Converting", model.size(), "triples")) {
			while (triples.hasNext()) {

				t = triples.next();
				s = t.getSubject();
				p = t.getPredicate();
				o = t.getObject();

				predicateString = p.toString();

				// Ignore unweighted predicates
				if(doPredicateWeighting && !predicateWeights.containsKey(predicateString)) {
					// Adjust the total number of triples we are considering
					// Maybe we can do pb.step() here instead to make it less confusing
					pb.step();
					skippedTriples++;
					continue;
				}

				// Only create a new ID if the vertex is not yet present
				s_i = addVertex(g, s, vertexMap);
				o_i = addVertex(g, o, vertexMap);
				addEdge(g, p, s_i, o_i, edgeTypes, predicateString);

				if(doSimilarityMatching) {
					// Some similarity metrics require pre-processing
					final SimilarityGroup sg = similarityGroups.get(predicateString);
					if(sg != null) {
						sg.addToGroup( o_i);
						if(sg.needsPrecompute()) ((PreComputed)sg.similarity).preCompute(o.toString(false));
					}
				}

				pb.step();
			}
		} finally {
			triples.close();
			logger.info("Skipped " + skippedTriples +
					" unweighted triples (" + String.format("%.2f", (skippedTriples/(double)model.size()*100)) + " %)");
		}


		if(!doSimilarityMatching) {
			logger.info("Partial matching is disabled, no edges between similar literals are added");
			return g;
		}

		for(Map.Entry<String, SimilarityGroup> entry : similarityGroups.entrySet()) {

			final ExecutorService es = Executors.newWorkStealingPool(config.getThreads());
			final CompletionService<CompareResult> completionService = new ExecutorCompletionService<>(es);
			final SimilarityGroup similarityGroup = entry.getValue();
			final int groupSize = similarityGroup.nodes.size();
			final StringSimilarity metric = similarityGroup.similarity;
			final int[] nodes = similarityGroup.nodes.stream().mapToInt(i -> i).toArray();

			logger.info("Processing similarities for predicate " + entry.getKey());

			for (int i = 0; i < groupSize; i++) {
				completionService.submit(new CompareJob(i, nodes, similarityGroup.threshold, metric, g.getVertexLabelProperty()));
			}

			int received = 0;
			int edgesAdded = 0;
			try(ProgressBar pb = Configuration.progressBar("Comparing", groupSize, "literals")) {
				pb.setExtraMessage(Integer.toString(edgesAdded));
				while (received < groupSize) {
					try {

						final CompareResult result = completionService.take().get();
						final int vert = result.vert;
						final int size = result.otherVerts.size();
						for (int i = 0; i < size; i++) {

							final int otherVert = result.otherVerts.get(i);
							final float similarity = result.similarities.get(i);
							final int e1 = g.addDirectedSimpleEdge(vert, otherVert);
							final int e2 = g.addDirectedSimpleEdge(otherVert, vert);

							// Similarities between vertices are stored as edge width property, which is limited to
							// a few bits so we store the similarity as a byte (a number between 0 and 100)
							g.getEdgeWeightProperty().setValue(e1, similarity);
							g.getEdgeWeightProperty().setValue(e2, similarity);
							g.getEdgeTypeProperty().setValue(e1, 0);
							g.getEdgeTypeProperty().setValue(e2, 0);
							edgesAdded++;
							pb.setExtraMessage(Integer.toString(edgesAdded));
						}

					} catch (InterruptedException | ExecutionException e) {
						e.printStackTrace();
					} finally {
						received++;
						pb.step();
					}
				}
			} finally {
				es.shutdown();
			}
			logger.info("Created links for " +edgesAdded+ " literal pairs");
		}

		return g;
	}

	/**
	 * Instantiate a similarity object from the configuration information
	 */
	private StringSimilarity createSimilarityMetric(Configuration.SimilarityGroup sim ) {

		switch (sim.getMethodEnum()) {
			case NUMERIC:
				return new Numeric(sim.getSmooth());
			case JACCARD:
				if(sim.getNgram() == 0) return new PreComputedJaccard(3);
				else return new PreComputedJaccard(sim.getNgram());
			case COSINE:
				if(sim.getNgram() == 0) return new PreComputedCosine(3);
				else return new PreComputedCosine(sim.getNgram());
			case JAROWINKLER:
				return new JaroWinkler();
			case TOKEN:
				return new PreComputedToken();
			case DATE:
				return new Date(sim.getFormat(), sim.getSmooth());
		}
		return null;
	}


	private static int type2index(Node node) {
		if(node.isURI()) return NodeInfo.URI.id;
		else if (node.isBlank()) return NodeInfo.BLANK.id;
		else if (node.isLiteral()) return NodeInfo.LITERAL.id;
		else throw new IllegalArgumentException("Node " + node + " is not of type URI, blank or literal");
	}


	/**
	 * Add a vertex to the graph if not present, the vertices are given consecutive IDs.
	 * Node type and label information are also added
	 */
	private int addVertex(InMemoryRdfGraph g, Node n, Map<Node, Integer> vertexMap) {
		final Integer key = vertexMap.get(n);
		if(key != null) return key; // Vertex already present

		final int i = vertexMap.size();
		vertexMap.put(n, i);
		g.addVertex(i);
		// Add vertex type info (URI, blank, literal)
		g.getVertexTypeProperty().setValue(i, type2index(n));
		// Add vertex label information (used for the embedding dictionary)
		g.getVertexLabelProperty().setValue(i, n.toString(false));
		return i;
    }

	/**
	 * 	Edges are considered unique for a subject-object pair
	 * 	However there are only a few types of edges (relationships)
	 * 	So we also store a reference to a unique edge-type id
	 */
    private void addEdge(InMemoryRdfGraph g, Node p, int s_i, int o_i, Map<Node, Byte> edgeTypes, String predicateString) {
    	// Create a unique id for this predicate given the subject-object pair
		final int p_i = g.addDirectedSimpleEdge(s_i, o_i);
		g.getEdgeLabelProperty().setValue(p_i, p.toString(false));
		// If we have not encountered this edge-type before, give it a unique id
		edgeTypes.putIfAbsent(p, (byte) (edgeTypes.size() + 1));
		// Store the edge-type value for this new edge
		g.getEdgeTypeProperty().setValue(p_i, edgeTypes.get(p));
		g.getEdgeWeightProperty().setValue(p_i, predicateWeights.get(predicateString));
	}

	/**
	 * Associates a set of nodes with a similarity metric
	 */
	private static class SimilarityGroup {

		public final StringSimilarity similarity;
		public final HashSet<Integer> nodes;
		public final double threshold;

		private SimilarityGroup(StringSimilarity similarity, double threshold) {
			this.similarity = similarity;
			this.nodes = new HashSet<>();
			this.threshold = threshold;
		}

		public boolean needsPrecompute(){
			return this.similarity instanceof PreComputed;
		}

		public void addToGroup(int i){
			this.nodes.add(i);
		}
	}

	/**
	 * The result of comparing a node with other nodes
	 */
	private static class CompareResult {

		public final int vert;
		public final ArrayList<Integer> otherVerts;
		public final ArrayList<Float> similarities;

		public CompareResult(int vert) {
			this.vert = vert;
			this.otherVerts = new ArrayList<>();
			this.similarities = new ArrayList<>();
		}
	}

	/**
	 * Do the similarity matching in parallel
	 */
	private static class CompareJob implements Callable<CompareResult> {

		private final int index;
		private final int[] nodes;
		private final double threshold;
		private final StringSimilarity metric;
		private final Property vertexLabels;

		public CompareJob(int index, int[] nodes, double threshold, StringSimilarity metric, Property vertexLabels) {
			this.index = index;
			this.nodes = nodes;
			this.threshold = threshold;
			this.metric = metric;
			this.vertexLabels = vertexLabels;
		}

		@Override
		public CompareResult call() {

			final int vert = nodes[index];
			final CompareResult result = new CompareResult(vert);

			for (int j = index + 1; j < nodes.length; j++) {

				final int otherVert = nodes[j];
				final String s1 = vertexLabels.getValueAsString(vert);
				final String s2 = vertexLabels.getValueAsString(otherVert);
				final double similarity = metric.similarity(s1, s2);

				if (similarity >= threshold) {
					result.otherVerts.add(otherVert);
					result.similarities.add((float) similarity);
				}

			}
			return result;
		}
	}
}
