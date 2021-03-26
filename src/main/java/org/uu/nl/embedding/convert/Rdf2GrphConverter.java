package org.uu.nl.embedding.convert;

import grph.algo.distance.PageRank;
import info.debatty.java.stringsimilarity.interfaces.StringSimilarity;
import me.tongfei.progressbar.ProgressBar;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.log4j.Logger;
import org.uu.nl.embedding.compare.CompareGroup;
import org.uu.nl.embedding.compare.CompareJob;
import org.uu.nl.embedding.compare.CompareResult;
import org.uu.nl.embedding.convert.util.NodeInfo;
import org.uu.nl.embedding.util.InMemoryRdfGraph;
import org.uu.nl.embedding.util.config.Configuration;
import org.uu.nl.embedding.util.config.InvalidConfigException;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

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

	private static final String QUERY_SIMILARITY_FORMAT =
			"SELECT ?entity ?value WHERE {?entity a %s. ?entity %s ?value.}";

	private static final String QUERY_OUTPUT_FORMAT =
			"SELECT ?entity WHERE {?entity a %s.}";

	@Override
	public InMemoryRdfGraph convert(Model model) {

		logger.info("Converting RDF data into fast graph representation");

		try {

			final InMemoryRdfGraph g = new InMemoryRdfGraph();

			final Map<String, Float> prefixedWeights = config.getPredicates().getWeights();
			final Set<String> prefixedFilter = config.getPredicates().getFilter();
			final Map<String, Float> expandedWeights = new HashMap<>();
			final Set<String> expandedFilter = new HashSet<>();

			for(Map.Entry<String, Float> entry : prefixedWeights.entrySet()) {
				expandedWeights.put(model.expandPrefix(entry.getKey()), entry.getValue());
			}

			for(String prefixed : prefixedFilter) {
				expandedFilter.add(model.expandPrefix(prefixed));
			}

			config.getPredicates().setWeights(expandedWeights);
			config.getPredicates().setFilter(expandedFilter);

			final boolean doSimilarityMatching = config.getSimilarity() != null;
			final boolean useManualWeights = config.getPredicates().usingManualWeights();
			final boolean usePageRankWeights = config.getPredicates().usingPageRankWeights();
			final boolean useNoWeights = config.getPredicates().usingNoWeights();

			CompareGroup[] compareGroups = null;

			if(doSimilarityMatching) {
				compareGroups = new CompareGroup[config.getSimilarity().size()];
				for(int i = 0; i < compareGroups.length; i++){

					final Configuration.SimilarityGroup configGroup = config.getSimilarity().get(i);
					final CompareGroup compareGroup = new CompareGroup(configGroup);

					final String sourceQueryString =
							String.format(
									QUERY_SIMILARITY_FORMAT,
									configGroup.getSourceType(),
									configGroup.getSourcePredicate());

					final String targetQueryString =
							String.format(
									QUERY_SIMILARITY_FORMAT,
									configGroup.getTargetType(),
									configGroup.getTargetPredicate());

					final ParameterizedSparqlString sourceQuery = new ParameterizedSparqlString();
					sourceQuery.setCommandText(sourceQueryString);
					sourceQuery.setNsPrefixes(model.getNsPrefixMap());

					final ParameterizedSparqlString targetQuery = new ParameterizedSparqlString();
					targetQuery.setCommandText(targetQueryString);
					targetQuery.setNsPrefixes(model.getNsPrefixMap());

					try (QueryExecution execSource = QueryExecutionFactory.create(sourceQuery.asQuery(), model)) {
						final ResultSet sourceEntities = execSource.execSelect();
						while(sourceEntities.hasNext()) {
							final QuerySolution solution = sourceEntities.nextSolution();
							compareGroup.addSourceEntity(solution.get("value").asNode());
						}
					}

					try (QueryExecution execTarget = QueryExecutionFactory.create(targetQuery.asQuery(), model)) {
						final ResultSet targetEntities = execTarget.execSelect();
						while(targetEntities.hasNext()) {
							final QuerySolution solution = targetEntities.nextSolution();
							compareGroup.addTargetEntity(solution.get("value").asNode());
						}
					}
					compareGroups[i] = compareGroup;
				}
			}

			final Set<Node> outputNodes = new HashSet<>();

			for(String type : config.getOutput().getType()){

				final ParameterizedSparqlString query = new ParameterizedSparqlString();
				query.setCommandText(String.format(QUERY_OUTPUT_FORMAT, type));
				query.setNsPrefixes(model.getNsPrefixMap());
				try (QueryExecution exec = QueryExecutionFactory.create(query.asQuery(), model)) {
					final ResultSet entities = exec.execSelect();
					while(entities.hasNext()) {
						final QuerySolution solution = entities.next();
						outputNodes.add(solution.get("entity").asNode());
					}
				}
			}

			final Map<Node, Map<Node, Integer>> predicateLiteralMap = new HashMap<>();
			final Map<Node, Integer> vertexMap = new HashMap<>();
			final Map<Node, Integer> edgeTypes = new HashMap<>();

			long skippedTriples = 0;

			final ExtendedIterator<Triple> triples = model.getGraph().find();
			final PredicateStatistics statistics = new PredicateStatistics();

			Triple t;
			Node s, p, o;
			int s_i, o_i, p_i;

			try(ProgressBar pb = Configuration.progressBar("Converting", model.size(), "triples")) {
				while (triples.hasNext()) {

					t = triples.next();
					s = t.getSubject();
					p = t.getPredicate();
					o = t.getObject();

					final String predicateString = model.expandPrefix(p.toString(false));

					// Ignore unweighted predicates
					if(!expandedFilter.contains(predicateString)) {
						// Adjust the total number of triples we are considering
						// Maybe we can do pb.step() here instead to make it less confusing
						pb.step();
						skippedTriples++;
						continue;
					}

					// Only create a new ID if the vertex is not yet present
					s_i = addVertex(g, p, s, vertexMap, predicateLiteralMap, model);
					o_i = addVertex(g, p, o, vertexMap, predicateLiteralMap, model);

					p_i = addEdge(g, p, s_i, o_i, edgeTypes, model);

					if(!(useNoWeights || usePageRankWeights)) {
						if(useManualWeights) {
							statistics.addManualWeight(p_i, expandedWeights.getOrDefault(predicateString, 1f));
						} else {
							statistics.addPredicateInstance(p_i);
						}
					}


					if(doSimilarityMatching && o.isLiteral()) {
						for (CompareGroup compareGroup : compareGroups) compareGroup.process(o, o_i);
					}

					// Mark a node for output
					if(s.isURI() && outputNodes.contains(s)) {
						config.getOutput().addNodeIndex(s_i);
					} else if(o.isURI() && outputNodes.contains(o)) {
						config.getOutput().addNodeIndex(o_i);
					}

					pb.step();
				}
			} finally {
				triples.close();
				logger.info("Skipped " + skippedTriples +
						" unweighted triples (" + String.format("%.2f", (skippedTriples/(double)model.size()*100)) + " %)");
			}



			Configuration.PredicateWeights predicateWeights = config.getPredicates();

			switch (predicateWeights.getTypeEnum()) {
				case NONE: {
					for(int v : g.getVertices()) {
						for(int e : g.getOutEdges(v)) {
							g.getEdgeWeightProperty().setValue(e, 1);
						}
					}
				}
				break;
				case MANUAL: {
					statistics.manual().forEach((key, weight) ->
							g.getEdgeWeightProperty().setValue(key, weight));
				}
				break;
				case PAGERANK: {
					logger.info("Computing pagerank");
					PageRank pr = g.getPageRanking(new Random());
					pr.compute();
					for(int v : g.getVertices()) {
						final float rank = (float) pr.getRank(v);
						for(int e : g.getOutOnlyEdges(v)) {
							g.getEdgeWeightProperty().setValue(e, rank);
						}
						// Special case for bidirectional edges between literals
						for(int e : g.getInOutOnlyEdges(v)) {
							if(g.getEdgeWeightProperty().isSetted(e)) {
								float old = g.getEdgeWeightProperty().getValue(e);
								g.getEdgeWeightProperty().setValue(e,(old + rank) / 2);
							} else {
								g.getEdgeWeightProperty().setValue(e, rank);
							}
						}
					}
				}
				break;
				case FREQUENCY: {
					statistics.frequency().forEach((key, weight) ->
							g.getEdgeWeightProperty().setValue(key, weight));
				}
				break;
				case INVERSE_FREQUENCY: {
					statistics.inverseFrequency().forEach((key, weight) ->
							g.getEdgeWeightProperty().setValue(key, weight));
				}
				break;
			}

			if(config.getOutput().getNodeIndex().size() == 0) {
				throw new InvalidConfigException("No nodes of output type(s): " + String.join(", ", config.getOutput().getType()));
			}

			if(doSimilarityMatching) {
				for (CompareGroup compareGroup : compareGroups) compareLiterals(g, compareGroup);
			} else {
				logger.info("Partial matching is disabled, no edges between similar literals are added");
			}
			return g;

		} finally {
			model.close();
		}
	}

	private void compareLiterals(InMemoryRdfGraph g, CompareGroup compareGroup) {
		final ExecutorService es = Executors.newWorkStealingPool(config.getThreads());
		final CompletionService<CompareResult> completionService = new ExecutorCompletionService<>(es);

		final int sourceSize = compareGroup.sourceIndexes.size();
		final int targetSize = compareGroup.targetIndexes.size();

		final StringSimilarity metric = compareGroup.similarity;

		final int[] sourceNodes = compareGroup.sourceIndexes.stream().sorted().mapToInt(i -> i).toArray();
		final int[] targetNodes = compareGroup.targetIndexes.stream().sorted().mapToInt(i -> i).toArray();

		logger.info("Processing similarities between:");
		logger.info(compareGroup.sourceURI + "* " +
					compareGroup.sourcePredicate + " ("+sourceSize+") and" );
		logger.info(compareGroup.targetURI + "* " +
					compareGroup.targetPredicate + " ("+ targetSize +")");

		boolean inGroupComparison = compareGroup.sourceURI.equals(compareGroup.targetURI) && compareGroup.sourcePredicate.equals(compareGroup.targetPredicate);

		for (int i = 0; i < sourceSize; i++) {
			completionService.submit(new CompareJob(sourceNodes[i], targetNodes, compareGroup.threshold, metric, g.getVertexLabelProperty(), inGroupComparison));
		}

		int received = 0;
		int edgesAdded = 0;
		try(ProgressBar pb = Configuration.progressBar("Comparing", sourceSize, "literals")) {
			pb.setExtraMessage(Integer.toString(edgesAdded));
			while (received < sourceSize) {
				try {

					final CompareResult result = completionService.take().get();
					final int vert = result.vert;
					for (int i = 0; i < result.otherVerts.size(); i++) {

							final int otherVert = result.otherVerts.get(i);

							if(vert == otherVert) continue;

							final float similarity = result.similarities.get(i);

							final int e = g.addUndirectedSimpleEdge(vert, otherVert);
							// Similarities between vertices are stored as edge width property, which is limited to
							// a few bits so we store the similarity as a byte (a number between 0 and 100)
							g.getEdgeWeightProperty().setValue(e, similarity);
							g.getEdgeTypeProperty().setValue(e, 0);

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

	/**
	 * Add a vertex to the graph if not present, the vertices are given consecutive IDs.
	 * Node type and label information are also added
	 */
	private int addVertex(InMemoryRdfGraph g, Node p, Node n, Map<Node, Integer> vertexMap, Map<Node, Map<Node, Integer>> predicateLiteralMap, PrefixMapping prefixMapping) {

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
				g.getLiteralPredicateProperty().setValue(i, p.toString( prefixMapping,false));
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
		g.getVertexLabelProperty().setValue(i, n.toString(prefixMapping,false));
		return i;
    }

	/**
	 * 	Edges are considered unique for a subject-object pair
	 * 	However there are only a few types of edges (relationships)
	 * 	So we also store a reference to a unique edge-type id
	 */
    int addEdge(InMemoryRdfGraph g, Node p, int s_i, int o_i, Map<Node, Integer> edgeTypes, PrefixMapping prefixMapping) {
    	// Create a unique id for this predicate given the subject-object pair
		final int p_i = g.addUndirectedSimpleEdge(s_i, o_i);
		g.getEdgeLabelProperty().setValue(p_i, p.toString(prefixMapping,false) );
		// If we have not encountered this edge-type before, give it a unique id
		edgeTypes.putIfAbsent(p, (edgeTypes.size() + 1));
		// Store the edge-type value for this new edge
		g.getEdgeTypeProperty().setValue(p_i, edgeTypes.get(p));

		return p_i;
		//g.getEdgeWeightProperty().setValue(p_i, weight);
	}

	static class PredicateStatistics {

    	final Map<Integer, Float> weights = new HashMap<>();
    	float tripleCount = 0;

    	void addManualWeight(int p_i, float weight) {
    		weights.putIfAbsent(p_i, weight);
		}

    	void addPredicateInstance(int p_i) {
    		weights.compute(p_i, (k, v) -> (v == null) ? 1 : v+1);
    		tripleCount++;
		}

		Map<Integer, Float> manual() {
			return weights;
		}

		Map<Integer, Float> unweighted() {
			return weights.entrySet().stream().collect(Collectors.toMap(
					Map.Entry::getKey, entry -> 1f));
		}

		Map<Integer, Float> frequency() {
			return weights.entrySet().stream().collect(Collectors.toMap(
					Map.Entry::getKey, entry -> entry.getValue() / tripleCount));
		}

		Map<Integer, Float> inverseFrequency() {
			return weights.entrySet().stream().collect(Collectors.toMap(
					Map.Entry::getKey, entry -> 1 / entry.getValue()));
		}
	}
}
