package org.uu.nl.embedding.convert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
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
		
		final List<Triple> triples = model.getGraph().find().toList();
		final Map<Node, Integer> processed = new HashMap<>();


		final ExtendedIterator<Triple> it = model.getGraph().find();
		try {
			
			while(it.hasNext()) {
				final Triple t = it.next();
				if(t.getSubject().toString().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#nil")) 
					it.remove();
				else if(t.getObject().toString().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#nil")) 
					it.remove();
			}
			
		} finally {
			it.close();
		}
		
		
		int s_i, o_i, p_i;
		Node s, p, o;
		
		for(Triple t : triples) {
			
			s = t.getSubject();
			o = t.getObject();
			
			// Skip if we don't want literals
			if(!literals && o.isLiteral()) continue;

			// Only create a new ID if the subject is not yet present
			if(processed.containsKey(s)) {
				s_i = processed.get(s);
			} else {
				s_i = processed.size();
				g.addVertex(s_i);
				vertexType.setValue(s_i, type2color(s));
				vertexLabel.setValue(s_i, s.toString());
				processed.put(s, s_i);
			}
			
			// Only create a new ID if the object is not yet present
			if(processed.containsKey(o)) {
				o_i = processed.get(o);
			} else {
				o_i = processed.size();
				g.addVertex(o_i);
				vertexType.setValue(o_i, type2color(o));
				vertexLabel.setValue(o_i, o.toString());
				processed.put(o, o_i);
			}
			
		}
		
		int offset = processed.size();
		for(Triple t : triples) {
			
			s = t.getSubject();
			p = t.getPredicate();
			o = t.getObject();
			
			// Skip if we don't want literals
			if(!literals && o.isLiteral()) continue;
			
			s_i = processed.get(s);
			o_i = processed.get(o);
			
			// A new edge is created with every triple
			p_i = offset++;
			g.addDirectedSimpleEdge(s_i, p_i, o_i);
			edgeLabel.setValue(p_i, p.toString());
			processed.putIfAbsent(p, processed.size());
			
			edgeType.setValue(p_i, processed.get(p));
		}

		return g;
	}
}
