package org.uu.nl.embedding.util.read;

import java.io.File;

/**
 * @author Jurian Baas
 * @param <T> The type to read
 */
interface Reader<T> {
    T load(File file);
}
