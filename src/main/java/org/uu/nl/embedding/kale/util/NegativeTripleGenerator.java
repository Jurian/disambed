package org.uu.nl.embedding.kale.util;

import org.uu.nl.embedding.kale.struct.Triple;


/**
 * Class imported from iieir-km / KALE on GitHub
 * (https://github.com/iieir-km/KALE/tree/817474edb0da54a76b562bed2328e96284557b87)
 *
 */
public class NegativeTripleGenerator {
	public Triple PositiveTriple;
	public int iNumberOfEntities;
	public int iNumberOfRelation;
	
	public NegativeTripleGenerator(Triple inPositiveTriple,
			int inNumberOfEntities, int inNumberOfRelation) {
		PositiveTriple = inPositiveTriple;
		iNumberOfEntities = inNumberOfEntities;
		iNumberOfRelation = inNumberOfRelation;
	}
	
	public Triple generateHeadNegTriple() throws Exception {
		int iPosHead = PositiveTriple.head();
		int iPosTail = PositiveTriple.tail();
		int iPosRelation = PositiveTriple.relation();
		
		int iNegHead = iPosHead;
		Triple NegativeTriple = new Triple(iNegHead, iPosTail, iPosRelation);
		while (iNegHead == iPosHead) {
			iNegHead = (int)(Math.random() * iNumberOfEntities);
			NegativeTriple = new Triple(iNegHead, iPosTail, iPosRelation);
		}
		return NegativeTriple;
	}
	
	public Triple generateTailNegTriple() throws Exception {
		int iPosHead = PositiveTriple.head();
		int iPosTail = PositiveTriple.tail();
		int iPosRelation = PositiveTriple.relation();
		
		int iNegTail = iPosTail;
		Triple NegativeTriple = new Triple(iPosHead, iNegTail, iPosRelation);
		while (iNegTail == iPosTail) {
			iNegTail = (int)(Math.random() * iNumberOfEntities);
			NegativeTriple = new Triple(iPosHead, iNegTail, iPosRelation);
		}
		return NegativeTriple;
	}
	
	public Triple generateRelNegTriple() throws Exception {
		int iPosHead = PositiveTriple.head();
		int iPosTail = PositiveTriple.tail();
		int iPosRelation = PositiveTriple.relation();
		
		int iNegRelation = iPosRelation;
		Triple NegativeTriple = new Triple(iPosHead, iPosTail, iNegRelation);
		while (iNegRelation == iPosRelation) {
			iNegRelation = (int)(Math.random() * iNumberOfRelation);
			NegativeTriple = new Triple(iPosHead, iPosTail, iNegRelation);
		}
		return NegativeTriple;
	}
}