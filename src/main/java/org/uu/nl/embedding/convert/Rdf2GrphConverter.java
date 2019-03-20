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

		// The spaces of identifiers do not have to be contiguous, though the data
		// structure exhibit de best memory performance when these spaces are dense.

		// For this reason we give consecutive id's to the edges, starting after all
		// identifiers that will be necessary for the vertices
		final int vertexCount = getVertexCount(model);
		int s_i, o_i, p_i = vertexCount;
		Node s, p, o;
		Triple t;

		final ExtendedIterator<Triple> triples = model.getGraph().find();

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

		// Pre-compute the edge neighborhoods, using the same order as the node neighborhoods
		//System.out.println("Pre-computing edge in-neighborhoods");
		//int[][] inEdgeNeighborhood = getInEdgeNeighborhood(g);
		//System.out.println("Pre-computing edge out-neighborhoods");
		//int[][] outEdgeNeighborhood = getOutEdgeNeighborhood(g);

		return g;
	}
/*
	private int[][] getInEdgeNeighborhood(Grph g) {
		int[][] inNeighborhood = g.getInNeighborhoods();
		int[][] edgeNeighborhood = new int[inNeighborhood.length][];


		for(int focusNode = 0; focusNode < inNeighborhood.length; focusNode++) {

			int[] inEdges = g.getInOnlyEdges(focusNode).toIntArray();

			edgeNeighborhood[focusNode] = new int[inNeighborhood[focusNode].length];
			for(int j = 0; j < inNeighborhood[focusNode].length; j++) {

				int neighbor = inNeighborhood[focusNode][j];
				int[] outEdges = g.getOutOnlyEdges(neighbor).toIntArray();

				edgeNeighborhood[focusNode][j] = getEdge(g, neighbor, focusNode, outEdges, inEdges);
				assert  edgeNeighborhood[focusNode][j] != -1;
			}
		}

		return  edgeNeighborhood;
	}

	private int[][] getOutEdgeNeighborhood(Grph g) {

		int[][] outNeighborhood = g.getOutNeighborhoods();
		int[][] edgeNeighborhood = new int[outNeighborhood.length][];

		for(int focusNode = 0; focusNode < outNeighborhood.length; focusNode++) {

			int[] outEdges = g.getOutOnlyEdges(focusNode).toIntArray();

			edgeNeighborhood[focusNode] = new int[outNeighborhood[focusNode].length];
			for(int j = 0; j < outNeighborhood[focusNode].length; j++) {

				int neighbor = outNeighborhood[focusNode][j];
				int[] inEdges = g.getInOnlyEdges(neighbor).toIntArray();

				edgeNeighborhood[focusNode][j] = getEdge(g, focusNode, neighbor, outEdges, inEdges);
				assert  edgeNeighborhood[focusNode][j] != -1;
			}
		}

		return  edgeNeighborhood;
	}
	private int getEdge(Grph graph, int src, int dest, int[] out, int[] in) {
		if (out.length == 0 || in.length == 0) {
			return -1;
		} else {
			if (out.length < in.length) {
				for(int e : out)
					if (graph.getDirectedSimpleEdgeHead(e) == dest) return e;
			} else {
				for(int e : in)
					if (graph.getDirectedSimpleEdgeTail(e) == src) return e;
			}
			return -1;
		}
	}
*/
	private int addVertex(Grph g, Node n, Map<Node, Integer> vertexMap, NumericalProperty vertexType, Property vertexLabel) {
		final int i = vertexMap.size();
		g.addVertex(i);
		vertexType.setValue(i, type2color(n));
		vertexLabel.setValue(i, n.toString());
		vertexMap.put(n, i);

		return i;
	}

	private int getVertexCount(Model model) {
		// Taken from https://stackoverflow.com/a/24189589/10610389
		final Query qry = QueryFactory.create(
				"SELECT (COUNT(DISTINCT ?vertex) AS ?vertexCount) " +
				"WHERE" +
				"{" +
					"{" +
						"?vertex ?p [] " +
					"}" +
					"UNION" +
					"{ " +
						// The [] is an anonymous variable because you don't care about the some of the positions on either side of the UNION
						"[] ?p ?vertex " +
						// Filter out literals as they have to be on the right hand side
						"FILTER(!IsLiteral(?vertex))" +
					"}" +
				"}"
		);
		try(QueryExecution qe = QueryExecutionFactory.create(qry, model)) {
			final ResultSet rs = qe.execSelect();

			if (rs.hasNext()) {
				return rs.nextSolution().getLiteral("vertexCount").getInt();
			}
		}
		return -1;

	}
}
