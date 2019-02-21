package org.uu.nl.embedding.ann.kdtree;

import org.uu.nl.embedding.ann.kdtree.split.SplitPolicy;
import org.uu.nl.embedding.ann.kdtree.util.*;

import java.util.*;


/**
 * Generalized ANN tree structure that splits along a given split policy. This
 * is the base class for implementing KD and RP trees.
 * 
 * This version has been modified to make use of primitive arrays and optimized
 * for fast lookups. Adding new elements after creation is currently not supported.
 * 
 * This tree is mainly used to quickly get nearest neighbor value information
 * 
 * @author Jurian Baas & Thomas Jungblut (original)
 * @see <a href=
 *      "https://github.com/thomasjungblut/JRPT/blob/master/src/de/jungblut/jrpt/ANNTree.java">Original</a>
 * @param <T> the payload type
 */
public class StaticANNArrayTree<T> implements ANN<T>, ANNValueSearch<T> {

	/**
	 * Each node references the data array for coordinate information
	 * and stores only the value
	 * @author Jurian Baas
	 *
	 * @param <V>
	 */
	public final class TreeNode<V> {
		
		final int splitDimension, index, splitIndex, dataIndex;
		final V value;

		TreeNode<V> left;
		TreeNode<V> right;

		public TreeNode(int index, int splitDimension, V val) {
			this.splitDimension = splitDimension;
			this.value = val;
			this.index = index;
			this.splitIndex = index * dimension + splitDimension;
			this.dataIndex = index * dimension;
		}
		
		/**
		 * Extract the coordinates for this node. 
		 * Even though optimized, this is still an expensive operation. Use sparingly!
		 * @return The coordinate information
		 */
		public double[] keyVector() {
			double[] vec = new double[dimension];
			System.arraycopy(data, index * dimension, vec, 0, dimension);
			return vec;
		}
		
		public int dataIndex() {
			return dataIndex;
		}

		public double splitValue() {
			return data[splitIndex];
		}

		@Override
		public String toString() {
			return "Node [splitDimension=" + splitDimension + ", value=" + value.toString() + "]";
		}
	}

	private final SplitPolicy<T> splitRule;
	private final List<TreeNode<T>> treeNodes = new ArrayList<>();

	private TreeNode<T> root;
	private int size;
	private final int dimension;
	private final double[] data;

	public StaticANNArrayTree(SplitPolicy<T> splitRule, double[] data, T[] payloads, int dimension) {
		this.splitRule = splitRule;
		this.data = data;
		this.dimension = dimension;
		this.root = new TreeNode<T>(0, splitRule.splitDimension(dimension, 0), payloads[0]);
		this.treeNodes.add(root);
		this.size++;
		build(payloads);
		balance();
	}
	
	private void build(T[] payloads) {
		
		TreeNode<T> parent = root, child;
		int level = 0;
		boolean right;
		
		for(int i = 1; i < payloads.length; i++) {
			// we always increment size at the beginning, given the guarantee that every
			// add creates a new node in the tree.
			size++;

			// traverse the tree to the free spot that matches the dimension
			do {
				right = parent.splitValue() <= data[i * dimension + parent.splitDimension];
				child = right ? parent.right : parent.left;
				parent = child == null ? parent : child;
				level++;
			} while(child != null);
			
			// do the "real" insert
			// note that current in this case is the parent
			child = new TreeNode<T>(i, splitRule.splitDimension(dimension, level), payloads[i]);
			treeNodes.add(child);
			if (right) {
				parent.right = child;
			} else {
				parent.left = child;
			}
		}
	}

	@Override
	public void add(double[] vec, T value) {}

	@Override
	public void balance() {
		Collections.sort(treeNodes, (o1, o2) -> Double.compare(o1.splitValue(), o2.splitValue()));
		// do an inverse binary search to build up the tree from the root
		root = fix(treeNodes, 0, treeNodes.size() - 1);
	}

