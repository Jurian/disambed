package org.uu.nl.embedding.kale.util;

import org.uu.nl.embedding.kale.struct.Triple;
import org.uu.nl.embedding.kale.struct.TripleRule;


/**
 * Class imported from iieir-km / KALE on GitHub
 * (https://github.com/iieir-km/KALE/tree/817474edb0da54a76b562bed2328e96284557b87)
 *
 */
public class NegativeRuleGenerator {
	public TripleRule PositiveRule;
	public int iNumberOfRelations;
	
	public NegativeRuleGenerator(TripleRule inPositiveRule,
			int inNumberOfRelations) {
		PositiveRule = inPositiveRule;
		iNumberOfRelations = inNumberOfRelations;
	}
	
	public TripleRule generateSndNegRule() throws Exception {
		if(PositiveRule.getThirdTriple()==null){
			Triple fstTriple = PositiveRule.getFirstTriple();
			int iSndHead = PositiveRule.getSecondTriple().head();
			int iSndTail = PositiveRule.getSecondTriple().tail();
			int iSndRelation = PositiveRule.getSecondTriple().relation();
			int iFstRelation = PositiveRule.getFirstTriple().relation();
			
			int iNegRelation = iSndRelation;
			Triple sndTriple = new Triple(iSndHead, iSndTail, iNegRelation);
			while (iNegRelation == iSndRelation || iNegRelation == iFstRelation) {
				iNegRelation = (int)(Math.random() * iNumberOfRelations);
				sndTriple = new Triple(iSndHead, iSndTail, iNegRelation);
			}
			TripleRule NegativeRule = new TripleRule(fstTriple, sndTriple);
			return NegativeRule;
		}
		else{
			Triple fstTriple = PositiveRule.getFirstTriple();
			Triple sndTriple = PositiveRule.getSecondTriple();
			int iTrdHead = PositiveRule.getThirdTriple().head();
			int iTrdTail = PositiveRule.getThirdTriple().tail();
			int iTrdRelation = PositiveRule.getThirdTriple().relation();
			int iFstRelation = PositiveRule.getFirstTriple().relation();
			int iSndRelation = PositiveRule.getSecondTriple().relation();
			
			int iNegRelation = iTrdRelation;
			Triple trdTriple = new Triple(iTrdHead, iTrdTail, iNegRelation);
			while (iNegRelation == iTrdRelation || iNegRelation == iSndRelation || iNegRelation == iFstRelation) {
				iNegRelation = (int)(Math.random() * iNumberOfRelations);
				trdTriple = new Triple(iTrdHead, iTrdTail, iNegRelation);
			}
			TripleRule NegativeRule = new TripleRule(fstTriple, sndTriple, trdTriple);
			return NegativeRule;
		}
		
	}
	
	public TripleRule generateFstNegRule() throws Exception {
		Triple sndTriple = PositiveRule.getSecondTriple();
		int ifstHead = PositiveRule.getFirstTriple().head();
		int ifstTail = PositiveRule.getFirstTriple().tail();
		int iFstRelation = PositiveRule.getFirstTriple().relation();
		int iSndRelation = PositiveRule.getSecondTriple().relation();
		
		int iNegRelation = iFstRelation;
		Triple fstTriple = new Triple(ifstHead, ifstTail, iNegRelation);
		while (iNegRelation == iSndRelation || iNegRelation == iFstRelation) {
			iNegRelation = (int)(Math.random() * iNumberOfRelations);
			fstTriple = new Triple(ifstHead, ifstTail, iNegRelation);
		}
		TripleRule NegativeRule = new TripleRule(fstTriple, sndTriple);
		return NegativeRule;
	}
	
}