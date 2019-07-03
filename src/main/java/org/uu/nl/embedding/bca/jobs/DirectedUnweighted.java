package org.uu.nl.embedding.bca.jobs;

import grph.Grph;
import org.uu.nl.embedding.bca.util.BCAJob;
import org.uu.nl.embedding.bca.util.BCV;
import org.uu.nl.embedding.bca.util.PaintedNode;

import java.util.TreeMap;

/**
 * @author Jurian Baas
 */
public class DirectedUnweighted extends BCAJob {

	private final int[][] vertexOut, vertexIn, edgeOut, edgeIn;
	
	public DirectedUnweighted(Grph graph,
							  int bookmark, boolean reverse, boolean predicates, double alpha, double epsilon,
							  int[][] vertexIn, int[][] vertexOut, int[][] edgeIn, int[][] edgeOut) {
		super(bookmark, reverse, predicates, alpha, epsilon, graph);
		this.vertexOut = vertexOut;
		this.vertexIn = vertexIn;
		this.edgeOut = edgeOut;
		this.edgeIn = edgeIn;
	}

	@Override
	protected BCV doWork(Grph graph, boolean reverse) {

		final TreeMap<Integer, PaintedNode> nodeTree = new TreeMap<>();
		final BCV bcv = new BCV(bookmark);

		nodeTree.put(bookmark, new PaintedNode(bookmark, PaintedNode.SKIP, PaintedNode.SKIP, 1));
		
		int[] neighbors, edges;
		int focusNode, edgeType;
		double partialWetPaint, wetPaint;
		PaintedNode node;

		while (!nodeTree.isEmpty()) {

			node = nodeTree.pollFirstEntry().getValue();
			focusNode = node.nodeID;
			wetPaint = node.getPaint();

            // Keep part of the available paint on this node, distribute the rest
            bcv.add(focusNode, (alpha * wetPaint));

            // If there is not enough paint we stop and don't distribute among the neighbors
            if (wetPaint < epsilon)
                continue;

            if(reverse) neighbors = vertexIn[focusNode];
            else neighbors = vertexOut[focusNode];

            if(neighbors.length == 0)
                continue;

            if(reverse) edges = edgeIn[focusNode];
            else edges = edgeOut[focusNode];

            partialWetPaint = (1 - alpha) * wetPaint / neighbors.length;

            // We can already tell that the neighbors will not have enough paint to continue
            if(partialWetPaint < epsilon)
                continue;

            for (int i = 0; i < neighbors.length; i++) {

				edgeType = getEdgeType(edges[i]);

				if(predicates) {
					// Add the predicate to the context
					int edgeIndex = graph.getVertices().size() + edgeType;
					bcv.add(edgeIndex, epsilon);
				}

				// Log(n) time lookup
				if (nodeTree.containsKey(neighbors[i])) {
					nodeTree.get(neighbors[i]).addPaint(partialWetPaint);
				} else {

					// Remember which node we came from so we don't go back
					// Remember which predicate we used to get here
					nodeTree.put(neighbors[i], new PaintedNode(neighbors[i], edgeType, focusNode, partialWetPaint));
				}
            }
		}
		return bcv;
	}

}
