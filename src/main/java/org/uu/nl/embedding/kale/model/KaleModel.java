package org.uu.nl.embedding.kale.model;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;

import org.uu.nl.embedding.kale.struct.RuleSet;
import org.uu.nl.embedding.kale.struct.Triple;
import org.uu.nl.embedding.kale.struct.TripleMatrix;
import org.uu.nl.embedding.kale.struct.TripleRule;
import org.uu.nl.embedding.kale.struct.TripleSet;
import org.uu.nl.embedding.kale.util.MetricMonitor;
import org.uu.nl.embedding.kale.util.NegativeRuleGenerator;
import org.uu.nl.embedding.kale.util.NegativeTripleGenerator;


/**
 * Class imported from iieir-km / KALE on GitHub
 * (https://github.com/iieir-km/KALE/tree/817474edb0da54a76b562bed2328e96284557b87)
 *
 */
public class KaleModel {
	public TripleSet m_TrainingTriples;
	public TripleSet m_ValidateTriples;
	public TripleSet m_TestingTriples;
	public TripleSet m_Triples;
	public RuleSet m_TrainingRules;
	
	public TripleMatrix m_Entity_Factor_MatrixE;
	public TripleMatrix m_Relation_Factor_MatrixR;
	public TripleMatrix m_MatrixEGradient;
	public TripleMatrix m_MatrixRGradient;
	
	public int m_NumRelation;
	public int m_NumEntity;
	public String m_MatrixE_prefix = "";
	public String m_MatrixR_prefix = "";
	
	public int m_NumFactor = 20;
	public int m_NumMiniBatch = 100;
	public double m_Delta = 0.1;
	public double m_GammaE = 0.01;
	public double m_GammaR = 0.01;
	public int m_NumIteration = 1000;
	public int m_OutputIterSkip = 50;
	public double m_Weight = 0.01;
	
	/**
	 * 
	 */
	public String fileExtension = ".csv";
	
	java.text.DecimalFormat decimalFormat = new java.text.DecimalFormat("#.######");
	
	public KaleModel() {
	}
	
	/**
	 * 
	 * @param iNumRelation
	 * @param iNumEntity
	 * @param fnTrainingTriples
	 * @param fnValidateTriples
	 * @param fnTestingTriples
	 * @param fnTrainingRules
	 * @throws Exception
	 */
	public void Initialization(final int iNumRelation, final int iNumEntity,
			String fnTrainingTriples, String fnValidateTriples, String fnTestingTriples,
			String fnTrainingRules) throws Exception {
		this.m_NumRelation = iNumRelation;
		this.m_NumEntity = iNumEntity;
		this.m_MatrixE_prefix = "MatrixE-k" + m_NumFactor 
				+ "-d" + decimalFormat.format(m_Delta)
				+ "-ge" + decimalFormat.format(m_GammaE) 
				+ "-gr" + decimalFormat.format(m_GammaR)
				+ "-w" +  decimalFormat.format(m_Weight);
		this.m_MatrixR_prefix = "MatrixR-k" + m_NumFactor 
				+ "-d" + decimalFormat.format(m_Delta)
				+ "-ge" + decimalFormat.format(m_GammaE) 
				+ "-gr" + decimalFormat.format(m_GammaR)
				+ "-w" +  decimalFormat.format(m_Weight);
		
		System.out.println("\nLoading training and validate triples");
		this.m_TrainingTriples = new TripleSet(this.m_NumEntity, this.m_NumRelation);
		this.m_ValidateTriples = new TripleSet(this.m_NumEntity, this.m_NumRelation);
		this.m_Triples = new TripleSet();
		this.m_TrainingTriples.load(fnTrainingTriples);
		this.m_ValidateTriples.subload(fnValidateTriples);
		this.m_Triples.loadStr(fnTrainingTriples);
		this.m_Triples.loadStr(fnValidateTriples);
		this.m_Triples.loadStr(fnTestingTriples);
		System.out.println("Success.");
		
		System.out.println("\nLoading grounding rules");
		this.m_TrainingRules = new RuleSet(this.m_NumEntity, this.m_NumRelation);
		this.m_TrainingRules.load(fnTrainingRules);
		System.out.println("Success.");		
		
		System.out.println("\nRandomly initializing matrix E and matrix R");
		this.m_Entity_Factor_MatrixE = new TripleMatrix(this.m_NumEntity, this.m_NumFactor);
		this.m_Entity_Factor_MatrixE.setToRandom();
		this.m_Entity_Factor_MatrixE.normalizeByRow();
		this.m_Relation_Factor_MatrixR = new TripleMatrix(this.m_NumRelation, this.m_NumFactor);
		this.m_Relation_Factor_MatrixR.setToRandom();
		this.m_Relation_Factor_MatrixR.normalizeByRow();
		System.out.println("Success.");
		
		System.out.println("\nInitializing gradients of matrix E and matrix R");
		this.m_MatrixEGradient = new TripleMatrix(this.m_NumEntity, this.m_NumFactor);
		this.m_MatrixRGradient = new TripleMatrix(this.m_NumRelation, this.m_NumFactor);
		System.out.println("Success.");
	}
	
