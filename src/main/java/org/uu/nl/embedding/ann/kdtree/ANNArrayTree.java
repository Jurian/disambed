package org.uu.nl.embedding.ann.kdtree;

import org.uu.nl.embedding.ann.kdtree.split.SplitPolicy;
import org.uu.nl.embedding.ann.kdtree.util.Distance;
import org.uu.nl.embedding.ann.kdtree.util.HyperRectangle;
import org.uu.nl.embedding.ann.kdtree.util.LimitedPriorityQueue;
import org.uu.nl.embedding.ann.kdtree.util.VectorDistanceTuple;

import java.util.*;

/**
 * Generalized ANN tree structure that splits along a given split policy. This
 * is the base class for implementing KD and RP trees.
 * 
 * This version has been modified to make use of primitive arrays
 * 
 * @author Jurian Baas & Thomas Jungblut (original)
 * @see <a href=
 *      "https://github.com/thomasjungblut/JRPT/blob/master/src/de/jungblut/jrpt/ANNTree.java">Original</a>
 * @param <T> the payload type
 */
public class ANNArrayTree<T> implements ANN<T> {

	public static final class TreeNode<V> {
		final int splitDimension;
		// keyvector by the value in the split dimension
		final double[] keyVector;
		final V value;

		TreeNode<V> left;
		TreeNode<V> right;

		TreeNode(int splitDimension, double[] keyVector, V val) {
			this.splitDimension = splitDimension;
			this.keyVector = keyVector;
			this.value = val;
		}

		double splitValue() {
			return keyVector[splitDimension];
		}

		@Override
		public String toString() {
			return "Node [splitDimension=" + splitDimension + ", value=" + Arrays.toString(keyVector) + "]";
		}
	}

	private final SplitPolicy<T> splitRule;
	private final List<TreeNode<T>> treeNodes = new ArrayList<>();

	private TreeNode<T> root;
	private int size;

	public ANNArrayTree(SplitPolicy<T> splitRule) {
		this.splitRule = splitRule;
	}

	@Override
	public void add(double[] vec, T value) {

		// we always increment size at the beginning, given the guarantee that every
		// add creates a new node in the tree.
		size++;

		// shortcut for empty tree
		if (root == null) {
			root = new TreeNode<>(splitRule.splitDimension(vec, 0), vec, value);
			return;
		}

		TreeNode<T> current = root;
		int level = 0;
		boolean right;
		// traverse the tree to the free spot that matches the dimension
		while (true) {
			right = current.splitValue() <= vec[current.splitDimension];
			TreeNode<T> next = right ? current.right : current.left;
			if (next == null) {
				break;
			} else {
				current = next;
			}
			level++;
		}

		int splitDimension = splitRule.splitDimension(vec, level);

		// do the "real" insert
		// note that current in this case is the parent
		TreeNode<T> n = new TreeNode<>(splitDimension, vec, value);
		treeNodes.add(n);
		if (right) {
			current.right = n;
		} else {
			current.left = n;
		}
	}

	@Override
	public void balance() {

		treeNodes.sort(Comparator.comparingDouble(o -> o.keyVector[o.splitDimension]));

		// do an inverse binary search to build up the tree from the root
		root = fix(treeNodes, 0, treeNodes.size() - 1);
	}

	/**
	 * Fixup the tree recursively by divide and conquering the sorted array.
	 */
	private TreeNode<T> fix(List<TreeNode<T>> nodes, int start, int end) {
		if (start > end) {
			return null;
		} else {
			int mid = (start + end) >>> 1;
			TreeNode<T> midNode = nodes.get(mid);
			midNode.left = fix(nodes, start, mid - 1);
			midNode.right = fix(nodes, mid + 1, end);
			return midNode;
		}
	}

	@Override
	public List<VectorDistanceTuple<T>> rangeQuery(double[] lower, double[] upper) {
		List<VectorDistanceTuple<T>> list = new ArrayList<>();
		List<TreeNode<T>> rangeInternal = rangeInternal(lower, upper);
		for (TreeNode<T> node : rangeInternal) {
			list.add(new VectorDistanceTuple<>(node.keyVector, node.value, 0));
		}
		return list;
	}

	private List<TreeNode<T>> rangeInternal(double[] lower, double[] upper) {
		List<TreeNode<T>> list = new ArrayList<>();
		Deque<TreeNode<T>> toVisit = new ArrayDeque<>();
		toVisit.add(root);
		while (!toVisit.isEmpty()) {
			TreeNode<T> next = toVisit.pop();
			if (strictLower(upper, next.keyVector) && strictHigher(lower, next.keyVector)) {
				list.add(next);
			}

			if (checkSubtree(lower, upper, next.left)) {
				toVisit.add(next.left);
			}
			if (checkSubtree(lower, upper, next.right)) {
				toVisit.add(next.right);
			}
		}
		return list;
	}

	/**
	 * checks if the given node is inside the range based on the split.
	 */
	private boolean checkSubtree(double[] lower, double[] upper, TreeNode<T> next) {
		if (next != null) {
			boolean greater = lower[next.splitDimension] >= next.keyVector[next.splitDimension];
			boolean lower2 = upper[next.splitDimension] >= next.keyVector[next.splitDimension];
			return greater || lower2;
		}
		return false;
	}

	@Override
	public List<VectorDistanceTuple<T>> getNearestNeighbours(double[] vec, int k) {
		return getNearestNeighbours(vec, k, Double.MAX_VALUE);
	}

	@Override
	public List<VectorDistanceTuple<T>> getNearestNeighbours(double[] vec, double radius) {
		return getNearestNeighbours(vec, Integer.MAX_VALUE, radius);
	}

