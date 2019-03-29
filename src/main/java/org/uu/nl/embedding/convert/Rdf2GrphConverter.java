package org.uu.nl.embedding.convert;

import grph.Grph;
import grph.in_memory.InMemoryGrph;
import grph.properties.NumericalProperty;
import grph.properties.Property;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.log4j.Logger;
import org.uu.nl.embedding.convert.util.NodeInfo;

import java.util.HashMap;
import java.util.Map;

public class Rdf2GrphConverter implements Converter<Grph, Model> {

	final static Logger logger = Logger.getLogger(Rdf2GrphConverter.class);
	private static final int PB_UPDATE_INTERVAL = 250;
	private static final ProgressBarStyle PB_STYLE = ProgressBarStyle.COLORFUL_UNICODE_BLOCK;

	private static int type2color(Node node) {
		if(node.isURI()) return NodeInfo.URI;
		else if (node.isBlank()) return NodeInfo.BLANK;
		else if (node.isLiteral()) return NodeInfo.LITERAL;
		else throw new IllegalArgumentException("Node " + node + " is not of type URI, blank or literal");
	}

	@Override
	public Grph convert(Model model) {
		
		final Grph g = new InMemoryGrph();

		final NumericalProperty edgeTypes = g.getEdgeColorProperty();

		final Property edgeLabel = g.getEdgeLabelProperty();
		
		final Map<Node, Integer> vertexMap = getVertices(model, g);
		final Map<Node, Integer> edgeMap = new HashMap<>();

		final int vertexCount = vertexMap.size();

		logger.info("Converting Jena model with "+vertexCount+" vertices");

		int s_i, o_i, p_i = vertexCount;
		Node s, p, o;
		Triple t;
		
		final ExtendedIterator<Triple> triples = model.getGraph().find();

		try(ProgressBar pb = new ProgressBar("Converting", model.size(), PB_UPDATE_INTERVAL, System.out, PB_STYLE, " triples", 1 , true)) {
			while (triples.hasNext()) {
				t = triples.next();
				s = t.getSubject();
				p = t.getPredicate();
				o = t.getObject();

				assert vertexMap.containsKey(s);
				assert vertexMap.containsKey(o);

				s_i = vertexMap.get(s);
				o_i = vertexMap.get(o);

				// Every edge is unique, we always add one to the graph
				// However there are only a few types of edges (relationships)
				// So we also store a reference to a unique edge-type id
				g.addDirectedSimpleEdge(s_i, p_i, o_i);
				edgeLabel.setValue(p_i, p.toString());

				// If we have not encountered this edge-type before, give it a unique id
				edgeMap.putIfAbsent(p, edgeMap.size());
				// Store the edge-type value for this new edge
				int edgeType = edgeMap.get(p);

				edgeTypes.setValue(p_i, edgeType);
				p_i++;
				pb.step();
			}
		} finally {
			triples.close();
		}

		assert g.getVertices().size() == vertexCount: g.getVertices().size() + "!=" + vertexCount;

		return g;
	}

	private Map<Node, Integer> getVertices(Model model, Grph g) {

		final Map<Node, Integer> vertexMap = new HashMap<>();
		final ExtendedIterator<Triple> triples = model.getGraph().find();

		try {
			Node s, o;
			Triple t;

			while (triples.hasNext()) {
				t = triples.next();
				s = t.getSubject();
				o = t.getObject();

				// Only create a new ID if the subject is not yet present
				if (!vertexMap.containsKey(s)) addVertex(g, s, vertexMap);
				if (!vertexMap.containsKey(o)) addVertex(g, o, vertexMap);
			}
		} finally {
			triples.close();
		}

		return vertexMap;
	}

	private int addVertex(Grph g, Node n, Map<Node, Integer> vertexMap) {
		final int i = vertexMap.size();
		g.addVertex(i);
		g.getVertexColorProperty().setValue(i, type2color(n));
		g.getVertexLabelProperty().setValue(i, n.toString());
		vertexMap.put(n, i);
		return i;
	}

	/*
	private int getVertexCount(Model model) {
		final String sparql = "SELECT (COUNT(DISTINCT ?vertex) AS ?vertexCount) " +
				"WHERE" + 
				"{" + 
					"{" + 
						"?vertex ?p [] " + 
					"}" + 
					"UNION" +
					"{ " + 
						"[] ?p ?vertex " + 
						"FILTER(!IsLiteral(?vertex))" + 
					"}" + 
				"}";
		Query qry = QueryFactory.create(sparql);
		try(QueryExecution qe = QueryExecutionFactory.create(qry, model)) {
			ResultSet rs = qe.execSelect();

			while (rs.hasNext()) {
				return rs.nextSolution().getLiteral("vertexCount").getInt();
			}
		}
		return -1;

	}*/
}