	public void Initialization(String strNumRelation, String strNumEntity,
			String fnTrainingTriples, String fnValidateTriples, String fnTestingTriples,
			String fnTrainingRules) throws Exception {
		
		m_NumRelation = Integer.parseInt(strNumRelation);
		m_NumEntity = Integer.parseInt(strNumEntity);
		
		Initialization(m_NumRelation, 
				m_NumEntity,
				fnTrainingTriples, 
				fnValidateTriples, 
				fnTestingTriples, 
				fnTrainingRules); 
	}
	
	public void Cochez_learn() throws Exception {
		
	}
	
	public void TransE_Learn() throws Exception {
		HashMap<Integer, ArrayList<Triple>> lstPosTriples = new HashMap<Integer, ArrayList<Triple>>();
		HashMap<Integer, ArrayList<Triple>> lstHeadNegTriples = new HashMap<Integer, ArrayList<Triple>>();
		HashMap<Integer, ArrayList<Triple>> lstTailNegTriples = new HashMap<Integer, ArrayList<Triple>>();
		HashMap<Integer, ArrayList<TripleRule>> lstRules = new HashMap<Integer, ArrayList<TripleRule>>();
		HashMap<Integer, ArrayList<TripleRule>> lstSndRelNegRules = new HashMap<Integer, ArrayList<TripleRule>>();
		
		
		String PATHLOG = "result-k" + m_NumFactor 
				+ "-d" + decimalFormat.format(m_Delta)
				+ "-ge" + decimalFormat.format(m_GammaE) 
				+ "-gr" + decimalFormat.format(m_GammaR)
				+ "-w" +  decimalFormat.format(m_Weight) + this.fileExtension;
		
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(PATHLOG), "UTF-8"));
		
		int iIter = 0;
		writer.write("Complete iteration #" + iIter + ":\n");
		System.out.println("Complete iteration #" + iIter + ":");
		MetricMonitor first_metrics = new MetricMonitor(
				m_ValidateTriples,
				m_Triples.tripleSet(),
				m_Entity_Factor_MatrixE,
				m_Relation_Factor_MatrixR);
		first_metrics.calculateMetrics();
		double dCurrentHits = first_metrics.dHits;
		double dCurrentMRR = first_metrics.dMRR;
		writer.write("------Current MRR:"+ dCurrentMRR + "\tCurrent Hits@10:" + dCurrentHits + "\n");
		System.out.print("\n");
		double dBestHits = first_metrics.dHits;
		double dBestMRR = first_metrics.dMRR;
		int iBestIter = 0;
		
		
		long startTime = System.currentTimeMillis();
		while (iIter < m_NumIteration) {
			m_TrainingTriples.randomShuffle();
			for (int iIndex = 0; iIndex < m_TrainingTriples.triples(); iIndex++) {
				Triple PosTriple = m_TrainingTriples.get(iIndex);
				NegativeTripleGenerator negTripGen = new NegativeTripleGenerator(
						PosTriple, m_NumEntity, m_NumRelation);
				Triple headNegTriple = negTripGen.generateHeadNegTriple();
				Triple tailNegTriple = negTripGen.generateTailNegTriple();
				
				int iID = iIndex % m_NumMiniBatch;
				if (!lstPosTriples.containsKey(iID)) {
					ArrayList<Triple> tmpPosLst = new ArrayList<Triple>();
					ArrayList<Triple> tmpHeadNegLst = new ArrayList<Triple>();
					ArrayList<Triple> tmpTailNegLst = new ArrayList<Triple>();
					tmpPosLst.add(PosTriple);
					tmpHeadNegLst.add(headNegTriple);
					tmpTailNegLst.add(tailNegTriple);
					lstPosTriples.put(iID, tmpPosLst);
					lstHeadNegTriples.put(iID, tmpHeadNegLst);
					lstTailNegTriples.put(iID, tmpTailNegLst);
				} else {
					lstPosTriples.get(iID).add(PosTriple);
					lstHeadNegTriples.get(iID).add(headNegTriple);
					lstTailNegTriples.get(iID).add(tailNegTriple);
				}
			}
			
			m_TrainingRules.randomShuffle();
			for (int iIndex = 0; iIndex < m_TrainingRules.rules(); iIndex++) {
				TripleRule rule = m_TrainingRules.get(iIndex);
				
				NegativeRuleGenerator negRuleGen = new NegativeRuleGenerator(
						rule,  m_NumRelation);
				TripleRule sndRelNegrule = negRuleGen.generateSndNegRule();			

				int iID = iIndex % m_NumMiniBatch;
				if (!lstRules.containsKey(iID)) {
					ArrayList<TripleRule> tmpLst = new ArrayList<TripleRule>();
					ArrayList<TripleRule> tmpsndRelNegLst = new ArrayList<TripleRule>();
					tmpLst.add(rule);
					tmpsndRelNegLst.add(sndRelNegrule);
					lstRules.put(iID, tmpLst);
					lstSndRelNegRules.put(iID, tmpsndRelNegLst);
					
				} else {
					lstRules.get(iID).add(rule);
					lstSndRelNegRules.get(iID).add(sndRelNegrule);
				}
			}
			
			double m_BatchSize= m_TrainingTriples.triples()/(double)m_NumMiniBatch;
			for (int iID = 0; iID < m_NumMiniBatch; iID++) {
				StochasticUpdater stochasticUpdate = new StochasticUpdater(
						lstPosTriples.get(iID),
						lstHeadNegTriples.get(iID),
						lstTailNegTriples.get(iID),
						lstRules.get(iID),
						lstSndRelNegRules.get(iID),
						m_Entity_Factor_MatrixE,
						m_Relation_Factor_MatrixR,
						m_MatrixEGradient,
						m_MatrixRGradient,
//	###					learning rate
						m_GammaE,
						m_GammaR,
//	###					margin
						m_Delta,
//	###					weight
						m_Weight);
				stochasticUpdate.stochasticIteration();
			}

			
			lstPosTriples = new HashMap<Integer, ArrayList<Triple>>();
			lstHeadNegTriples = new HashMap<Integer, ArrayList<Triple>>();
			lstTailNegTriples = new HashMap<Integer, ArrayList<Triple>>();

			lstRules = new HashMap<Integer, ArrayList<TripleRule>>();
			lstSndRelNegRules = new HashMap<Integer, ArrayList<TripleRule>>();
			
			iIter++;
			System.out.println("Complete iteration #" + iIter + ":");
			
			if (iIter % m_OutputIterSkip == 0) {
				writer.write("Complete iteration #" + iIter + ":\n");
				System.out.println("Complete iteration #" + iIter + ":");
				MetricMonitor metric = new MetricMonitor(
						m_ValidateTriples,
						m_Triples.tripleSet(),
						m_Entity_Factor_MatrixE,
						m_Relation_Factor_MatrixR);
				metric.calculateMetrics();
				dCurrentHits = metric.dHits;
				dCurrentMRR = metric.dMRR;
				writer.write("------Current MRR:"+ dCurrentMRR + "\tCurrent Hits@10:" + dCurrentHits + "\n");
				if (dCurrentMRR > dBestMRR) {
					m_Relation_Factor_MatrixR.output(m_MatrixR_prefix + ".best");
					m_Entity_Factor_MatrixE.output(m_MatrixE_prefix + ".best");
					dBestHits = dCurrentHits;
					dBestMRR = dCurrentMRR;
					iBestIter = iIter;
				}
				writer.write("------Best iteration #" + iBestIter + "\t" + dBestMRR + "\t" + dBestHits+"\n");
				writer.flush();
				System.out.println("------\tBest iteration #" + iBestIter + "\tBest MRR:" + dBestMRR + "Best \tHits@10:" + dBestHits);
				writer.flush();
			}
		}
		long endTime = System.currentTimeMillis();
		System.out.println("All running time:" + (endTime-startTime)+"ms");
		writer.close();
	}
}