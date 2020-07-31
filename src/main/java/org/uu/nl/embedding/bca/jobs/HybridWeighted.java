package org.uu.nl.embedding.bca.jobs;

import org.uu.nl.embedding.bca.util.BCAJob;
import org.uu.nl.embedding.convert.util.NodeInfo;
import org.uu.nl.embedding.util.InMemoryRdfGraph;

/**
 * @author Jurian Baas
 */
public class HybridWeighted extends BCAJob {

	public HybridWeighted(
            InMemoryRdfGraph graph, int bookmark,
            double alpha, double epsilon,
			int[][] vertexIn, int[][] vertexOut, int[][] edgeIn, int[][] edgeOut) {
		super(bookmark, true, alpha, epsilon, graph, "hybirdweighted", vertexOut, vertexIn, edgeOut, edgeIn);
	}

    @Override
    protected int[] getIndexes(boolean reverse, int focusNode, int[][] indexIn, int[][] indexOut) {

        final NodeInfo nodeType = NodeInfo.fromByte((byte) graph.getVertexTypeProperty().getValueAsInt(focusNode));
        int[] index;

        if(nodeType == NodeInfo.LITERAL) {
            index = new int[indexIn[focusNode].length + indexOut[focusNode].length];
            System.arraycopy(indexIn[focusNode], 0, index, 0, indexIn[focusNode].length);
            System.arraycopy(indexOut[focusNode], 0, index, indexIn[focusNode].length, indexOut[focusNode].length);
        } else {
            index = (reverse) ? indexIn[focusNode] : indexOut[focusNode];
        }
        return index;
    }


}
