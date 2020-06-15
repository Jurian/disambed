package org.uu.nl.embedding.util;


import grph.in_memory.InMemoryGrph;
import grph.properties.NumericalProperty;

public class InMemoryRdfGraph extends InMemoryGrph {

    private final NumericalProperty edgeTypeProperty, edgeWeightProperty, vertexTypeProperty;

    public InMemoryRdfGraph() {
        super();
        // Support up to 127 different predicate types
        edgeTypeProperty = new NumericalProperty("Edge types",32, 0);
        // Weights are stored as floats
        edgeWeightProperty = new NumericalProperty("Edge weights",32, 0);
        // We only need to support 3 vertex types
        vertexTypeProperty = new NumericalProperty("Vertex types",2, 0);
    }

    public NumericalProperty getEdgeTypeProperty() { return edgeTypeProperty; }

    public NumericalProperty getEdgeWeightProperty() {
        return edgeWeightProperty;
    }

    public NumericalProperty getVertexTypeProperty() {
        return vertexTypeProperty;
    }
}
