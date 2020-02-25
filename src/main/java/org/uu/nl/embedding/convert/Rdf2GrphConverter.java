package org.uu.nl.embedding.convert;

import grph.Grph;
import grph.in_memory.InMemoryGrph;
import grph.properties.NumericalProperty;
import grph.properties.Property;
import me.tongfei.progressbar.ProgressBar;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.log4j.Logger;
import org.uu.nl.embedding.convert.util.NodeInfo;
import org.uu.nl.embedding.util.compare.*;
import org.uu.nl.embedding.util.config.Configuration;

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
public class Rdf2GrphConverter implements Converter<Model, Grph> {

	private static final Logger logger = Logger.getLogger(Rdf2GrphConverter.class);

	private final Map<String, Double> weights;
	private final Map<String, SimilarityGroup<String>> similarityGroups;
	private final Configuration config;

	public Rdf2GrphConverter(Configuration config) {

		this.config = config;
		this.weights = config.getWeights();
		this.similarityGroups = new HashMap<>();
		config.getSimilarity().forEach(
				sim -> similarityGroups.put(
						sim.getPredicate(), new SimilarityGroup<>(createSimilarityMetric(sim), sim.getThreshold())));
	}

	private static int type2color(Node node) {
		if(node.isURI()) return NodeInfo.URI.id;
		else if (node.isBlank()) return NodeInfo.BLANK.id;
		else if (node.isLiteral()) return NodeInfo.LITERAL.id;
		else throw new IllegalArgumentException("Node " + node + " is not of type URI, blank or literal");
	}

