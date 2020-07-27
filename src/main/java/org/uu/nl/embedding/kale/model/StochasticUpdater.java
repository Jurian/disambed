package org.uu.nl.embedding.kale.model;

import java.util.ArrayList;

import org.uu.nl.embedding.kale.struct.Triple;
import org.uu.nl.embedding.kale.struct.TripleSet;
import org.uu.nl.embedding.kale.struct.TripleRule;
import org.uu.nl.embedding.kale.struct.KaleMatrix;


/**
 * Class imported from iieir-km / KALE on GitHub
 * (https://github.com/iieir-km/KALE/tree/817474edb0da54a76b562bed2328e96284557b87)
 *
 */
public class StochasticUpdater {
	public ArrayList<Triple> lstPosTriples;
	public ArrayList<Triple> lstHeadNegTriples;
	public ArrayList<Triple> lstTailNegTriples;
	public ArrayList<TripleRule> lstRules;
//	public ArrayList<Rule> lstFstRelNegRules;
	public ArrayList<TripleRule> lstSndRelNegRules;

	public KaleMatrix MatrixE;
	public KaleMatrix MatrixR;
	public KaleMatrix MatrixEGradient;
	public KaleMatrix MatrixRGradient;
	public double dGammaE;
	public double dGammaR;
	public double dDelta;
	public double m_Weight;
	boolean isGlove;
	
	public StochasticUpdater(
			ArrayList<Triple> inLstPosTriples,
			ArrayList<Triple> inLstHeadNegTriples,
			ArrayList<Triple> inLstTailNegTriples,
			ArrayList<TripleRule> inlstRules,
			ArrayList<TripleRule> inlstSndRelNegRules,
			KaleMatrix inMatrixE, 
			KaleMatrix inMatrixR,
			KaleMatrix inMatrixEGradient, 
			KaleMatrix inMatrixRGradient,
			double inGammaE,
			double inGammaR,
			double inDelta,
			double in_m_Weight,
			final boolean isGlove) {
		lstPosTriples = inLstPosTriples;
		lstHeadNegTriples = inLstHeadNegTriples;
		lstTailNegTriples = inLstTailNegTriples;
		lstRules = inlstRules;
		lstSndRelNegRules = inlstSndRelNegRules;
		MatrixE = inMatrixE;
		MatrixR = inMatrixR;
		MatrixEGradient = inMatrixEGradient;
		MatrixRGradient = inMatrixRGradient;
		m_Weight = in_m_Weight;
		dGammaE = inGammaE;
		dGammaR = inGammaR;
		dDelta = inDelta;
		this.isGlove = isGlove;
	}
	
	/**
	 * 
	 * @throws Exception
	 * @author Euan Westenbroek
	 */
	public void stochasticIteration() throws Exception {
		if (this.isGlove) stochasticIterationGlove();
		else stochasticIterationDefault();
	}
	
