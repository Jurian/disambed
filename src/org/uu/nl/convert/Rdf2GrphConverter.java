package org.uu.nl.convert;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.util.iterator.ExtendedIterator;

import grph.Grph;
import grph.in_memory.InMemoryGrph;
import grph.properties.NumericalProperty;
import grph.properties.Property;

public class Rdf2GrphConverter implements Converter<Grph, Model> {
	
	public static final byte LITERAL = 2, BLANK = 1, URI = 0;
	
	private static int type2color(Node node) {
		if(node.isURI()) return URI;
		else if (node.isBlank()) return BLANK;
		else if (node.isLiteral()) return LITERAL;
		else throw new IllegalArgumentException("Node " + node + " is not of type URI, blank or literal");
	}
	
	@Override
	public Grph convert(Model model) {
		
		final Grph g = new InMemoryGrph();
		final NumericalProperty vertexType = g.getVertexColorProperty();
		final Property vertexLabel = g.getVertexLabelProperty();
		final Property edgeLabel = g.getEdgeLabelProperty();
		
		final ExtendedIterator<Triple> it = model.getGraph().find();
		final Map<Node, Integer> processed = new HashMap<>();

		try {
			
			int s_i, o_i;
			Node s, p, o;
			
			while(it.hasNext()) {
				
				final Triple t = it.next();
				
				s = t.getSubject();
				p = t.getPredicate();
				o = t.getObject();

				// Only create a new ID if the subject is not yet present
				if(!processed.containsKey(s)) {
					s_i = g.addVertex();
					vertexType.setValue(s_i, type2color(s));
					vertexLabel.setValue(s_i, s.toString());
					processed.put(s, s_i);
				} else {
					s_i = processed.get(s);
				}
				
				// Only create a new ID if the object is not yet present
				if(!processed.containsKey(o)) {
					o_i = g.addVertex();
					vertexType.setValue(o_i, type2color(o));
					vertexLabel.setValue(o_i, o.toString());
					processed.put(o, o_i);
				} else {
					o_i = processed.get(o);
				}
				
				// A new edge is created with every triple
				final int p_i = g.addDirectedSimpleEdge(s_i, o_i);
				edgeLabel.setValue(p_i, p.toString());
			}
			
		} finally {
			it.close();
		}
		
		return g;
	}
}
