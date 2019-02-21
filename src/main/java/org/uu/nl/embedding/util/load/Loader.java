package org.uu.nl.embedding.util.load;

import java.io.File;

public interface Loader<T> {
    T load(File file);
}
