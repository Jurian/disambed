package org.uu.nl.disembed.util.read;

import com.github.jelmerk.knn.hnsw.HnswIndex;
import org.uu.nl.disembed.embedding.opt.Optimizer;

import java.io.File;
import java.io.IOException;

public class HnswIndexReader implements Reader<HnswIndex<String, float[], Optimizer.EmbeddedEntity, Float>> {

    @Override
    public HnswIndex<String, float[], Optimizer.EmbeddedEntity, Float> load(File file) throws IOException {
        return HnswIndex.load(file);
    }
}
