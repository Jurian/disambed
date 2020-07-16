package org.uu.nl.embedding.kale;

import java.nio.file.Files;

import org.uu.nl.embedding.kale.model.KaleModel;
import org.uu.nl.embedding.util.InMemoryRdfGraph;
import org.uu.nl.embedding.util.config.Configuration;

public class KaleRunner {
	java.text.DecimalFormat decimalFormat = new java.text.DecimalFormat("#.######");
	
	private final KaleModel kale;
	private final InMemoryRdfGraph graph;
	private final int iNumEntities;
	private final int iNumRelations;
	
	private int m_NumFactor = 20;
	private int m_NumMiniBatch = 100;
	private double m_Delta = 0.1;
	private double m_GammaE = 0.01;
	private double m_GammaR = 0.01;
	private int m_NumIteration = 1000;
	private int m_OutputIterSkip = 50;
	private double m_Weight = 0.01;
	

	private String FILEPATH = "/Users/euan/eclipse-workspace/graphLogicEmbed/data/input";
	private String fileExtension;
	private String fnSaveFile;
	private String fnTrainingTriples;
	private String fnValidateTriples;
	private String fnTestingTriples;
	private String fnTrainingRules;
	
	/**
	 * 
	 * @param graph
	 * @param config
	 */
	public KaleRunner(final InMemoryRdfGraph graph, final Configuration config) throws Exception{

		this.graph = graph;
		final int[] verts = graph.getVertices().toIntArray();
		final int[] edges = graph.getEdges().toIntArray();
		this.iNumEntities = verts.length;
		this.iNumRelations = edges.length;
		

		this.fnSaveFile = "result-k" + m_NumFactor 
				+ "-d" + decimalFormat.format(m_Delta)
				+ "-ge" + decimalFormat.format(m_GammaE) 
				+ "-gr" + decimalFormat.format(m_GammaR)
				+ "-w" +  decimalFormat.format(m_Weight) + this.fileExtension;
		
		this.fnTrainingTriples = this.FILEPATH + "training_triples";
		this.fnValidateTriples = this.FILEPATH + "validate_triples";
		this.fnTestingTriples = this.FILEPATH + "testing_triples";
		this.fnTrainingRules = this.FILEPATH + "training_rules";
		
		this.kale = new KaleModel();
		this.kale.Initialization(this.iNumRelations, 
				this.iNumEntities, 
				this.fnTrainingTriples, 
				this.fnValidateTriples, 
				this.fnTestingTriples, 
				this.fnTrainingRules);
		
		
	}
	

	/*
	 * Hyper-parameter getters and setters.
	 *
	 */
	
	public void setNumFactors(final int numFactors) {
		this.m_NumFactor = numFactors;
		this.kale.m_NumFactor = numFactors;
	}
	
	public void setNumMiniBatch(final int numMiniBatch) {
		this.m_NumMiniBatch = numMiniBatch;
		this.kale.m_NumMiniBatch = numMiniBatch;
	}
	
	public void setDelta(final double delta) {
		this.m_Delta = delta;
		this.kale.m_Delta = delta;
	}
	
	public void setGammaE(final double gammaE) {
		this.m_GammaE = gammaE;
		this.kale.m_GammaE = gammaE;
	}
	
	public void setGammaR(final double gammaR) {
		this.m_GammaR = gammaR;
		this.kale.m_GammaR = gammaR;
	}
	
	public void setNumIteration(final int numIterations) {
		this.m_NumIteration = numIterations;
		this.kale.m_NumIteration = numIterations;
	}
	
	public void setOutputIterSkip(final int outputIterSkip) {
		this.m_OutputIterSkip = outputIterSkip;
		this.kale.m_OutputIterSkip = outputIterSkip;
	}
	
	public void setWeight(final double weight) {
		this.m_Weight = weight;
		this.kale.m_Weight = weight;
	}
	
	public void setfileExtension(final String fileExtension) throws Exception {
		// Simple check
		if (fileExtension.charAt(0) != '.') { 
			throw new Exception("provided file extension in KaleRunner has invalid format.");
		}
		this.fileExtension = fileExtension;
		this.kale.fileExtension = fileExtension;
	}
	
	public int getNumFactors() {
		return this.m_NumFactor;
	}
	
	public int getNumMiniBatch() {
		return this.m_NumMiniBatch;
	}
	
	public double getDelta() {
		return this.m_Delta;
	}
	
	public double getGammaE() {
		return this.m_GammaE;
	}
	
	public double getGammaR() {
		return this.m_GammaR;
	}
	
	public int getNumIteration() {
		return this.m_NumIteration;
	}
	
	public int getOutputIterSkip() {
		return this.m_OutputIterSkip;
	}
	
	public double getWeight() {
		return this.m_Weight;
	}
	
	public String getfileExtension() {
		return this.fileExtension;
	}
}
