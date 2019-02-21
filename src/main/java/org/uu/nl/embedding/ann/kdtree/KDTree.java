package org.uu.nl.embedding.ann.kdtree;

import org.uu.nl.embedding.ann.kdtree.split.KDTreeSplitPolicy;

/**
 * Implementation of a kd-tree that handles dense vectors as well as sparse
 * vectors. It offers O(log n) best case lookup time, but can degrade to O(n) if
 * the tree isn't balanced well. It is mostly optimized for special cases like
 * two or three dimensional data.
 * 
 * @author Thomas Jungblut
 * @see <a href="https://github.com/thomasjungblut/JRPT/blob/master/src/de/jungblut/jrpt/KDTree.java">Original</a>
 * @param <T> the payload value type
 */
public final class KDTree<T> extends StaticANNArrayTree<T> {

  public KDTree(double[] data, T[] payloads, int dimension) {
    super(new KDTreeSplitPolicy<T>(), data, payloads, dimension);
  }

}
