package org.uu.nl.embedding.kale.struct;

public class Triple {

	private int iEntity;
	private int kRelation;
	private int jEntity;
	
	public Triple(final int ei, final int rk, final int ej) {
		this.iEntity = ei;
		this.kRelation = rk;
		this.jEntity = ej;
	}
	
	public int head() {
		return this.iEntity;
	}
	
	public int relation() {
		return this.kRelation;
	}
	
	public int tail() {
		return this.jEntity;
	}
}
