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
import org.uu.nl.embedding.Settings;
import org.uu.nl.embedding.convert.util.NodeInfo;
import org.uu.nl.embedding.util.read.WeightsReader;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Converts an RDF graph to the Grph form
 *
 * @author Jurian Baas
 */
public class Rdf2GrphConverter implements Converter<Model, Grph> {

	private static final Logger logger = Logger.getLogger(Rdf2GrphConverter.class);
	private static final Settings settings = Settings.getInstance();

	private Map<String, Integer> weights;

	private static int type2color(Node node) {
		if(node.isURI()) return NodeInfo.URI;
		else if (node.isBlank()) return NodeInfo.BLANK;
		else if (node.isLiteral()) return NodeInfo.LITERAL;
		else throw new IllegalArgumentException("Node " + node + " is not of type URI, blank or literal");
	}


	public Rdf2GrphConverter(Map<String, Integer> weights) {
		this.weights = weights;
	}

	@Override
	public Grph convert(Model model) {
		
		final Grph g = new InMemoryGrph();

		final NumericalProperty edgeTypes = g.getEdgeColorProperty();
		final Property edgeLabel = g.getEdgeLabelProperty();
		
		final Map<Node, Integer> vertexMap = new HashMap<>();
		final Map<Node, Integer> edgeMap = new HashMap<>();
		//final int vertexCount = vertexMap.size();

		//logger.info("Converting Jena model with "+vertexCount+" vertices");

		int s_i, o_i, p_i, edgeType;
		Node s, p, o;
		Triple t;
		
		final ExtendedIterator<Triple> triples = model.getGraph().find();

		try(ProgressBar pb = settings.progressBar("Converting", model.size(), "triples")) {
			while (triples.hasNext()) {

				t = triples.next();
				s = t.getSubject();
				p = t.getPredicate();
				o = t.getObject();

				// Ignore unweighted predicates
				if(!weights.containsKey(p.toString())) {
					// Adjust the total number of triples we are considering
					// Maybe we can do pb.step() here instead to make it less confusing
					pb.maxHint(pb.getMax()-1);
					continue;
				}

				// Only create a new ID if the subject is not yet present
				if (!vertexMap.containsKey(s)) addVertex(g, s, vertexMap);
				if (!vertexMap.containsKey(o)) addVertex(g, o, vertexMap);

				s_i = vertexMap.get(s);
				o_i = vertexMap.get(o);

				// Every edge is unique, we always add one to the graph
				// However there are only a few types of edges (relationships)
				// So we also store a reference to a unique edge-type id
				p_i = g.addDirectedSimpleEdge(s_i, o_i);
				edgeLabel.setValue(p_i, p.toString());

				// If we have not encountered this edge-type before, give it a unique id
				edgeMap.putIfAbsent(p, edgeMap.size());
				// Store the edge-type value for this new edge
				edgeType = edgeMap.get(p);

				edgeTypes.setValue(p_i, edgeType);
				pb.step();
			}
		} finally {
			triples.close();
		}

		//assert g.getVertices().size() == vertexCount: g.getVertices().size() + "!=" + vertexCount;

		return g;
	}

	private void addVertex(Grph g, Node n, Map<Node, Integer> vertexMap) {
		final int i = vertexMap.size();
		g.addVertex(i);
		g.getVertexColorProperty().setValue(i, type2color(n));
		g.getVertexLabelProperty().setValue(i, n.toString());
		vertexMap.put(n, i);
    }

}
