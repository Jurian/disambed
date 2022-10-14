package org.uu.nl.embedding.util.read;

import java.io.File;
import java.io.IOException;

/**
 * @author Jurian Baas
 * @param <T> The type to read
 */
public interface Reader<T> {
    T load(File file) throws IOException;
}
