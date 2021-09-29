package org.uu.nl.embedding.convert;

import grph.algo.distance.PageRank;
import me.tongfei.progressbar.ProgressBar;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.log4j.Logger;
import org.uu.nl.embedding.compare.CompareResources;
import org.uu.nl.embedding.convert.util.NodeInfo;
import org.uu.nl.embedding.util.InMemoryRdfGraph;
import org.uu.nl.embedding.util.config.Configuration;
import org.uu.nl.embedding.util.config.InvalidConfigException;
import org.uu.nl.embedding.util.config.PredicateWeights;
import org.uu.nl.embedding.util.config.SimilarityGroup;

import java.util.*;
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

	private static final String QUERY_OUTPUT_FORMAT =
			"SELECT ?entity WHERE {?entity a %s.}";

	@Override
	public InMemoryRdfGraph convert(Model model) {

		logger.info("Converting RDF data into fast graph representation");

		final InMemoryRdfGraph g = new InMemoryRdfGraph();

		final boolean doSimilarityMatching = config.getSimilarityGroups() != null;
		final boolean useManualWeights = config.getPredicates().usingManualWeights();
		final boolean usePageRankWeights = config.getPredicates().usingPageRankWeights();
		final boolean useNoWeights = config.getPredicates().usingNoWeights();

		if(config.getPrefixes() != null) {
			for(Map.Entry<String, String> prefix : config.getPrefixes().entrySet()) {
				model.setNsPrefix(prefix.getKey(), prefix.getValue());
			}
		}

		final Map<String, Float> prefixedWeights = config.getPredicates().getWeights();
		final Map<String, Float> expandedWeights = new HashMap<>();

		if(useManualWeights && prefixedWeights != null) {
			for(Map.Entry<String, Float> entry : prefixedWeights.entrySet()) {
				expandedWeights.put(model.expandPrefix(entry.getKey()), entry.getValue());
			}
			config.getPredicates().setWeights(expandedWeights);
		}

		final Set<String> prefixedFilter = config.getPredicates().getFilter();
		final Set<String> expandedFilter = new HashSet<>();

		if(prefixedFilter != null) {
			for(String prefixed : prefixedFilter) {
				expandedFilter.add(model.expandPrefix(prefixed));
			}
			config.getPredicates().setFilter(expandedFilter);
		}



		Map<String, Map<String, Float>> resourceSimilarityMap = new HashMap<>();
		if(doSimilarityMatching) {
			for(SimilarityGroup group : config.getSimilarityGroups()) {
				CompareResources compare = new CompareResources(model, group, config);
				for(Map.Entry<String, Map<String, Float>> entry : compare.doCompare().entrySet()) {
					resourceSimilarityMap.compute(entry.getKey(), (k, v) -> {
						if(v == null) {
							return entry.getValue();
						} else {
							v.putAll(entry.getValue());
							return v;
						}
					});
				}
			}

		} else {
			logger.info("Partial matching is disabled, no edges between similar literals are added");
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

		final Map<String, Integer> uriToIndexMap = new HashMap<>();

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

				if(doSimilarityMatching) {
					if(resourceSimilarityMap.containsKey(s.getURI())) {
						uriToIndexMap.putIfAbsent(s.getURI(), s_i);
					}
					if(resourceSimilarityMap.containsKey(o.getURI())) {
						uriToIndexMap.putIfAbsent(o.getURI(), o_i);
					}
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
			model.close();
		}

		if(doSimilarityMatching) {
			for(Map.Entry<String, Integer> entry : uriToIndexMap.entrySet()) {
				Map<String, Float> sim = resourceSimilarityMap.get(entry.getKey());
				final int vert = entry.getValue();
				for(Map.Entry<String, Float> simEntry : sim.entrySet()) {
					final int otherVert = uriToIndexMap.get(simEntry.getKey());
					p_i = g.addUndirectedSimpleEdge(vert, otherVert);
					g.getEdgeWeightProperty().setValue(p_i, simEntry.getValue());
				}
			}
		}


		PredicateWeights predicateWeights = config.getPredicates();

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

		return g;
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
