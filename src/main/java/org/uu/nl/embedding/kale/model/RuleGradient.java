package org.uu.nl.embedding.kale.model;

import org.uu.nl.embedding.kale.struct.TripleMatrix;
import org.uu.nl.embedding.kale.struct.TripleRule;


/**
 * Class imported from iieir-km / KALE on GitHub
 * (https://github.com/iieir-km/KALE/tree/817474edb0da54a76b562bed2328e96284557b87)
 *
 */
public class RuleGradient {
	public TripleRule Rule;
	public TripleRule NegRule;
	public TripleMatrix MatrixE;
	public TripleMatrix MatrixR;
	public TripleMatrix MatrixEGradient;
	public TripleMatrix MatrixRGradient;
	double dDelta;
	double dFstPi;
	double dSndPi;
	double dTrdPi;
	double dNegFstPi;
	double dNegSndPi;
	double dNegTrdPi;
	double costrule;
	double Negcostrule;
	
	public RuleGradient(
			TripleRule inRule,
			TripleRule inNegRule,
			TripleMatrix inMatrixE, 
			TripleMatrix inMatrixR,
			TripleMatrix inMatrixEGradient, 
			TripleMatrix inMatrixRGradient,
			double inDelta) {
		Rule = inRule;
		NegRule = inNegRule;
		MatrixE = inMatrixE;
		MatrixR = inMatrixR;
		MatrixEGradient = inMatrixEGradient;
		MatrixRGradient = inMatrixRGradient;
		dDelta = inDelta;
	}
	
