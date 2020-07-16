package org.uu.nl.embedding.kale.struct;

public class TripleRule {
	
	private Triple firstTriple = null;
	private Triple secondTriple = null;
	private Triple thirdTriple = null;
	
	public TripleRule(final Triple firstTriple, final Triple secondTriple) {
		this.firstTriple = firstTriple;
		this.secondTriple = secondTriple;
	}
	
	public TripleRule(final Triple firstTriple, final Triple secondTriple, final Triple thirdTriple) {
		this.firstTriple = firstTriple;
		this.secondTriple = secondTriple;
		this.thirdTriple = thirdTriple;
	}
	
	public Triple getFirstTriple() {
		return this.firstTriple;
	}
	
	public Triple getSecondTriple() {
		return this.secondTriple;
	}
	
	public Triple getThirdTriple() {
		return this.thirdTriple;
	}
	
	

}
