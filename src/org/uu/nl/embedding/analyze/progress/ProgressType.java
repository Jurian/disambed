package org.uu.nl.embedding.analyze.progress;

public enum ProgressType {
	
	BCA(1), 
	GLOVE(2);
	
	final int index;
	
	private ProgressType(int index){
		this.index = index;
	}
	
	public int toIndex() {
		return this.index;
	}
}