	public void calculateGradient(double weight) throws Exception {
		int iNumberOfFactors = MatrixE.columns();
		int iFstHead = Rule.getFirstTriple().head();
		int iFstTail = Rule.getFirstTriple().tail();
		int iFstRelation = Rule.getFirstTriple().relation();
		int iSndHead = Rule.getSecondTriple().head();
		int iSndTail = Rule.getSecondTriple().tail();
		int iSndRelation = Rule.getSecondTriple().relation();
		
		int iNegFstHead = NegRule.getFirstTriple().head();
		int iNegFstTail = NegRule.getFirstTriple().tail();
		int iNegFstRelation = NegRule.getFirstTriple().relation();
		int iNegSndHead = NegRule.getSecondTriple().head();
		int iNegSndTail = NegRule.getSecondTriple().tail();
		int iNegSndRelation = NegRule.getSecondTriple().relation();
		

		
		double dValue = 1.0 / (3.0 * Math.sqrt(iNumberOfFactors));
		
		if(Rule.getThirdTriple()==null){
			dFstPi = 0.0;
			double dPosPi =0.0;
			for (int p = 0; p < iNumberOfFactors; p++) {
				dFstPi -= Math.abs(MatrixE.get(iFstHead, p) + MatrixR.get(iFstRelation, p) - MatrixE.get(iFstTail, p));
			}
			dFstPi *= dValue;
			dFstPi += 1.0;
			
			dSndPi = 0.0;
			for (int p = 0; p < iNumberOfFactors; p++) {
				dSndPi -= Math.abs(MatrixE.get(iSndHead, p) + MatrixR.get(iSndRelation, p) - MatrixE.get(iSndTail, p));
			}
			dSndPi *= dValue;
			dSndPi += 1.0;
			dPosPi = dFstPi *(dSndPi- 1.0 ) + 1.0;
			
			dNegFstPi = 0.0;
			double dNegPi=0.0;
			for (int p = 0; p < iNumberOfFactors; p++) {
				dNegFstPi -= Math.abs(MatrixE.get(iNegFstHead, p) + MatrixR.get(iNegFstRelation, p) - MatrixE.get(iNegFstTail, p));
			}
			dNegFstPi *= dValue;
			dNegFstPi += 1.0;
			
			dNegSndPi = 0.0;
			for (int p = 0; p < iNumberOfFactors; p++) {
				dNegSndPi -= Math.abs(MatrixE.get(iNegSndHead, p) + MatrixR.get(iNegSndRelation, p) - MatrixE.get(iNegSndTail, p));
			}
			dNegSndPi *= dValue;
			dNegSndPi += 1.0;
			dNegPi = dNegFstPi *(dNegSndPi- 1.0 ) + 1.0;

			if (dDelta - dPosPi + dNegPi > 0.0) {
				for (int p = 0; p < iNumberOfFactors; p++) {
					double dFstSgn = 0.0;
					if (MatrixE.get(iFstHead, p) + MatrixR.get(iFstRelation, p) - MatrixE.get(iFstTail, p) > 0) {
						dFstSgn = 1.0;
					} else if (MatrixE.get(iFstHead, p) + MatrixR.get(iFstRelation, p) - MatrixE.get(iFstTail, p) < 0) {
						dFstSgn = -1.0;
					}
					MatrixEGradient.add(iFstHead, p, weight * (dSndPi-1) * dValue * dFstSgn);
					MatrixEGradient.add(iFstTail, p, -1.0 * weight * (dSndPi-1) * dValue * dFstSgn);
					MatrixRGradient.add(iFstRelation, p, weight * (dSndPi-1) * dValue * dFstSgn);
					
					double dSndSgn = 0.0;
					if (MatrixE.get(iSndHead, p) + MatrixR.get(iSndRelation, p) - MatrixE.get(iSndTail, p) > 0) {
						dSndSgn = 1.0;
					} else if (MatrixE.get(iSndHead, p) + MatrixR.get(iSndRelation, p) - MatrixE.get(iSndTail, p) < 0) {
						dSndSgn = -1.0;
					}
					MatrixEGradient.add(iSndHead, p, weight * dFstPi * dValue * dSndSgn);
					MatrixEGradient.add(iSndTail, p, -1.0 * weight * dFstPi * dValue * dSndSgn);
					MatrixRGradient.add(iSndRelation, p, weight * dFstPi * dValue * dSndSgn);
					
					double dNegFstSgn = 0.0;
					if (MatrixE.get(iNegFstHead, p) + MatrixR.get(iNegFstRelation, p) - MatrixE.get(iNegFstTail, p) > 0) {
						dNegFstSgn = 1.0;
					} else if (MatrixE.get(iNegFstHead, p) + MatrixR.get(iNegFstRelation, p) - MatrixE.get(iNegFstTail, p) < 0) {
						dNegFstSgn = -1.0;
					}
					MatrixEGradient.add(iNegFstHead, p,  -1.0 * weight * (dNegSndPi-1) * dValue * dNegFstSgn);
					MatrixEGradient.add(iNegFstTail, p, weight * (dNegSndPi-1) * dValue * dNegFstSgn);
					MatrixRGradient.add(iNegFstRelation, p,  -1.0 * weight * (dNegSndPi-1) * dValue * dNegFstSgn);
											
					double dNegSndSgn = 0.0;
					if (MatrixE.get(iNegSndHead, p) + MatrixR.get(iNegSndRelation, p) - MatrixE.get(iNegSndTail, p) > 0) {
						dNegSndSgn = 1.0;
					} else if (MatrixE.get(iNegSndHead, p) + MatrixR.get(iNegSndRelation, p) - MatrixE.get(iNegSndTail, p) < 0) {
						dNegSndSgn = -1.0;
					}
					MatrixEGradient.add(iNegSndHead, p, -1.0 * weight * dNegFstPi * dValue * dNegSndSgn);
					MatrixEGradient.add(iNegSndTail, p, weight * dNegFstPi * dValue * dNegSndSgn);
					MatrixRGradient.add(iNegSndRelation, p, -1.0 * weight * dNegFstPi * dValue * dNegSndSgn);

				}
				
			}
		}
//		long rule
		else{
//			System.out.println("#######################long rule");
			int iTrdHead = Rule.getThirdTriple().head();
			int iTrdTail = Rule.getThirdTriple().tail();
			int iTrdRelation = Rule.getThirdTriple().relation();
			
			int iNegTrdHead = NegRule.getThirdTriple().head();
			int iNegTrdTail = NegRule.getThirdTriple().tail();
			int iNegTrdRelation = NegRule.getThirdTriple().relation();
			
			dFstPi = 0.0;
			double dPosPi =0.0;
			for (int p = 0; p < iNumberOfFactors; p++) {
				dFstPi -= Math.abs(MatrixE.get(iFstHead, p) + MatrixR.get(iFstRelation, p) - MatrixE.get(iFstTail, p));
			}
			dFstPi *= dValue;
			dFstPi += 1.0;
			
			dSndPi = 0.0;
			for (int p = 0; p < iNumberOfFactors; p++) {
				dSndPi -= Math.abs(MatrixE.get(iSndHead, p) + MatrixR.get(iSndRelation, p) - MatrixE.get(iSndTail, p));
			}
			dSndPi *= dValue;
			dSndPi += 1.0;
			
			
			dTrdPi = 0.0;
			for (int p = 0; p < iNumberOfFactors; p++) {
				dTrdPi -= Math.abs(MatrixE.get(iTrdHead, p) + MatrixR.get(iTrdRelation, p) - MatrixE.get(iTrdTail, p));
			}
			dTrdPi *= dValue;
			dTrdPi += 1.0;
			dPosPi = (dFstPi * dSndPi) * (dTrdPi - 1.0 ) + 1.0;
			
			dNegFstPi = 0.0;
			double dNegPi=0.0;
			for (int p = 0; p < iNumberOfFactors; p++) {
				dNegFstPi -= Math.abs(MatrixE.get(iNegFstHead, p) + MatrixR.get(iNegFstRelation, p) - MatrixE.get(iNegFstTail, p));
			}
			dNegFstPi *= dValue;
			dNegFstPi += 1.0;
			
			dNegSndPi = 0.0;
			for (int p = 0; p < iNumberOfFactors; p++) {
				dNegSndPi -= Math.abs(MatrixE.get(iNegSndHead, p) + MatrixR.get(iNegSndRelation, p) - MatrixE.get(iNegSndTail, p));
			}
			dNegSndPi *= dValue;
			dNegSndPi += 1.0;
			
			dNegTrdPi = 0.0;
			for (int p = 0; p < iNumberOfFactors; p++) {
				dNegTrdPi -= Math.abs(MatrixE.get(iNegTrdHead, p) + MatrixR.get(iNegTrdRelation, p) - MatrixE.get(iNegTrdTail, p));
			}
			dNegTrdPi *= dValue;
			dNegTrdPi += 1.0;
			
			dNegPi = (dNegFstPi * dNegSndPi) * ( dNegTrdPi - 1.0 ) + 1.0;
			
			if (dDelta - dPosPi + dNegPi > 0.0) {;
				for (int p = 0; p < iNumberOfFactors; p++) {
					double dFstSgn = 0.0;
					if (MatrixE.get(iFstHead, p) + MatrixR.get(iFstRelation, p) - MatrixE.get(iFstTail, p) > 0) {
						dFstSgn = 1.0;
					} else if (MatrixE.get(iFstHead, p) + MatrixR.get(iFstRelation, p) - MatrixE.get(iFstTail, p) < 0) {
						dFstSgn = -1.0;
					}
					MatrixEGradient.add(iFstHead, p, weight * dSndPi * (dTrdPi-1) * dValue * dFstSgn);
					MatrixEGradient.add(iFstTail, p, -1.0 * weight * dSndPi * (dTrdPi-1) * dValue * dFstSgn);
					MatrixRGradient.add(iFstRelation, p, weight * dSndPi * (dTrdPi-1) * dValue * dFstSgn);
					
					double dSndSgn = 0.0;
					if (MatrixE.get(iSndHead, p) + MatrixR.get(iSndRelation, p) - MatrixE.get(iSndTail, p) > 0) {
						dSndSgn = 1.0;
					} else if (MatrixE.get(iSndHead, p) + MatrixR.get(iSndRelation, p) - MatrixE.get(iSndTail, p) < 0) {
						dSndSgn = -1.0;
					}
					MatrixEGradient.add(iSndHead, p, weight * dFstPi * (dTrdPi-1) * dValue * dSndSgn);
					MatrixEGradient.add(iSndTail, p, -1.0 * weight * dFstPi * (dTrdPi-1) * dValue * dSndSgn);
					MatrixRGradient.add(iSndRelation, p, weight * dFstPi * (dTrdPi-1) * dValue * dSndSgn);
					
					double dTrdSgn = 0.0;
					if (MatrixE.get(iTrdHead, p) + MatrixR.get(iTrdRelation, p) - MatrixE.get(iTrdTail, p) > 0) {
						dTrdSgn = 1.0;
					} else if (MatrixE.get(iTrdHead, p) + MatrixR.get(iTrdRelation, p) - MatrixE.get(iTrdTail, p) < 0) {
						dTrdSgn = -1.0;
					}
					MatrixEGradient.add(iTrdHead, p, weight * dFstPi * dSndPi * dValue * dTrdSgn);
					MatrixEGradient.add(iTrdTail, p, -1.0 * weight * dFstPi * dSndPi * dValue * dTrdSgn);
					MatrixRGradient.add(iTrdRelation, p, weight * dFstPi * dSndPi * dValue * dTrdSgn);
					
					double dNegFstSgn = 0.0;
					if (MatrixE.get(iNegFstHead, p) + MatrixR.get(iNegFstRelation, p) - MatrixE.get(iNegFstTail, p) > 0) {
						dNegFstSgn = 1.0;
					} else if (MatrixE.get(iNegFstHead, p) + MatrixR.get(iNegFstRelation, p) - MatrixE.get(iNegFstTail, p) < 0) {
						dNegFstSgn = -1.0;
					}
					MatrixEGradient.add(iNegFstHead, p,  -1.0 * weight * dNegSndPi * ( dNegTrdPi - 1.0 ) * dValue * dNegFstSgn);
					MatrixEGradient.add(iNegFstTail, p, weight * dNegSndPi * ( dNegTrdPi - 1.0 ) * dValue * dNegFstSgn);
					MatrixRGradient.add(iNegFstRelation, p,  -1.0 * weight * dNegSndPi * ( dNegTrdPi - 1.0 ) * dValue * dNegFstSgn);
										
					double dNegSndSgn = 0.0;
					if (MatrixE.get(iNegSndHead, p) + MatrixR.get(iNegSndRelation, p) - MatrixE.get(iNegSndTail, p) > 0) {
						dNegSndSgn = 1.0;
					} else if (MatrixE.get(iNegSndHead, p) + MatrixR.get(iNegSndRelation, p) - MatrixE.get(iNegSndTail, p) < 0) {
						dNegSndSgn = -1.0;
					}
					MatrixEGradient.add(iNegSndHead, p, -1.0 * weight * dNegFstPi * ( dNegTrdPi - 1.0 ) * dValue * dNegSndSgn);
					MatrixEGradient.add(iNegSndTail, p, weight * dNegFstPi * ( dNegTrdPi - 1.0 ) * dValue * dNegSndSgn);
					MatrixRGradient.add(iNegSndRelation, p, -1.0 * weight * dNegFstPi * ( dNegTrdPi - 1.0 ) * dValue * dNegSndSgn);

					double dNegTrdSgn = 0.0;
					if (MatrixE.get(iNegTrdHead, p) + MatrixR.get(iNegTrdRelation, p) - MatrixE.get(iNegTrdTail, p) > 0) {
						dNegTrdSgn = 1.0;
					} else if (MatrixE.get(iNegTrdHead, p) + MatrixR.get(iNegTrdRelation, p) - MatrixE.get(iNegTrdTail, p) < 0) {
						dNegTrdSgn = -1.0;
					}
					MatrixEGradient.add(iNegTrdHead, p, -1.0 * weight * dNegFstPi * dNegSndPi * dValue * dNegTrdSgn);
					MatrixEGradient.add(iNegTrdTail, p, weight * dNegFstPi * dNegSndPi * dValue * dNegTrdSgn);
					MatrixRGradient.add(iNegTrdRelation, p, -1.0 * weight * dNegFstPi * dNegSndPi * dValue * dNegTrdSgn);

				}
				
			}
		}		
	}	
}