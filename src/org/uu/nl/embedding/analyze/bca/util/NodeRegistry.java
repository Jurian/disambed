package org.uu.nl.embedding.analyze.bca.util;

/**
 * A more time-efficient way of remembering which nodes to traverse in Semantic
 * BCA. In the registry, value of 0 means no value, while positive integers mean
 * a node has to be traversed (or not) for the node that corresponds to the
 * index.
 * 
 * @author Jurian Baas
 *
 */
public class NodeRegistry {

	private final long[] registry;

	public NodeRegistry(int nrOfNodes) {
		this.registry = new long[nrOfNodes];
	}

	private long get(int nodeIndex) {
		return registry[nodeIndex];
	}

	public int getFirst(int nodeIndex) {
		return (int) (get(nodeIndex) >> Integer.SIZE) - 1;
	}
	
	public int getSecond(int nodeIndex) {
		return (int) get(nodeIndex) - 1;
	}

	private void set(int nodeIndex, long value) {
		registry[nodeIndex] = value;
	}
	
	public void setFirst(int nodeIndex, int value) {
		set(nodeIndex,  ++value << Integer.SIZE | (int) get(nodeIndex) & 0xFFFFFFFFL);
	}

	public void setSecond(int nodeIndex, int value) {
		set(nodeIndex, get(nodeIndex) << Integer.SIZE | (++value & 0xFFFFFFFFL));
	}
	
	public void clear(int nodeIndex) {
		set(nodeIndex, 0);
	}
	
	public void clearFirst(int nodeIndex) {
		setFirst(nodeIndex, -1);
	}
	
	public void clearSecond(int nodeIndex) {
		setSecond(nodeIndex, -1);
	}
	
	public String toString() {
		return java.util.Arrays.toString(registry);
	}

}
