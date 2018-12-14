package org.uu.nl.analyze.bca.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.shared.Lock;
import org.apache.jena.util.iterator.ExtendedIterator;

/**
 * This class will collect all the necessary information about the graph
 * in an efficient manner by only traversing it once.
 * @author Jurian Baas
 */
public class GraphStatistics {
	
	/**
	 * A mapping between nodes and a unique index, used in the bookmark 
	 * coloring algorithm to do lookups in constant time.
	 */
	public final Map<Node, Integer> keys;
	/**
	 * The type (URI, blank, literal, predicate) for each node in the graph
	 */
	public final NodeType[] types;
	/**
	 * All the unique nodes in the graph from which 
	 * we can start the bookmark coloring algorithm
	 */
	public final Node[] jobs;
	/**
	 * The string representations of all the nodes in the graph
	 */
	public final String[] dict;
	
	private int predicateNodeCount;
	private int uriNodeCount;
	private int blankNodeCount;
	private int literalNodeCount;
	
	public GraphStatistics(Model model) {
		
		final Map<Node, Integer> keys = new HashMap<>((int) model.size());
		final List<String> dict = new ArrayList<>();
		final List<NodeType> types = new ArrayList<>();

		final ExtendedIterator<Triple> it = model.getGraph().find();
		int i = 0;
		try {
			model.enterCriticalSection(true);
			while(it.hasNext()) {
				final Triple t = it.next();
				
				if(!keys.containsKey(t.getSubject())) {
					
					NodeType type = NodeType.fromNode(t.getSubject());
					if(type == NodeType.BLANK) blankNodeCount++;
					else if(type == NodeType.URI) uriNodeCount++;
					else if(type == NodeType.LITERAL) literalNodeCount++;
					
					keys.put(t.getSubject(), i++);
					dict.add(t.getSubject().toString(true));
					types.add(type);
				}
				if(!keys.containsKey(t.getPredicate())) {
					keys.put(t.getPredicate(), i++);
					dict.add(t.getPredicate().toString(true));
					types.add(NodeType.PREDICATE);
					predicateNodeCount++;
				}
				if(!keys.containsKey(t.getObject())) {
					
					NodeType type = NodeType.fromNode(t.getObject());
					if(type == NodeType.BLANK) blankNodeCount++;
					else if(type == NodeType.URI) uriNodeCount++;
					else if(type == NodeType.LITERAL) literalNodeCount++;
					
					keys.put(t.getObject(), i++);
					dict.add(t.getObject().toString(true));
					types.add(type);
				}
			}
		} finally {
			it.close();
			model.leaveCriticalSection();
		}
		
		this.keys = Collections.unmodifiableMap(keys);
		this.jobs = computeBcvOrder(model).stream().filter(node -> node.isURI()).toArray(Node[]::new);
		this.types = types.toArray(new NodeType[types.size()]);
		this.dict = dict.toArray(new String[dict.size()]);
	}

	private List<Node> computeBcvOrder(Model model) {

		final Queue<Node> priority = new PriorityQueue<>(Collections.reverseOrder(new Comparator<Node>() {
			@Override
			public int compare(Node n1, Node n2) {
				return Integer.compare(inDegree(n1, model.getGraph()), inDegree(n2, model.getGraph()));
			}
		}));
		
		final Set<Node> nodes = getNodes(model);
		final List<Node> order = new ArrayList<>(nodes.size());
		// We want to put nodes with 0 out-degree first and then order nodes by their descending in-degree
		for(Node n : nodes) 
			if(outDegree(n, model.getGraph()) == 0) order.add(n);
			else priority.add(n);
		
		while(!priority.isEmpty()) 
			order.add(priority.poll());
		
		return order;
	}
	
	private Set<Node> getNodes(Model model) {
		
		final Set<Node> nodes = new HashSet<>();
		final ExtendedIterator<Triple> it = model.getGraph().find();
		model.enterCriticalSection(Lock.READ);
		try {
			while(it.hasNext()) {
				final Triple t = it.next();
				nodes.add(t.getSubject());
				nodes.add(t.getObject());
			}
		} finally {
			it.close();
			model.leaveCriticalSection();
		}
		return nodes;
	}
	
	private Set<Node> getDependentNodes(Node n, Graph g) {
		Set<Node> nodes = new HashSet<>();
		ExtendedIterator<Triple> it;
		
		it = g.find(n, Node.ANY, Node.ANY);
		try {
			while(it.hasNext()) nodes.add(it.next().getObject());
		} finally {
			it.close();
		}
	
		return nodes;
	}
	
	private void removeNode(Node n, Graph g) {
		g.remove(Node.ANY, Node.ANY, n);
		g.remove(n, Node.ANY, Node.ANY);
	}
	
	private int inDegree(Node n, Graph g) {
		return g.find(Node.ANY, Node.ANY, n).toList().size();
	}
	
	private int outDegree(Node n, Graph g) {
		return g.find(n, Node.ANY, Node.ANY).toList().size();
	}
	
	public int getPredicateNodeCount() {
		return predicateNodeCount;
	}

	public void setPredicateNodeCount(int predicateNodeCount) {
		this.predicateNodeCount = predicateNodeCount;
	}

	public int getUriNodeCount() {
		return uriNodeCount;
	}

	public void setUriNodeCount(int uriNodeCount) {
		this.uriNodeCount = uriNodeCount;
	}

	public int getBlankNodeCount() {
		return blankNodeCount;
	}

	public void setBlankNodeCount(int blankNodeCount) {
		this.blankNodeCount = blankNodeCount;
	}

	public int getLiteralNodeCount() {
		return literalNodeCount;
	}

	public void setLiteralNodeCount(int literalNodeCount) {
		this.literalNodeCount = literalNodeCount;
	}

	
}