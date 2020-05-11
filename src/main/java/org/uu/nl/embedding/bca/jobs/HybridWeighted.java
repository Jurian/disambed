package org.uu.nl.embedding.bca.jobs;

import grph.properties.NumericalProperty;
import org.uu.nl.embedding.bca.util.BCAJob;
import org.uu.nl.embedding.bca.util.BCV;
import org.uu.nl.embedding.bca.util.PaintedNode;
import org.uu.nl.embedding.convert.util.NodeInfo;
import org.uu.nl.embedding.util.InMemoryRdfGraph;

import java.util.TreeMap;

/**
 * @author Jurian Baas
 */
public class HybridWeighted extends BCAJob {


    private final int[][] vertexOut, vertexIn, edgeOut, edgeIn;

	public HybridWeighted(
            InMemoryRdfGraph graph, int bookmark,
            double alpha, double epsilon,
			int[][] vertexIn, int[][] vertexOut, int[][] edgeIn, int[][] edgeOut) {
		super(bookmark, true, alpha, epsilon, graph);
        this.vertexOut = vertexOut;
        this.vertexIn = vertexIn;
        this.edgeOut = edgeOut;
        this.edgeIn = edgeIn;
	}

    @Override
	protected BCV doWork(final InMemoryRdfGraph graph, final boolean reverse) {

	    final NumericalProperty nodeTypes = graph.getVertexTypeProperty();
        final NumericalProperty edgeWeights = graph.getEdgeWeightProperty();
        final NumericalProperty edgeTypes = graph.getEdgeTypeProperty();

        final TreeMap<Integer, PaintedNode> nodeTree = new TreeMap<>();
        final BCV bcv = new BCV(bookmark);

        nodeTree.put(bookmark, new PaintedNode(bookmark, 1));

        NodeInfo nodeType;
		int[] neighbors, edges;
		int focusNode;
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

            nodeType = NodeInfo.fromByte((byte) nodeTypes.getValueAsInt(focusNode));

            if(nodeType == NodeInfo.LITERAL) {

                neighbors = vertexIn[focusNode];
                edges = edgeIn[focusNode];

            } else {

                if(reverse) neighbors = vertexIn[focusNode];
                else neighbors = vertexOut[focusNode];

                if(reverse) edges = edgeIn[focusNode];
                else edges = edgeOut[focusNode];
            }

            // Use double to avoid integer division
            double totalWeight = 0;
            for (int i = 0; i < neighbors.length; i++) {
                totalWeight += edgeWeights.getValueAsFloat(edges[i]);
            }

            for (int i = 0; i < neighbors.length; i++) {

                float weight = edgeWeights.getValueAsFloat(edges[i]);
                partialWetPaint = (1 - alpha) * wetPaint * (weight / totalWeight);

                // Log(n) time lookup
                if (nodeTree.containsKey(neighbors[i])) {
                    nodeTree.get(neighbors[i]).addPaint(partialWetPaint);
                } else {
                    nodeTree.put(neighbors[i], new PaintedNode(neighbors[i], partialWetPaint));
                }
            }
		}
		return bcv;
	}
}
