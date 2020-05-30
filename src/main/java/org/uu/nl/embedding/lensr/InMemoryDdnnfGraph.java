package org.uu.nl.embedding.lensr;

import grph.in_memory.InMemoryGrph;
import grph.properties.NumericalProperty;

public class InMemoryDdnnfGraph extends InMemoryGrph {

    private final NumericalProperty edgeTypeProperty, vertexTypeProperty;
	
	public InMemoryDdnnfGraph() {

        super();
        // Support up to 127 different predicate types
        edgeTypeProperty = new NumericalProperty("Edge types",8, 0);
        // We only need to support 3 vertex types
        vertexTypeProperty = new NumericalProperty("Vertex types",2, 0);
    }

    public NumericalProperty getEdgeTypeProperty() { return edgeTypeProperty; }

    public NumericalProperty getVertexTypeProperty() {
        return vertexTypeProperty;
	}
}
