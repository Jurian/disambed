package org.uu.nl.embedding.bca;

import grph.Grph;
import org.uu.nl.embedding.bca.util.BCAJob;
import org.uu.nl.embedding.bca.util.BCV;
import org.uu.nl.embedding.bca.util.PaintRegistry;

import java.util.LinkedList;
import java.util.Queue;

public class SemanticBCAJob extends BCAJob {

	private static final int SKIP = -1;

	private final int[][] in, out;
	public SemanticBCAJob(
			Grph graph, int bookmark,
			boolean reverse, double alpha, double epsilon,
			int[][] in, int[][] out) {
		super(bookmark, reverse, alpha, epsilon, graph);
		this.in = in;
		this.out = out;
	}
	
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

		int[] neighbors, edges, edgeCache;
		int focusNode, neighbor, predicate, edge, neighborCount, ignoredEdgeCount;
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

            // In the case of a literal node, we follow incoming relationships of the same
            // type as the one that was used to reach the literal node
            if(isLiteral(focusNode)) {
                neighbors = in[focusNode];
                edges = new int[neighbors.length];
                // If the bookmark is a literal, the previous predicate ID will be SKIP
                predicate = node.predicateID == SKIP ? SKIP : getEdgeType(node.predicateID);
                // Cache edges and reuse in loop
                edgeCache = graph.getInOnlyEdges(focusNode).toIntArray();

                // Check which edges we need to follow and which to ignore
                for(int i = 0; i < neighbors.length; i++) {

                    edges[i] = getEdge(neighbors[i], focusNode, graph.getOutEdges(neighbors[i]).toIntArray(), edgeCache);
                    // In case the predicate is SKIP, we consider all edges
                    // otherwise...
                    if(predicate != SKIP && getEdgeType(edges[i]) != predicate) {
                        edges[i] = SKIP;
                        ignoredEdgeCount++;
                    }
                }
            } else {
                // At this point the focus node is not a literal

                if(reverse) {
                    neighbors = new int[in[focusNode].length + out[focusNode].length];
                    // Use all incoming neighbors
                    System.arraycopy(in[focusNode], 0, neighbors, 0, in[focusNode].length);

                    // Also use outgoing neighbors if they're literal nodes
                    for(int i = 0 ; i < out[focusNode].length; i++) {
                        if(isLiteral(out[focusNode][i]))
                            neighbors[i + in[focusNode].length] = out[focusNode][i];
                        else
                            neighbors[i + in[focusNode].length] = SKIP;
                    }

                    edgeCache = graph.getInOnlyEdges(focusNode).toIntArray();
                    edges = new int[neighbors.length];

                    for(int i = 0; i < neighbors.length; i++)
                        edges[i] = getEdge(neighbors[i], focusNode, graph.getInEdges(neighbors[i]).toIntArray(), edgeCache);

                } else {
                    // Simply follow outgoing nodes
                    neighbors = out[focusNode];
                    edgeCache = graph.getOutOnlyEdges(focusNode).toIntArray();
                    edges = new int[neighbors.length];

                    for(int i = 0; i < neighbors.length; i++)
                        edges[i] = getEdge(focusNode, neighbors[i], edgeCache, graph.getInEdges(neighbors[i]).toIntArray());
                }
            }


            neighborCount = neighbors.length - ignoredEdgeCount;
            if(neighborCount == 0)
                continue;

            partialWetPaint = (1 - alpha) * wetPaint / neighborCount;
            // We can already tell that the neighbors will not have enough paint to continue
            if(partialWetPaint < epsilon)
                continue;

            for (int i = 0; i < neighbors.length; i++) {

                neighbor = neighbors[i];
                edge = edges[i];

                if(neighbor == node.prevNodeID || edge == SKIP) continue;

                bcv.add(graph.getVertices().size() + getEdgeType(edge), partialWetPaint);

                // Remember which node we came from so we don't go back
                // Remember which predicate we used to get here
                // This way we don't visit nodes that have a different relationship with the literal
                SemanticNode neighborNode = new SemanticNode(neighbor, edge, focusNode);

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
