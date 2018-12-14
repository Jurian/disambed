package org.uu.nl.ann.kdtree.split;

import java.util.List;

import org.uu.nl.ann.kdtree.ANNArrayTree.TreeNode;



/**
 * This version has been modified to make use of primitive arrays
 * 
 * @author Jurian Baas & Thomas Jungblut (original)
 * @see <a href="https://github.com/thomasjungblut/JRPT/blob/master/src/de/jungblut/jrpt/rules/SplitPolicy.java">Original</a>
 * @param <T> the payload type
 */
public interface SplitPolicy<T> {

  /**
   * Choose a split dimension for the given vector and tree level.
   * 
   * @param v the vector.
   * @param level the tree level.
   * @param treeNodes the tree nodes that were added so far.
   * @return an index of the dimension between 0 and v.getDimension()
   *         (exclusive).
   */
  public int splitDimension(double[] v, int level, List<TreeNode<T>> treeNodes);

  public int splitDimension(int dimension, int level);
  
}