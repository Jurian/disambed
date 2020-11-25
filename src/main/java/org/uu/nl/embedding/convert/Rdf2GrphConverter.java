package org.uu.nl.embedding.convert;

import info.debatty.java.stringsimilarity.interfaces.StringSimilarity;
import me.tongfei.progressbar.ProgressBar;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.log4j.Logger;
import org.uu.nl.embedding.compare.CompareGroup;
import org.uu.nl.embedding.compare.CompareJob;
import org.uu.nl.embedding.compare.CompareResult;
import org.uu.nl.embedding.convert.util.NodeInfo;
import org.uu.nl.embedding.util.InMemoryRdfGraph;
import org.uu.nl.embedding.util.config.Configuration;

import java.util.HashMap;
import java.util.Map;
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

		final boolean doSimilarityMatching = config.getSimilarity() != null;
		final boolean doPredicateWeighting = predicateWeights != null && !predicateWeights.isEmpty();

		CompareGroup[] compareGroups = null;

		if(doSimilarityMatching) {
			compareGroups = new CompareGroup[config.getSimilarity().size()];
			for(int i = 0; i < compareGroups.length; i++){
				compareGroups[i] = new CompareGroup(config.getSimilarity().get(i));
			}
		}

		final Map<Node, Map<Node, Integer>> predicateLiteralMap = new HashMap<>();
		final Map<Node, Integer> vertexMap = new HashMap<>();
		final Map<Node, Integer> edgeTypes = new HashMap<>();

		long skippedTriples = 0;

		final ExtendedIterator<Triple> triples = model.getGraph().find();

		Triple t;
		Node s, p, o;
		int s_i, o_i;

		try(ProgressBar pb = Configuration.progressBar("Converting", model.size(), "triples")) {
			while (triples.hasNext()) {

				t = triples.next();
				s = t.getSubject();
				p = t.getPredicate();
				o = t.getObject();

				final String predicateString = p.toString();

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

				addEdge(g, p, s_i, o_i, edgeTypes, doPredicateWeighting ? predicateWeights.get(predicateString) : 1f);

				if(doSimilarityMatching) {
					for (CompareGroup compareGroup : compareGroups) compareGroup.process(s, o, o_i, predicateString);
				}

				pb.step();
			}
		} finally {
			triples.close();
			logger.info("Skipped " + skippedTriples +
					" unweighted triples (" + String.format("%.2f", (skippedTriples/(double)model.size()*100)) + " %)");
		}

		if(doSimilarityMatching) {
			for (CompareGroup compareGroup : compareGroups) compareLiterals(g, compareGroup);
		} else {
			logger.info("Partial matching is disabled, no edges between similar literals are added");
		}

		return g;
	}

	private void compareLiterals(InMemoryRdfGraph g, CompareGroup compareGroup) {
		final ExecutorService es = Executors.newWorkStealingPool(config.getThreads());
		final CompletionService<CompareResult> completionService = new ExecutorCompletionService<>(es);

		final int sourceSize = compareGroup.sourceNodes.size();
		final int targetSize = compareGroup.targetNodes.size();

		final StringSimilarity metric = compareGroup.similarity;

		final int[] sourceNodes = compareGroup.sourceNodes.stream().mapToInt(i -> i).toArray();
		final int[] targetNodes = compareGroup.targetNodes.stream().mapToInt(i -> i).toArray();

		logger.info("Processing similarities between:");
		logger.info(compareGroup.sourceURI + "* " +
					compareGroup.sourcePredicate + " ("+sourceSize+") and" );
		logger.info(compareGroup.targetURI + "* " +
					compareGroup.targetPredicate + " ("+ targetSize +")");

		for (int i = 0; i < sourceSize; i++) {
			completionService.submit(new CompareJob(sourceNodes[i], targetNodes, compareGroup.threshold, metric, g.getVertexLabelProperty()));
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
							final float similarity = result.similarities.get(i);
							final int e1 = g.addDirectedSimpleEdge(vert, otherVert);
							final int e2 = g.addDirectedSimpleEdge(otherVert, vert);

							// TODO: This would be nicer than adding two edges, however the edge-neighborhood algorithms
							//  would need to account for this, among other things
							//g.addSimpleEdge(vert, otherVert, false);

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
				g.getLiteralPredicateProperty().setValue(i, p.toString(false));
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
    private void addEdge(InMemoryRdfGraph g, Node p, int s_i, int o_i, Map<Node, Integer> edgeTypes, float weight) {
    	// Create a unique id for this predicate given the subject-object pair
		final int p_i = g.addDirectedSimpleEdge(s_i, o_i);
		g.getEdgeLabelProperty().setValue(p_i, p.toString(false));
		// If we have not encountered this edge-type before, give it a unique id
		edgeTypes.putIfAbsent(p, (edgeTypes.size() + 1));
		// Store the edge-type value for this new edge
		g.getEdgeTypeProperty().setValue(p_i, edgeTypes.get(p));
		g.getEdgeWeightProperty().setValue(p_i, weight);
	}

}
