package org.uu.nl.embedding.progress;

public class Progress {

	private int index;
	private String type;
	private long n;
	private double value;

	private boolean finished;

	public Progress() {}

	public Progress(ProgressType type) {
		this(type, false);
	}
	
	public Progress(ProgressType type, boolean finished) {
		this.index = type.toIndex();
		this.type = type.name();
		this.finished = finished;
	}

	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public String getType() { return type; }
	public void setType(String type) {
		this.type = type;
	}
	public double getValue() {
		return value;
	}
	public void setValue(double value) {
		this.value = value;
	}
	public boolean isFinished() {return finished; }
	public void setFinished(boolean finished) {this.finished = finished;}

	@Override
	public String toString() {
		return String.format("Embedding progress [index=%d type=%s value=%f done=%b]",
				 index, type, value, finished);
	}

	public long getN() {
		return n;
	}

	public void setN(long n) {
		this.n = n;
	}
}
