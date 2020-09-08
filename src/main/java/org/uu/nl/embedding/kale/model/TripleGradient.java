package org.uu.nl.embedding.kale.model;

import org.uu.nl.embedding.kale.struct.Triple;
import org.uu.nl.embedding.kale.struct.TripleMatrix;


/**
 * Class imported from iieir-km / KALE on GitHub
 * (https://github.com/iieir-km/KALE/tree/817474edb0da54a76b562bed2328e96284557b87)
 *
 */
public class TripleGradient {
	public Triple PosTriple;
	public Triple NegTriple;
	public TripleMatrix MatrixE;
	public TripleMatrix MatrixR;
	public TripleMatrix MatrixEGradient;
	public TripleMatrix MatrixRGradient;
	public double dDelta;
	double dPosPi;
	double dNegPi;
	
	public TripleGradient(
			Triple inPosTriple,
			Triple inNegTriple,
			TripleMatrix inMatrixE, 
			TripleMatrix inMatrixR,
			TripleMatrix inMatrixEGradient, 
			TripleMatrix inMatrixRGradient,
			double inDelta) {
		PosTriple = inPosTriple;
		NegTriple = inNegTriple;
		MatrixE = inMatrixE;
		MatrixR = inMatrixR;
		MatrixEGradient = inMatrixEGradient;
		MatrixRGradient = inMatrixRGradient;
		dDelta = inDelta;
	}
	
	public void calculateGradient() throws Exception {
		int iNumberOfFactors = MatrixE.columns();
		int iPosHead = PosTriple.head();
		int iPosTail = PosTriple.tail();
		int iPosRelation = PosTriple.relation();
		int iNegHead = NegTriple.head();
		int iNegTail = NegTriple.tail();
		int iNegRelation = NegTriple.relation();

		double dValue = 1.0 / (3.0 * Math.sqrt(iNumberOfFactors));
		dPosPi = 0.0;
		for (int p = 0; p < iNumberOfFactors; p++) {
			dPosPi -= Math.abs(MatrixE.get(iPosHead, p) + MatrixR.get(iPosRelation, p) - MatrixE.get(iPosTail, p));
		}
		dPosPi *= dValue;
		dPosPi += 1.0;
		
		dNegPi = 0.0;
		for (int p = 0; p < iNumberOfFactors; p++) {
			dNegPi -= Math.abs(MatrixE.get(iNegHead, p) + MatrixR.get(iNegRelation, p) - MatrixE.get(iNegTail, p));
		}
		dNegPi *= dValue;
		dNegPi += 1.0;
         
		
		if (dDelta - dPosPi + dNegPi > 0.0) {
//		if (dDeltaAdapt - dPosPi + dNegPi > 0.0) {
			for (int p = 0; p < iNumberOfFactors; p++) {
				double dPosSgn = 0.0;
				if (MatrixE.get(iPosHead, p) + MatrixR.get(iPosRelation, p) - MatrixE.get(iPosTail, p) > 0) {
					dPosSgn = 1.0;
				} else if (MatrixE.get(iPosHead, p) + MatrixR.get(iPosRelation, p) - MatrixE.get(iPosTail, p) < 0) {
					dPosSgn = -1.0;
				}
				MatrixEGradient.add(iPosHead, p, dValue * dPosSgn);
				MatrixEGradient.add(iPosTail, p, -1.0 * dValue * dPosSgn);
				MatrixRGradient.add(iPosRelation, p, dValue * dPosSgn);
//				System.out.println("true0:"+  dValue * dPosSgn);
				double dNegSgn = 0.0;
				if (MatrixE.get(iNegHead, p) + MatrixR.get(iNegRelation, p) - MatrixE.get(iNegTail, p) > 0) {
					dNegSgn = 1.0;
				} else if (MatrixE.get(iNegHead, p) + MatrixR.get(iNegRelation, p) - MatrixE.get(iNegTail, p) < 0) {
					dNegSgn = -1.0;
				}
				MatrixEGradient.add(iNegHead, p, -1.0 * dValue * dNegSgn);
				MatrixEGradient.add(iNegTail, p, dValue * dNegSgn);
				MatrixRGradient.add(iNegRelation, p, -1.0 * dValue * dNegSgn);
			}
		}
		
	}
}