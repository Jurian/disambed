package org.uu.nl.disembed.embedding.convert;

import me.tongfei.progressbar.ProgressBar;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.log4j.Logger;
import org.uu.nl.disembed.embedding.convert.util.NodeInfo;
import org.uu.nl.disembed.util.config.Configuration;
import org.uu.nl.disembed.util.config.EmbeddingConfiguration;
import org.uu.nl.disembed.util.progress.Progress;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public record SimpleRdf2GraphConverter(Configuration config) implements Converter<Model, InMemoryRdfGraph> {

    private static final String QUERY_OUTPUT_FORMAT =
            "SELECT ?entity WHERE {?entity a %s.}";

    private static final Logger logger = Logger.getLogger(SimpleRdf2GraphConverter.class);

    @Override
    public InMemoryRdfGraph convert(Model model) {

        logger.info("Converting RDF data into fast graph representation");
        EmbeddingConfiguration embeddingConfig = config.getEmbedding();
        try {

            final InMemoryRdfGraph g = new InMemoryRdfGraph();

            final Set<String> prefixedFilter = embeddingConfig.getPredicates().getFilter();
            final Set<String> expandedFilter = new HashSet<>();

            if (embeddingConfig.getPrefixes() != null) {
                embeddingConfig.getPrefixes().forEach(model::setNsPrefix);
            }

            for (String prefixed : prefixedFilter) {
                expandedFilter.add(model.expandPrefix(prefixed));
            }

            embeddingConfig.getPredicates().setFilter(expandedFilter);

            final Set<Node> outputNodes = new HashSet<>();

            for (String type : embeddingConfig.getTargetTypes()) {

                final ParameterizedSparqlString query = new ParameterizedSparqlString();
                query.setCommandText(String.format(QUERY_OUTPUT_FORMAT, type));
                query.setNsPrefixes(model.getNsPrefixMap());
                try (QueryExecution exec = QueryExecutionFactory.create(query.asQuery(), model)) {
                    final ResultSet entities = exec.execSelect();
                    while (entities.hasNext()) {
                        final QuerySolution solution = entities.next();
                        outputNodes.add(solution.get("entity").asNode());
                    }
                }
            }

            final Map<Node, Map<Node, Integer>> predicateLiteralMap = new HashMap<>();
            final Map<Node, Integer> vertexMap = new HashMap<>();
            final Map<Node, Integer> edgeTypes = new HashMap<>();

            AtomicLong skippedTriples = new AtomicLong();

            final ExtendedIterator<Triple> triples = model.getGraph().find();
            final Rdf2GrphConverter.PredicateStatistics statistics = new Rdf2GrphConverter.PredicateStatistics();

            try (ProgressBar pb = Progress.progressBar("Converting", model.size(), "triples")) {

                triples.forEach(triple -> {
                    Node s = triple.getSubject();
                    Node p = triple.getPredicate();
                    Node o = triple.getObject();

                    final String predicateString = model.expandPrefix(p.toString(false));

                    // Ignore unweighted predicates
                    if (!expandedFilter.contains(predicateString)) {
                        skippedTriples.getAndIncrement();
                    } else {
                        // Only create a new ID if the vertex is not yet present
                        int s_i = addVertex(g, p, s, vertexMap, predicateLiteralMap, model);
                        int o_i = addVertex(g, p, o, vertexMap, predicateLiteralMap, model);
                        int p_i = addEdge(g, p, s_i, o_i, edgeTypes, model);

                        // Mark a node for output
                        if (s.isURI() && outputNodes.contains(s)) {
                            g.addFocusNode(s_i);
                        } else if (o.isURI() && outputNodes.contains(o)) {
                            g.addFocusNode(o_i);
                        }
                    }

                    pb.step();
                });
            } finally {
                logger.info("Skipped " + skippedTriples +
                        " unweighted triples (" + String.format("%.2f", (skippedTriples.get() / (double) model.size() * 100)) + " %)");
            }

            return g;
        } finally {
            model.close();
        }
    }

    /**
     * Add a vertex to the graph if not present, the vertices are given consecutive IDs.
     * Node type and label information are also added
     */
    private int addVertex(InMemoryRdfGraph g, Node p, Node n, Map<Node, Integer> vertexMap, Map<Node, Map<Node, Integer>> predicateLiteralMap, PrefixMapping prefixMapping) {

        Integer key = vertexMap.get(n);
        if (!n.isLiteral() && key != null) return key; // Non-literal node is already present

        final int i = g.getNextVertexAvailable();

        if (n.isLiteral()) {
            // Literals are merged per predicate
            final Map<Node, Integer> nodes = predicateLiteralMap.computeIfAbsent(p, k -> new HashMap<>());

            key = nodes.get(n);
            if (key != null) {
                // Predicate-literal pair already present
                return key;
            } else {
                // First time we see this predicate-literal pair
                nodes.put(n, i);
                g.getLiteralPredicateProperty().setValue(i, p.toString(prefixMapping, false));
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
        g.getVertexLabelProperty().setValue(i, n.toString(prefixMapping, false));
        return i;
    }

    /**
     * Edges are considered unique for a subject-object pair
     * However there are only a few types of edges (relationships)
     * So we also store a reference to a unique edge-type id
     */
    int addEdge(InMemoryRdfGraph g, Node p, int s_i, int o_i, Map<Node, Integer> edgeTypes, PrefixMapping prefixMapping) {
        // Create a unique id for this predicate given the subject-object pair
        final int p_i = g.addUndirectedSimpleEdge(s_i, o_i);
        g.getEdgeLabelProperty().setValue(p_i, p.toString(prefixMapping, false));
        // If we have not encountered this edge-type before, give it a unique id
        edgeTypes.putIfAbsent(p, (edgeTypes.size() + 1));
        // Store the edge-type value for this new edge
        g.getEdgeTypeProperty().setValue(p_i, edgeTypes.get(p));

        return p_i;
        //g.getEdgeWeightProperty().setValue(p_i, weight);
    }
}
