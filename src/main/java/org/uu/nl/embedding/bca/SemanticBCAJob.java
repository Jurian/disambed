package org.uu.nl.embedding.bca;

import grph.Grph;
import org.uu.nl.embedding.bca.util.BCAJob;
import org.uu.nl.embedding.bca.util.BCV;
import org.uu.nl.embedding.bca.util.PaintRegistry;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author Jurian Baas
 */
public class SemanticBCAJob extends BCAJob {

	private static final int SKIP = -1;

    private final int[][] vertexOut, vertexIn, edgeOut, edgeIn;

	public SemanticBCAJob(
			Grph graph, int bookmark,
			boolean reverse, double alpha, double epsilon,
			int[][] vertexIn, int[][] vertexOut, int[][] edgeIn, int[][] edgeOut) {
		super(bookmark, reverse, alpha, epsilon, graph);
        this.vertexOut = vertexOut;
        this.vertexIn = vertexIn;
        this.edgeOut = edgeOut;
        this.edgeIn = edgeIn;
	}

    /**
     * Used for storing information about how we got to a focus node
     */
	private class SemanticNode {

        /**
         * The ID of this node
         */
		private final int nodeID;
        /**
         * The ID of the node that lead us to the current node
         */
		private final int prevNodeID;
        /**
         * The ID of the predicate relationship between the previous and current node
         */
		private final int predicateID;

		SemanticNode(int nodeID, int predicateID, int prevNodeID) {
			this.nodeID = nodeID;
			this.prevNodeID = prevNodeID;
			this.predicateID = predicateID;
		}

		@Override
		public boolean equals(Object obj) {
			if(obj instanceof SemanticNode) {
				return this.nodeID == ((SemanticNode) obj).nodeID;
			} else return false;
		}
	}

	@Override
	protected BCV doWork(Grph graph, boolean reverse) {
		
		final Queue<SemanticNode> nodeQueue = new LinkedList<>();
		final PaintRegistry<Integer> wetPaintRegister = new PaintRegistry<>();
		final BCV bcv = new BCV(bookmark);

		boolean focusIsLiteral;
		boolean[] skip;
		int[] neighbors, edges;
		int focusNode, neighbor, edge, neighborCount, ignoredEdgeCount;
		double partialWetPaint;
		SemanticNode node;

		nodeQueue.add(new SemanticNode(bookmark, SKIP, SKIP));
		wetPaintRegister.put(bookmark, 1d);

		while (!nodeQueue.isEmpty()) {

			node = nodeQueue.poll();
			focusNode = node.nodeID;
			final double wetPaint = wetPaintRegister.get(focusNode);

            // Keep part of the available paint on this node, distribute the rest
            bcv.add(focusNode, (alpha * wetPaint));

            // If there is not enough paint we stop and don't distribute among the neighbors
            if (wetPaint < epsilon) continue;

            ignoredEdgeCount = 0;
            focusIsLiteral = isLiteral(focusNode);


            skip = null;
            // In the case of a literal node, we follow incoming relationships of the same
            // type as the one that was used to reach the literal node
            if(focusIsLiteral) {
                neighbors = vertexIn[focusNode];
                skip = new boolean[edgeIn[focusNode].length];
                assert neighbors.length > 0;

                // Cache edges and reuse in loop
                edges = edgeIn[focusNode];

                // If the bookmark is a literal, the previous predicate ID will be SKIP
                int prevEdgeType = (node.prevNodeID == SKIP) ? SKIP : getEdgeType(node.predicateID);

                // Check which edges we need to follow and which to ignore
                for(int i = 0; i < neighbors.length; i++) {

                    edge = edges[i];

                    // In case the predicate is SKIP, we consider all edges
                    if(prevEdgeType == SKIP) continue;
                    // otherwise filter out the ones that are not like the incoming predicate
                    if(getEdgeType(edge) != prevEdgeType) {
                        skip[i] = true;
                        ignoredEdgeCount++;
                    }
                }
            } else {

                if(reverse) neighbors = vertexIn[focusNode];
                else neighbors = vertexOut[focusNode];

                if(reverse) edges = edgeIn[focusNode];
                else edges = edgeOut[focusNode];

                if(neighbors.length > 0 && node.predicateID == SKIP) ignoredEdgeCount++;

            }

            neighborCount = neighbors.length - ignoredEdgeCount;
            assert neighborCount >= 0;
            if(neighborCount == 0)
                continue;

            partialWetPaint = (1 - alpha) * wetPaint / neighborCount;

            // We can already tell that the neighbors will not have enough paint to continue
            if(partialWetPaint < epsilon)
                continue;

            for (int i = 0; i < neighbors.length; i++) {

                // Skip any edges we don't want to follow
                if(focusIsLiteral && skip[i]) continue;

                neighbor = neighbors[i];
                edge = edges[i];

                // Skip the previous node
                if(neighbor == node.prevNodeID) continue;


                // Add the predicate to the context
                bcv.add(graph.getVertices().size() + getEdgeType(edge), partialWetPaint);

                // Remember which node we came from so we don't go back
                // Remember which predicate we used to get here
                // This way we don't visit nodes that have a different relationship with the literal
                final SemanticNode neighborNode = new SemanticNode(neighbor, edge, focusNode);

                if (nodeQueue.contains(neighborNode)) {
                    wetPaintRegister.add(neighbor, partialWetPaint);
                } else {
                    nodeQueue.add(neighborNode);
                    wetPaintRegister.put(neighbor, partialWetPaint);
                }
            }

		}
		return bcv;
	}
}