	@Override
	public Grph convert(Model model) {

		logger.info("Converting RDF data into fast graph representation");

		final Grph g = new InMemoryGrph();

		final Property vertexLabels = g.getVertexLabelProperty();
		final NumericalProperty edgeTypes = g.getEdgeColorProperty();
		final NumericalProperty nodeSimilarity = g.getEdgeWidthProperty();
		
		final Map<Node, Integer> vertexMap = new HashMap<>();
		final Map<Node, Integer> edgeMap = new HashMap<>();

		long skippedTriples = 0;
		int s_i, o_i, p_i, edgeType;
		final boolean doSimilarityMatching = !similarityGroups.isEmpty();
		String predicateString;
		Node s, p, o;
		Triple t   ;

		final ExtendedIterator<Triple> triples = model.getGraph().find();

		//HashSet<String> predSet = new HashSet<>();
		try(ProgressBar pb = config.progressBar("Converting", model.size(), "triples")) {
			while (triples.hasNext()) {

				t = triples.next();
				s = t.getSubject();
				p = t.getPredicate();
				o = t.getObject();

				predicateString = p.toString();
				//predSet.add(predicateString);
				// Ignore unweighted predicates
				if(!weights.containsKey(predicateString)) {
					// Adjust the total number of triples we are considering
					// Maybe we can do pb.step() here instead to make it less confusing
					pb.step();
					skippedTriples++;
					continue;
				}

				// Only create a new ID if the subject is not yet present
				if (!vertexMap.containsKey(s)) addVertex(g, s, vertexMap);
				if (!vertexMap.containsKey(o)) addVertex(g, o, vertexMap);

				s_i = vertexMap.get(s);
				o_i = vertexMap.get(o);

				if(doSimilarityMatching && o.isLiteral()) {

					if(similarityGroups.containsKey(predicateString))
						similarityGroups.get(predicateString).preprocess(o.getLiteralValue().toString(), o_i);

					vertexLabels.setValue(o_i, o.getLiteralValue().toString());
				}

				// Every edge is unique, we always add one to the graph
				// However there are only a few types of edges (relationships)
				// So we also store a reference to a unique edge-type id
				p_i = g.addDirectedSimpleEdge(s_i, o_i);

				g.getEdgeLabelProperty().setValue(p_i, predicateString);

				// If we have not encountered this edge-type before, give it a unique id
				edgeMap.putIfAbsent(p, edgeMap.size());
				// Store the edge-type value for this new edge
				edgeType = edgeMap.get(p);

				edgeTypes.setValue(p_i, edgeType);

				pb.step();
			}
		} finally {
			triples.close();
			if(skippedTriples > 0) logger.info("Skipped " + skippedTriples +
					" unweighted triples (" + String.format("%.2f", (skippedTriples/(double)model.size()*100)) + " %)");
		}

		if(doSimilarityMatching) {
			for(Map.Entry<String, SimilarityGroup<String>> entry : similarityGroups.entrySet()) {

				final ExecutorService es = Executors.newWorkStealingPool(config.getThreads());
				final CompletionService<CompareResult> completionService = new ExecutorCompletionService<>(es);

				final SimilarityGroup<String> similarityGroup = entry.getValue();
				final int groupSize = similarityGroup.nodes.size();
				final Similarity<String> metric = similarityGroup.similarity;
				final int[] nodes = similarityGroup.nodes.stream().mapToInt(i -> i).toArray();

				logger.info("Processing similarities for predicate " + entry.getKey());

				for (int i = 0; i < groupSize; i++) {
					completionService.submit(new CompareJob(i, nodes, similarityGroup.threshold, metric, vertexLabels));
				}

				int received = 0;
				int edgesAdded = 0;
				try(ProgressBar pb = config.progressBar("Comparing", groupSize, "literals")) {
					pb.setExtraMessage(Integer.toString(edgesAdded));
					while (received < groupSize) {
						try {

							final CompareResult result = completionService.take().get();
							final int vert = result.vert;
							final int size = result.otherVerts.size();
							for (int i = 0; i < size; i++) {

								final int otherVert = result.otherVerts.get(i);
								final byte similarity = result.similarities.get(i);
								final int e1 = g.addDirectedSimpleEdge(vert, otherVert);
								final int e2 = g.addDirectedSimpleEdge(otherVert, vert);

								nodeSimilarity.setValue(e1, similarity);
								nodeSimilarity.setValue(e2, similarity);

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
		} else {
			logger.info("Partial matching is disabled, no edges between similar literals are added");
		}

		return g;
	}

	private Similarity<String> createSimilarityMetric(Configuration.SimilarityGroup sim ) {

		switch (sim.getMethodEnum()) {
			case TOKEN:
				return new TokenSimilarity();
			case NGRAM:
				if(sim.getNgram() == 0) return new NGramSimilarity();
				else return new NGramSimilarity(sim.getNgram());
			case JAROWINKLER:
				return new JaroWinklerSimilarity();
		}
		return null;
	}

	private void addVertex(Grph g, Node n, Map<Node, Integer> vertexMap) {
		final int i = vertexMap.size();
		g.addVertex(i);
		g.getVertexColorProperty().setValue(i, type2color(n));
		g.getVertexLabelProperty().setValue(i, clean(n.toString()));
		vertexMap.put(n, i);
    }

    private String clean(String s) {
	    return s.replace("\"","");
    }

	private static class SimilarityGroup<T> {

		public final Similarity<T> similarity;
		public final HashSet<Integer> nodes;
		public final double threshold;

		private SimilarityGroup(Similarity<T> similarity, double threshold) {
			this.similarity = similarity;
			this.nodes = new HashSet<>();
			this.threshold = threshold;
		}

		public void preprocess(T a, int i){
			this.similarity.preProcess(a);
			this.nodes.add(i);
		}
	}

	private static class CompareResult {

		public final int vert;
		public final ArrayList<Integer> otherVerts;
		public final ArrayList<Byte> similarities;

		public CompareResult(int vert) {
			this.vert = vert;
			this.otherVerts = new ArrayList<>();
			this.similarities = new ArrayList<>();
		}
	}

	private static class CompareJob implements Callable<CompareResult> {

		private final int index;
		private final int[] nodes;
		private final double threshold;
		private final Similarity<String> metric;
		private final Property vertexLabels;

		public CompareJob(int index, int[] nodes, double threshold, Similarity<String> metric, Property vertexLabels) {
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
				final double similarity = metric.calculate(s1, s2);

				if (similarity >= threshold) {

					final byte b = (byte) (similarity * 100);
					result.otherVerts.add(otherVert);
					result.similarities.add(b);
				}
			}
			return result;
		}
	}
}
