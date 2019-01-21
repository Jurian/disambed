package org.uu.nl.embedding.convert;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.uu.nl.embedding.convert.util.NodeInfo;

import grph.Grph;
import grph.in_memory.InMemoryGrph;
import grph.properties.NumericalProperty;
import grph.properties.Property;

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
		
		final Map<Node, Integer> verticeMap = new HashMap<>();
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
			if(verticeMap.containsKey(s)) {
				s_i = verticeMap.get(s);
			} else {
				s_i = verticeMap.size();
				g.addVertex(s_i);
				vertexType.setValue(s_i, type2color(s));
				vertexLabel.setValue(s_i, s.toString());
				verticeMap.put(s, s_i);
			}
			
			// Only create a new ID if the object is not yet present
			if(verticeMap.containsKey(o)) {
				o_i = verticeMap.get(o);
			} else {
				o_i = verticeMap.size();
				g.addVertex(o_i);
				vertexType.setValue(o_i, type2color(o));
				vertexLabel.setValue(o_i, o.toString());
				verticeMap.put(o, o_i);
			}
			
			g.addDirectedSimpleEdge(s_i, p_i, o_i);
			edgeLabel.setValue(p_i, p.toString());
			edgeMap.putIfAbsent(p, edgeMap.size());
			edgeType.setValue(p_i, edgeMap.get(p));
			p_i++;
		}
		/*
		triples = model.getGraph().find();
		while(triples.hasNext()) {
			t = triples.next();
			s = t.getSubject();
			p = t.getPredicate();
			o = t.getObject();
			
			// Skip if we don't want literals
			if(!literals && o.isLiteral()) continue;
			
			s_i = verticeMap.get(s);
			o_i = verticeMap.get(o);
			
			// A new edge is created with every triple
			p_i = verticeMap.size();
			g.addDirectedSimpleEdge(s_i, p_i, o_i);
			edgeLabel.setValue(p_i, p.toString());
			edgeMap.putIfAbsent(p, edgeMap.size());
			edgeType.setValue(p_i, edgeMap.get(p));
		}
		*/
		return g;
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
