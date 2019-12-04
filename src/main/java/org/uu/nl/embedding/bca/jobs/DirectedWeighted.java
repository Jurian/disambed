package org.uu.nl.embedding.bca.jobs;

import grph.Grph;
import grph.properties.NumericalProperty;
import org.uu.nl.embedding.bca.util.BCAJob;
import org.uu.nl.embedding.bca.util.BCV;
import org.uu.nl.embedding.bca.util.PaintedNode;

import java.util.TreeMap;

/**
 * @author Jurian Baas
 */
public class DirectedWeighted extends BCAJob {


	private final double[] weights;
    private final int[][] vertexOut, vertexIn, edgeOut, edgeIn;

	public DirectedWeighted(
			Grph graph, int bookmark, double[] weights,
			boolean reverse, boolean predicates, double alpha, double epsilon,
			int[][] vertexIn, int[][] vertexOut, int[][] edgeIn, int[][] edgeOut) {
		super(bookmark, reverse, predicates, alpha, epsilon, graph);
		this.weights = weights;
        this.vertexOut = vertexOut;
        this.vertexIn = vertexIn;
        this.edgeOut = edgeOut;
        this.edgeIn = edgeIn;
	}

    @Override
	protected BCV doWork(Grph graph, boolean reverse) {

        final NumericalProperty nodeSimilarity = graph.getEdgeWidthProperty();
        final TreeMap<Integer, PaintedNode> nodeTree = new TreeMap<>();
        final BCV bcv = new BCV(bookmark);

        nodeTree.put(bookmark, new PaintedNode(bookmark, PaintedNode.SKIP, PaintedNode.SKIP, 1));

		int[] neighbors, edges;
		int focusNode, edgeType;
		double wetPaint, partialWetPaint;
        PaintedNode node;

        while (!nodeTree.isEmpty()) {

            node = nodeTree.pollFirstEntry().getValue();
            focusNode = node.nodeID;
            wetPaint = node.getPaint();

            // Keep part of the available paint on this node, distribute the rest
            bcv.add(focusNode, (alpha * wetPaint));

            // If there is not enough paint we stop and don't distribute among the neighbors
            if (wetPaint < epsilon) continue;

            if(reverse) neighbors = vertexIn[focusNode];
            else neighbors = vertexOut[focusNode];

            if(reverse) edges = edgeIn[focusNode];
            else edges = edgeOut[focusNode];

            // Attempt early stopping as much as possible
            // There are no neighbors of the correct (ingoing or outgoing) type
            if(neighbors.length == 0) continue;
            // There is only one neighbor, and it's where we came from
            if(neighbors.length == 1 && neighbors[0] == node.prevNodeID) continue;

            // Use double to avoid integer division
            double totalWeight = 0;
            for (int i = 0; i < neighbors.length; i++) {

                // Skip any edges we don't want to follow
                if(neighbors[i] == node.prevNodeID) continue;

                if(nodeSimilarity.isSetted(edges[i])) {
                    int similarity = nodeSimilarity.getValueAsInt(edges[i]);
                    totalWeight += similarity / 100d;
                } else {
                    totalWeight += weights[getEdgeType(edges[i])];
                }

            }
            // We ended up skipping all neighbors
            if(totalWeight == 0) continue;

            for (int i = 0; i < neighbors.length; i++) {

                // Skip any edges we don't want to follow
                if(neighbors[i] == node.prevNodeID) continue;

                // We found an edge between two literal nodes
                if(nodeSimilarity.isSetted(edges[i])) {
                    int similarity = nodeSimilarity.getValueAsInt(edges[i]);
                    edgeType = PaintedNode.SKIP;
                    partialWetPaint = (1 - alpha) * wetPaint * (similarity / 100d / totalWeight);
                } else {
                    edgeType = getEdgeType(edges[i]);
                    partialWetPaint = (1 - alpha) * wetPaint * (weights[edgeType] / totalWeight);
                }

                // We can already tell that the neighbor will not have enough paint to continue
                if(partialWetPaint < epsilon)
                    continue;

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