	@Override
	public List<VectorDistanceTuple<T>> getNearestNeighbours(double[] vec, int k, double radius) {
		LimitedPriorityQueue<VectorDistanceTuple<T>> queue = new LimitedPriorityQueue<>(k);
		HyperRectangle hr = HyperRectangle.infiniteHyperRectangle(vec.length);
		getNearestNeighbourInternal(root, vec, hr, radius, k, radius, queue);
		return queue.toList();
	}

	/**
	 * Euclidian distance based recursive algorithm for nearest neighbour queries
	 * based on Andrew W. Moore.
	 */
	private void getNearestNeighbourInternal(TreeNode<T> current, double[] target, HyperRectangle hyperRectangle,
			double maxDistSquared, int k, final double radius, LimitedPriorityQueue<VectorDistanceTuple<T>> queue) {
		if (current == null) {
			return;
		}
		int s = current.splitDimension;
		double[] pivot = current.keyVector;
		double distancePivotToTarget = Distance.euclidean(pivot, target);

		HyperRectangle rightHyperRectangle = new HyperRectangle(
				Arrays.copyOf(hyperRectangle.min, hyperRectangle.min.length),
				Arrays.copyOf(hyperRectangle.max, hyperRectangle.max.length));

		hyperRectangle.max[s] = pivot[s];
		rightHyperRectangle.min[s] = pivot[s];

		boolean left = target[s] > pivot[s];
		TreeNode<T> nearestNode;
		HyperRectangle nearestHyperRectangle;
		TreeNode<T> furtherstNode;
		HyperRectangle furtherstHyperRectangle;
		if (left) {
			nearestNode = current.left;
			nearestHyperRectangle = hyperRectangle;
			furtherstNode = current.right;
			furtherstHyperRectangle = rightHyperRectangle;
		} else {
			nearestNode = current.right;
			nearestHyperRectangle = rightHyperRectangle;
			furtherstNode = current.left;
			furtherstHyperRectangle = hyperRectangle;
		}
		getNearestNeighbourInternal(nearestNode, target, nearestHyperRectangle, maxDistSquared, k, radius, queue);

		double distanceSquared = queue.isFull() ? queue.getMaximumPriority() : Double.MAX_VALUE;
		maxDistSquared = Math.min(maxDistSquared, distanceSquared);
		double[] closest = furtherstHyperRectangle.closestPoint(target);
		double closestDistance = Distance.euclidean(closest, target);
		// check subtrees even if they aren't in your maxDist but within our radius
		if (closestDistance < maxDistSquared || closestDistance < radius) {
			if (distancePivotToTarget < distanceSquared) {
				distanceSquared = distancePivotToTarget > 0d ? distancePivotToTarget : distanceSquared;
				// check if we are within our defined radius
				if (distancePivotToTarget <= radius) {
					queue.add(new VectorDistanceTuple<>(current.keyVector, current.value, distancePivotToTarget),
							distancePivotToTarget);
				}
				maxDistSquared = queue.isFull() ? queue.getMaximumPriority() : Double.MAX_VALUE;
				maxDistSquared = Math.min(maxDistSquared, distanceSquared);
			}
			// now inspect the furthest away node as well
			getNearestNeighbourInternal(furtherstNode, target, furtherstHyperRectangle, maxDistSquared, k, radius,
					queue);
		}
	}

	@Override
	public Iterator<double[]> iterator() {
		return new VectorBFSIterator();
	}

	// iterator for the implementation detail of tree nodes for test cases and
	// additional asserts
	Iterator<TreeNode<T>> iterateNodes() {
		return new BreadthFirstIterator();
	}

	/**
	 * @return the size of the kd-tree.
	 */
	@Override
	public int size() {
		return size;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		prettyPrintIternal(root, sb, 0);
		return sb.toString();
	}

	private void prettyPrintIternal(TreeNode<T> node, StringBuilder sb, int depth) {
		if (node != null) {
			sb.append("\n").append(repeatString(depth));
			sb.append(node.value);
			prettyPrintIternal(node.left, sb, depth + 1);
			prettyPrintIternal(node.right, sb, depth + 1);
		}
	}
	
	private String repeatString(int count) {
		StringBuilder out = new StringBuilder();
		for(int i = 0; i < count; i++) out.append("\t");
		return out.toString();
	}

	private boolean strictHigher(double[] lower, double[] current) {

		for (int i = 0; i < lower.length; i++) {
			if (lower[i] == 0)
				continue;
			if (current[i] < lower[i])
				return false;
		}
		return true;
	}

	private boolean strictLower(double[] upper, double[] current) {

		for (int i = 0; i < upper.length; i++) {
			if (upper[i] == 0)
				continue;
			if (current[i] > upper[i])
				return false;
		}
		return true;

	}

	private final class BreadthFirstIterator implements Iterator<TreeNode<T>> {

		private final Deque<TreeNode<T>> toVisit = new ArrayDeque<>();
		private TreeNode<T> current;

		BreadthFirstIterator() {
			toVisit.add(root);
		}

		@Override
		public boolean hasNext() {
			return toVisit.peek() != null;
		}

		@Override
		public TreeNode<T> next() {
			current = toVisit.poll();
			if (current != null) {
				if (current.left != null) {
					toVisit.add(current.left);
				}
				if (current.right != null) {
					toVisit.add(current.right);
				}
				return current;
			}
			return null;
		}
	}

	private final class VectorBFSIterator implements Iterator<double[]> {

		private final BreadthFirstIterator inOrderIterator;

		VectorBFSIterator() {
			inOrderIterator = new BreadthFirstIterator();
		}

		@Override
		public boolean hasNext() {
			return inOrderIterator.hasNext();
		}

		@Override
		public double[] next() {
			TreeNode<T> next = inOrderIterator.next();
			return next != null ? next.keyVector : null;
		}

	}

}