	/**
	 * Fix the tree recursively by divide and conquering the sorted array.
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
	public List<ValueDistanceTuple<T>> getValueNearestNeighbours(double[] vec, int k) {
		return getValueNearestNeighbours(vec, k, Double.MAX_VALUE);
	}

	@Override
	public List<ValueDistanceTuple<T>> getValueNearestNeighbours(double[] vec, double radius) {
		return getValueNearestNeighbours(vec, Integer.MAX_VALUE, radius);
	}

	@Override
	public List<ValueDistanceTuple<T>> getValueNearestNeighbours(double[] vec, int k, double radius) {
		LimitedPriorityQueue<ValueDistanceTuple<T>> queue = new LimitedPriorityQueue<>(k);
		HyperRectangle hr = HyperRectangle.infiniteHyperRectangle(vec.length);
		getValueNearestNeighbourInternal(root, vec, hr, radius, k, radius, queue);
		return queue.toList();
	}

	@Override
	public List<ValueDistanceTuple<T>> rangeValueQuery(double[] lower, double[] upper) {
		List<ValueDistanceTuple<T>> list = new ArrayList<>();
		List<TreeNode<T>> rangeInternal = rangeInternal(lower, upper);
		for (TreeNode<T> node : rangeInternal) {
			list.add(new ValueDistanceTuple<T>(node.value, 0));
		}
		return list;
	}
	
	
	
	@Override
	public List<VectorDistanceTuple<T>> rangeQuery(double[] lower, double[] upper) {
		List<VectorDistanceTuple<T>> list = new ArrayList<>();
		List<TreeNode<T>> rangeInternal = rangeInternal(lower, upper);
		for (TreeNode<T> node : rangeInternal) {
			list.add(new VectorDistanceTuple<T>(node.keyVector(), node.value, 0));
		}
		return list;
	}

	private List<TreeNode<T>> rangeInternal(double[] lower, double[] upper) {
		List<TreeNode<T>> list = new ArrayList<>();
		Deque<TreeNode<T>> toVisit = new ArrayDeque<>();
		toVisit.add(root);
		while (!toVisit.isEmpty()) {
			TreeNode<T> next = toVisit.pop();
			
			if (strictLower(upper, next.dataIndex()) && strictHigher(lower, next.dataIndex())) 
				list.add(next);
			if (next.left != null && checkSubtree(lower, upper, next.left)) 
				toVisit.add(next.left);
			if (next.right != null && checkSubtree(lower, upper, next.right)) 
				toVisit.add(next.right);
		}
		return list;
	}

	/**
	 * checks if the given node is inside the range based on the split.
	 */
	private boolean checkSubtree(double[] lower, double[] upper, TreeNode<T> next) {
		if (next != null) {
			boolean greater = lower[next.splitDimension] >= next.splitValue();
			boolean lower2 = upper[next.splitDimension] >= next.splitValue();
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
		
		if (current == null) 
			return;
		
		int s = current.splitDimension;
		double pivot = current.splitValue();
		double distancePivotToTarget = Distance.euclidean(data, dimension, current.index, target);

		HyperRectangle leftHyperRectangle = hyperRectangle;
		HyperRectangle rightHyperRectangle = leftHyperRectangle.copy();

		leftHyperRectangle.max[s] = pivot;
		rightHyperRectangle.min[s] = pivot;

		boolean left = target[s] > pivot;
		TreeNode<T> nearestNode;
		HyperRectangle nearestHyperRectangle;
		TreeNode<T> furtherstNode;
		HyperRectangle furtherstHyperRectangle;
		if (left) {
			nearestNode = current.left;
			nearestHyperRectangle = leftHyperRectangle;
			furtherstNode = current.right;
			furtherstHyperRectangle = rightHyperRectangle;
		} else {
			nearestNode = current.right;
			nearestHyperRectangle = rightHyperRectangle;
			furtherstNode = current.left;
			furtherstHyperRectangle = leftHyperRectangle;
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
					queue.add(new VectorDistanceTuple<>(current.keyVector(), current.value, distancePivotToTarget), distancePivotToTarget);
				}
				maxDistSquared = queue.isFull() ? queue.getMaximumPriority() : Double.MAX_VALUE;
				maxDistSquared = Math.min(maxDistSquared, distanceSquared);
			}
			// now inspect the furthest away node as well
			getNearestNeighbourInternal(furtherstNode, target, furtherstHyperRectangle, maxDistSquared, k, radius, queue);
		}
	}
	
	/**
	 * Euclidian distance based recursive algorithm for nearest neighbour queries
	 * based on Andrew W. Moore.
	 */
	private void getValueNearestNeighbourInternal(TreeNode<T> current, double[] target, HyperRectangle hyperRectangle,
			double maxDistSquared, int k, final double radius, LimitedPriorityQueue<ValueDistanceTuple<T>> queue) {
		
		if (current == null) 
			return;
		
		int s = current.splitDimension;
		double pivot = current.splitValue();
		double distancePivotToTarget = Distance.euclidean(data, dimension, current.index, target);

		HyperRectangle leftHyperRectangle = hyperRectangle;
		HyperRectangle rightHyperRectangle = leftHyperRectangle.copy();

		leftHyperRectangle.max[s] = pivot;
		rightHyperRectangle.min[s] = pivot;

		boolean left = target[s] > pivot;
		TreeNode<T> nearestNode;
		HyperRectangle nearestHyperRectangle;
		TreeNode<T> furtherstNode;
		HyperRectangle furtherstHyperRectangle;
		if (left) {
			nearestNode = current.left;
			nearestHyperRectangle = leftHyperRectangle;
			furtherstNode = current.right;
			furtherstHyperRectangle = rightHyperRectangle;
		} else {
			nearestNode = current.right;
			nearestHyperRectangle = rightHyperRectangle;
			furtherstNode = current.left;
			furtherstHyperRectangle = leftHyperRectangle;
		}
		getValueNearestNeighbourInternal(nearestNode, target, nearestHyperRectangle, maxDistSquared, k, radius, queue);

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
					queue.add(new ValueDistanceTuple<>(current.value, distancePivotToTarget), distancePivotToTarget);
				}
				maxDistSquared = queue.isFull() ? queue.getMaximumPriority() : Double.MAX_VALUE;
				maxDistSquared = Math.min(maxDistSquared, distanceSquared);
			}
			// now inspect the furthest away node as well
			getValueNearestNeighbourInternal(furtherstNode, target, furtherstHyperRectangle, maxDistSquared, k, radius, queue);
		}
	}

	@Override
	public Iterator<double[]> iterator() {
		return new VectorBFSIterator();
	}

	// iterator for the implementation detail of tree nodes for test cases and
	// additional asserts
	public Iterator<TreeNode<T>> iterateNodes() {
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

	private StringBuilder prettyPrintIternal(TreeNode<T> node, StringBuilder sb, int depth) {
		if (node != null) {
			sb.append("\n").append(repeatString("\t", depth));
			sb.append(node.value);
			prettyPrintIternal(node.left, sb, depth + 1);
			prettyPrintIternal(node.right, sb, depth + 1);
		}
		return sb;
	}
	
	private String repeatString(String s, int count) {
		String out = "";
		for(int i = 0; i < count; i++) out += s;
		return out;
	}

	private boolean strictHigher(double[] lower, int current) {

		for (int i = 0; i < lower.length; i++) {
			if (lower[i] == 0)
				continue;
			if (data[current + i] < lower[i])
				return false;
		}
		return true;
	}

	private boolean strictLower(double[] upper, int current) {

		for (int i = 0; i < upper.length; i++) {
			if (upper[i] == 0)
				continue;
			if (data[current + i] > upper[i])
				return false;
		}
		return true;

	}

	private final class BreadthFirstIterator implements Iterator<TreeNode<T>> {

		private final Deque<TreeNode<T>> toVisit = new ArrayDeque<>();
		private TreeNode<T> current;

		public BreadthFirstIterator() {
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

		private BreadthFirstIterator inOrderIterator;

		public VectorBFSIterator() {
			inOrderIterator = new BreadthFirstIterator();
		}

		@Override
		public boolean hasNext() {
			return inOrderIterator.hasNext();
		}

		@Override
		public double[] next() {
			TreeNode<T> next = inOrderIterator.next();
			return next != null ? next.keyVector() : null;
		}

	}

}
