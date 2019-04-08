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
public class VanillaBCAJob extends BCAJob {

	private final int[][] vertexOut, vertexIn, edgeOut, edgeIn;
	
	public VanillaBCAJob(Grph graph,
                         int bookmark, boolean reverse, double alpha, double epsilon,
                         int[][] vertexIn, int[][] vertexOut, int[][] edgeIn, int[][] edgeOut) {
		super(bookmark, reverse, alpha, epsilon, graph);
		this.vertexOut = vertexOut;
		this.vertexIn = vertexIn;
		this.edgeOut = edgeOut;
		this.edgeIn = edgeIn;
	}

	@Override
	protected BCV doWork(Grph graph, boolean reverse) {
		
		final Queue<Integer> nodeQueue = new LinkedList<>();
		final PaintRegistry<Integer> wetPaintRegister = new PaintRegistry<>();
		final BCV bcv = new BCV(bookmark);
		
		nodeQueue.add(bookmark);
		wetPaintRegister.put(bookmark, 1d);
		
		int[] neighbors, edges;
		int focusNode, edge, neighbor;
		double partialWetPaint;

		
		while (!nodeQueue.isEmpty()) {

			focusNode = nodeQueue.poll();
			final double wetPaint = wetPaintRegister.get(focusNode);

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

                neighbor = neighbors[i];
                edge = edges[i];

                // Add the predicate to the context
                bcv.add(graph.getVertices().size() + getEdgeType(edge), partialWetPaint);

                if (nodeQueue.contains(neighbor)) {
                    wetPaintRegister.add(neighbor, partialWetPaint);
                } else {
                    nodeQueue.add(neighbor);
                    wetPaintRegister.put(neighbor, partialWetPaint);
                }
            }
		}
		return bcv;
	}
}
