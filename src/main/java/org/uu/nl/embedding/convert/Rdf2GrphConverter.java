package org.uu.nl.embedding.convert;

import grph.properties.Property;
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
import org.uu.nl.embedding.util.similarity.PreComputed;

import java.util.*;
import java.util.concurrent.*;


/**
 * Converts an RDF graph to the Grph form
 *
 * @author Jurian Baas
 */
public class Rdf2GrphConverter implements Converter<Model, InMemoryRdfGraph> {

	private static final Logger logger = Logger.getLogger(Rdf2GrphConverter.class);
	private final Configuration config;

	public Rdf2GrphConverter(Configuration config) {
		this.config = config;
	}

	@Override
	public InMemoryRdfGraph convert(Model model) {

		logger.info("Converting RDF data into fast graph representation");

		final InMemoryRdfGraph g = new InMemoryRdfGraph();

		final Map<String, Float> predicateWeights = config.getWeights();
		final Map<String, SimilarityGroup> sourceGroups = new HashMap<>();
		final Map<String, SimilarityGroup> targetGroups = new HashMap<>();

		if(config.getSimilarity() != null)
			config.getSimilarity().forEach(
					sim -> {
						boolean upperTriangle = sim.getSourcePredicate().equals(sim.getTargetPredicate());
						SimilarityGroup group = new SimilarityGroup(sim.toFunction(), sim.getThreshold(), upperTriangle);
						sourceGroups.put(sim.getSourcePredicate(), group);
						targetGroups.put(sim.getTargetPredicate(), group);
					}
			);

		final boolean doSimilarityMatching = !sourceGroups.isEmpty();
		final boolean doPredicateWeighting = predicateWeights != null && !predicateWeights.isEmpty();

		final Map<Node, Map<Node, Integer>> predicateLiteralMap = new HashMap<>();
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
				s_i = addVertex(g, p, s, vertexMap, predicateLiteralMap);
				o_i = addVertex(g, p, o, vertexMap, predicateLiteralMap);
				addEdge(g, p, s_i, o_i, edgeTypes, predicateWeights.get(predicateString));

				if(doSimilarityMatching) {
					// Some similarity metrics require pre-processing
					final SimilarityGroup source = sourceGroups.get(predicateString);
					if(source != null) {
						source.addToSource(o_i);
						if(source.needsPrecompute()) ((PreComputed)source.similarity).preCompute(o.toString(false));
					}

					final SimilarityGroup target = targetGroups.get(predicateString);
					if(target != null) {
						target.addToTarget(o_i);
						if(target.needsPrecompute()) ((PreComputed)target.similarity).preCompute(o.toString(false));
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

		for(Map.Entry<String, SimilarityGroup> entry : sourceGroups.entrySet()) {

			final ExecutorService es = Executors.newWorkStealingPool(config.getThreads());
			final CompletionService<CompareResult> completionService = new ExecutorCompletionService<>(es);

			final SimilarityGroup group = entry.getValue();

			final int sourceSize = group.source.size();
			final int targetSize = group.target.size();

			final StringSimilarity metric = group.similarity;

			final int[] sourceNodes = group.source.stream().mapToInt(i -> i).toArray();
			final int[] targetNodes = group.target.stream().mapToInt(i -> i).toArray();

			logger.info("Processing similarities for predicate " + entry.getKey());

			for (int i = 0; i < sourceSize; i++) {
				completionService.submit(new CompareJob(group.upperTriangle, i, sourceNodes, targetNodes, group.threshold, metric, g.getVertexLabelProperty()));
			}

			int received = 0;
			int edgesAdded = 0;
			try(ProgressBar pb = Configuration.progressBar("Comparing", sourceSize, "literals")) {
				pb.setExtraMessage(Integer.toString(edgesAdded));
				while (received < sourceSize) {
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
	 * Add a vertex to the graph if not present, the vertices are given consecutive IDs.
	 * Node type and label information are also added
	 */
	private int addVertex(InMemoryRdfGraph g, Node p, Node n, Map<Node, Integer> vertexMap, Map<Node, Map<Node, Integer>> predicateLiteralMap) {

		Integer key = vertexMap.get(n);
		if(!n.isLiteral() && key != null) return key; // Non-literal node is already present

		final int i = g.getNextVertexAvailable();

		if(n.isLiteral()) {
			// Literals are merged per predicate
			final Map<Node, Integer> nodes = predicateLiteralMap.computeIfAbsent(p, k -> new HashMap<>());

			key = nodes.get(n);
			if(key != null) {
				// Predicate-literal pair already present
				return key;
			} else {
				// First time we see this predicate-literal pair
				nodes.put(n, i);
			}
		} else {
			// First time we see this non-literal node
			vertexMap.put(n, i);
		}
		// Add vertex to graph with given id
		g.addVertex(i);
		// Add vertex type info (URI, blank, literal)
		g.getVertexTypeProperty().setValue(i, NodeInfo.type2index(n));
		// Add vertex label information (used for the embedding dictionary)
		g.getVertexLabelProperty().setValue(i, n.toString(false));
		return i;
    }

	/**
	 * 	Edges are considered unique for a subject-object pair
	 * 	However there are only a few types of edges (relationships)
	 * 	So we also store a reference to a unique edge-type id
	 */
    private void addEdge(InMemoryRdfGraph g, Node p, int s_i, int o_i, Map<Node, Byte> edgeTypes, float weight) {
    	// Create a unique id for this predicate given the subject-object pair
		final int p_i = g.addDirectedSimpleEdge(s_i, o_i);
		g.getEdgeLabelProperty().setValue(p_i, p.toString(false));
		// If we have not encountered this edge-type before, give it a unique id
		edgeTypes.putIfAbsent(p, (byte) (edgeTypes.size() + 1));
		// Store the edge-type value for this new edge
		g.getEdgeTypeProperty().setValue(p_i, edgeTypes.get(p));
		g.getEdgeWeightProperty().setValue(p_i, weight);
	}

	/**
	 * Associates a set of nodes with a similarity metric
	 */
	private static class SimilarityGroup {

		public final StringSimilarity similarity;
		public final Set<Integer> source, target;
		public final double threshold;
		public final boolean upperTriangle;

		private SimilarityGroup(StringSimilarity similarity, double threshold, boolean upperTriangle) {
			this.similarity = similarity;
			this.source = new HashSet<>();
			this.target = new HashSet<>();
			this.threshold = threshold;
			this.upperTriangle = upperTriangle;
		}

		public boolean needsPrecompute(){
			return this.similarity instanceof PreComputed;
		}

		public void addToSource(int i){
			this.source.add(i);
		}

		public void addToTarget(int i){
			this.target.add(i);
		}
	}

	/**
	 * The result of comparing a node with other nodes
	 */
	private static class CompareResult {

		public final int vert;
		public final List<Integer> otherVerts;
		public final List<Float> similarities;

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
		private final int startIndex;
		private final int[] source, target;
		private final double threshold;
		private final StringSimilarity metric;
		private final Property vertexLabels;


		public CompareJob(boolean upperTriangle, int index, int[] source, int[] target, double threshold, StringSimilarity metric, Property vertexLabels) {
			this.index = index;
			this.source = source;
			this.target = target;
			this.threshold = threshold;
			this.metric = metric;
			this.vertexLabels = vertexLabels;
			this.startIndex = upperTriangle ? index + 1 : 0;
		}

		@Override
		public CompareResult call() {

			final int vert = source[index];
			final CompareResult result = new CompareResult(vert);

			for (int j = startIndex; j < target.length; j++) {

				final int otherVert = target[j];
				if(otherVert == vert) continue;

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