	/**
	 * 
	 * @throws Exception
	 * @author Euan Westenbroek
	 */
	public void stochasticIterationGlove() throws Exception {
		this.MatrixEGradient.setToValue(0.0);
		this.MatrixRGradient.setToValue(0.0);

		// Loop through positive triples and calculate
		// gradients.
		for (int iID = 0; iID < this.lstPosTriples.size(); iID++) {
			Triple PosTriple = this.lstPosTriples.get(iID);
			Triple HeadNegTriple = this.lstHeadNegTriples.get(iID);
			Triple TailNegTriple = this.lstTailNegTriples.get(iID);
			
			TripleGradient headGradient = new TripleGradient(
					PosTriple,
					HeadNegTriple,
					this.MatrixE,
					this.MatrixR,
					this.MatrixEGradient,
					this.MatrixRGradient,
					this.dDelta,
					this.isGlove);
			headGradient.calculateGradient();

			TripleGradient tailGradient = new TripleGradient(
					PosTriple,
					TailNegTriple,
					MatrixE,
					MatrixR,
					MatrixEGradient,
					MatrixRGradient,
					dDelta,
					this.isGlove);
			tailGradient.calculateGradient();
		}

		for (int iID = 0; iID < lstRules.size(); iID++) {
			TripleRule rule = lstRules.get(iID);
			TripleRule sndRelNegrule = lstSndRelNegRules.get(iID);
			
			RuleGradient tailruleGradient = new RuleGradient(
					rule,
					sndRelNegrule,
					MatrixE,
					MatrixR,
					MatrixEGradient,
					MatrixRGradient,
					dDelta,
					this.isGlove);
			tailruleGradient.calculateGradient(m_Weight);	
		}
		
		MatrixEGradient.rescaleByRow();
		MatrixRGradient.rescaleByRow();
		
		for (int i = 0; i < MatrixE.rows(); i++) {
			for (int j = 0; j < MatrixE.columns(); j++) {
				double dValue = MatrixEGradient.get(i, j);
				MatrixEGradient.accumulatedByGrad(i, j);
				double dLrate = Math.sqrt(MatrixEGradient.getSum(i, j)) + 1e-8;
				MatrixE.add(i, j, -1.0 * dGammaE * dValue / dLrate);
			}
		}
		for (int i = 0; i < MatrixR.rows(); i++) {
			for (int j = 0; j < MatrixR.columns(); j++) {
				double dValue = MatrixRGradient.get(i, j);
				MatrixRGradient.accumulatedByGrad(i, j);
				double dLrate = Math.sqrt(MatrixRGradient.getSum(i, j)) + 1e-8;
				MatrixR.add(i, j, -1.0 * dGammaR * dValue / dLrate);
			}
		}
		MatrixE.normalizeByRow();
		MatrixR.normalizeByRow();
	}
	
	public void stochasticIterationDefault() throws Exception {
		MatrixEGradient.setToValue(0.0);
		MatrixRGradient.setToValue(0.0);


		for (int iID = 0; iID < lstPosTriples.size(); iID++) {
			Triple PosTriple = lstPosTriples.get(iID);
			Triple HeadNegTriple = lstHeadNegTriples.get(iID);
			Triple TailNegTriple = lstTailNegTriples.get(iID);
			
			TripleGradient headGradient = new TripleGradient(
					PosTriple,
					HeadNegTriple,
					MatrixE,
					MatrixR,
					MatrixEGradient,
					MatrixRGradient,
					dDelta,
					this.isGlove);
			headGradient.calculateGradient();

			TripleGradient tailGradient = new TripleGradient(
					PosTriple,
					TailNegTriple,
					MatrixE,
					MatrixR,
					MatrixEGradient,
					MatrixRGradient,
					dDelta,
					this.isGlove);
			tailGradient.calculateGradient();
		}

		for (int iID = 0; iID < lstRules.size(); iID++) {
			TripleRule rule = lstRules.get(iID);
			TripleRule sndRelNegrule = lstSndRelNegRules.get(iID);
			
			RuleGradient tailruleGradient = new RuleGradient(
					rule,
					sndRelNegrule,
					MatrixE,
					MatrixR,
					MatrixEGradient,
					MatrixRGradient,
					dDelta,
					this.isGlove);
			tailruleGradient.calculateGradient(m_Weight);	
		}
		
		MatrixEGradient.rescaleByRow();
		MatrixRGradient.rescaleByRow();
		
		for (int i = 0; i < MatrixE.rows(); i++) {
			for (int j = 0; j < MatrixE.columns(); j++) {
				double dValue = MatrixEGradient.get(i, j);
				MatrixEGradient.accumulatedByGrad(i, j);
				double dLrate = Math.sqrt(MatrixEGradient.getSum(i, j)) + 1e-8;
				MatrixE.add(i, j, -1.0 * dGammaE * dValue / dLrate);
			}
		}
		for (int i = 0; i < MatrixR.rows(); i++) {
			for (int j = 0; j < MatrixR.columns(); j++) {
				double dValue = MatrixRGradient.get(i, j);
				MatrixRGradient.accumulatedByGrad(i, j);
				double dLrate = Math.sqrt(MatrixRGradient.getSum(i, j)) + 1e-8;
				MatrixR.add(i, j, -1.0 * dGammaR * dValue / dLrate);
			}
		}
		MatrixE.normalizeByRow();
		MatrixR.normalizeByRow();
	}
}