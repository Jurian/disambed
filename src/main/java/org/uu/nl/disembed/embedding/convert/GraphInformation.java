package org.uu.nl.disembed.embedding.convert;

import com.carrotsearch.hppc.IntHashSet;

public interface GraphInformation {

    int nrOfFocusNodes();
    int nrOfVertices();
    String key(int i);
    IntHashSet focusNodes();
}
