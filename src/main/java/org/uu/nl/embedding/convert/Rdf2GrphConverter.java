package org.uu.nl.embedding.convert;

import grph.Grph;
import grph.in_memory.InMemoryGrph;
import grph.properties.NumericalProperty;
import grph.properties.Property;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.uu.nl.embedding.convert.util.NodeInfo;

import java.util.HashMap;
import java.util.Map;

public class Rdf2GrphConverter implements Converter<Grph, Model> {
	
	private static int type2color(Node node) {
		if(node.isURI()) return NodeInfo.URI;
		else if (node.isBlank()) return NodeInfo.BLANK;
		else if (node.isLiteral()) return NodeInfo.LITERAL;
		else throw new IllegalArgumentException("Node " + node + " is not of type URI, blank or literal");
	}
	
	private final boolean literals;
	
	public Rdf2GrphConverter() {
		this(true);
	}
	
	public Rdf2GrphConverter(boolean literals) {
		this.literals = literals;
	}
	
	@Override
	public Grph convert(Model model) {
		
		final Grph g = new InMemoryGrph();
		final NumericalProperty vertexType = g.getVertexColorProperty();
		final NumericalProperty edgeType = g.getEdgeColorProperty();
		final Property vertexLabel = g.getVertexLabelProperty();
		final Property edgeLabel = g.getEdgeLabelProperty();
		
		final Map<Node, Integer> vertexMap = new HashMap<>();
		final Map<Node, Integer> edgeMap = new HashMap<>();
		
		int s_i, o_i, p_i = getVertexCount(model);
		Node s, p, o;
		Triple t;
		
		ExtendedIterator<Triple> triples = model.getGraph().find();
		
		while(triples.hasNext()) {
			t = triples.next();
			s = t.getSubject();
			p = t.getPredicate();
			o = t.getObject();
			
			// Skip if we don't want literals
			if(!literals && o.isLiteral()) continue;

			// Only create a new ID if the subject is not yet present
			if(vertexMap.containsKey(s)) s_i = vertexMap.get(s);
			else s_i = addVertex(g, s, vertexMap, vertexType, vertexLabel);

			// Only create a new ID if the object is not yet present
			if(vertexMap.containsKey(o)) o_i = vertexMap.get(o);
			else o_i = addVertex(g, o, vertexMap, vertexType, vertexLabel);

			// Every edge is unique, we always add one to the graph
			// However there are only a few types of edges (relationships)
			// So we also store a reference to a unique edge-type id
			g.addDirectedSimpleEdge(s_i, p_i, o_i);
			edgeLabel.setValue(p_i, p.toString());

			// If we have not encountered this edge-type before, give it a unique id
			edgeMap.putIfAbsent(p, edgeMap.size());
			// Store the edge-type value for this new edge
			edgeType.setValue(p_i, edgeMap.get(p));
			p_i++;
		}
		return g;
	}

	private int addVertex(Grph g, Node n, Map<Node, Integer> vertexMap, NumericalProperty vertexType, Property vertexLabel) {
		int i = vertexMap.size();
		g.addVertex(i);
		vertexType.setValue(i, type2color(n);
		vertexLabel.setValue(i, n.toString());
		vertexMap.put(n, i);

		return i;
	}

	private int getVertexCount(Model model) {
		String sparql = "SELECT (COUNT(DISTINCT ?vertex) AS ?vertexCount) " + 
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

	}
}
