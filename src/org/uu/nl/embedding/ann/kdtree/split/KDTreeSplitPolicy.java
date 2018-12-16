package org.uu.nl.embedding.ann.kdtree.split;

import java.util.List;

import org.uu.nl.embedding.ann.kdtree.ANNArrayTree.TreeNode;

/**
 * This version has been modified to make use of primitive arrays and greatly simplified as in the 
 * GloVe use-case we are only faced with dense high dimensional (between 50 and 300) vectors.
 * 
 * @author Jurian Baas & Thomas Jungblut (original)
 * @see <a href="https://github.com/thomasjungblut/JRPT/blob/master/src/de/jungblut/jrpt/rules/KDTreeSplitPolicy.java">Original</a>
 * @param <T> the payload type
 */
public final class KDTreeSplitPolicy<T> implements SplitPolicy<T> {

	@Override
	public int splitDimension(double[] v, int level, List<TreeNode<T>> treeNodes) {
		return (v.length == 1) ? 0 : (level + 1) % v.length;
	}

	@Override
	public int splitDimension(int dimension, int level) {
		return (dimension == 1) ? 0 : (level + 1) % dimension;
	}

}