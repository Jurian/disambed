package org.uu.nl.embedding.util.read;

import java.io.File;

public interface Reader<T> {
    T load(File file);
}
