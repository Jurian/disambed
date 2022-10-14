package org.uu.nl.embedding.util;

import com.carrotsearch.hppc.IntHashSet;
import grph.in_memory.InMemoryGrph;
import grph.properties.NumericalProperty;
import grph.properties.Property;
import grph.properties.StringProperty;

import java.io.Closeable;

public class InMemoryRdfGraph extends InMemoryGrph implements Closeable {

    private final IntHashSet focusNodes = new IntHashSet();

    public IntHashSet getFocusNodes() {
        return focusNodes;
    }

    public void addFocusNode(int index) {
        this.focusNodes.add(index);
    }

    private final NumericalProperty edgeTypeProperty, edgeWeightProperty, vertexTypeProperty;
    private final StringProperty literalPredicateProperty;

    /**
     * Estimate RAM usage of this object. Note we don't count Strings at the moment, so it is underestimating.
     * @return Approximate number of 32 numbers used
     */
    public double calculateMemoryMegaBytes() {
        // count edge itself and type and weight
        int edges = getNumberOfEdges() * 3;
        int nodes = getNumberOfVertices();
        // count vertex type bits
        int nodeTypes = nodes*2/32;

        double mb = (edges + nodes + nodeTypes) / 262144d;
        return (double) Math.round(mb * 100) / 100;
    }

    public InMemoryRdfGraph() {
        super();
        // Support up to 127 different predicate types
        edgeTypeProperty = new NumericalProperty("Edge types",32, 0);
        // Weights are stored as floats
        edgeWeightProperty = new NumericalProperty("Edge weights",32, 0);
        // We only need to support 3 vertex types
        vertexTypeProperty = new NumericalProperty("Vertex types",2, 0);

        literalPredicateProperty = new StringProperty("Literal Predicate", getVertices().size());
    }

    public Property getLiteralPredicateProperty() { return literalPredicateProperty; }

    public NumericalProperty getEdgeTypeProperty() { return edgeTypeProperty; }

    public NumericalProperty getEdgeWeightProperty() {
        return edgeWeightProperty;
    }

    public NumericalProperty getVertexTypeProperty() {
        return vertexTypeProperty;
    }

    @Override
    public void close() {
        super.clear();
    }
}
