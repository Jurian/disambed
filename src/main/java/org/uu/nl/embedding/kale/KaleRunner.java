package org.uu.nl.embedding.kale;

import java.io.File;
import java.util.HashSet;

import org.uu.nl.embedding.bca.BookmarkColoring;
import org.uu.nl.embedding.kale.model.KaleModel;
import org.uu.nl.embedding.kale.util.DataGenerator;
import org.uu.nl.embedding.util.InMemoryRdfGraph;
import org.uu.nl.embedding.util.config.Configuration;

public class KaleRunner {
	java.text.DecimalFormat decimalFormat = new java.text.DecimalFormat("#.######");
	
	private final KaleModel kale;
	private final InMemoryRdfGraph graph;
	private final int iNumEntities;
	private final int iNumRelations;
	private final HashSet<String> uniqueRelationTypes;
	private final int iNumUniqueRelations;
	
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
	private String fnGloveVectors;
	
	/**
	 * 
	 * @param graph
	 * @param config
	 */
	public KaleRunner(final InMemoryRdfGraph graph, final Configuration config) throws Exception{

		this.graph = graph;
		final int[] verts = graph.getVertices().toIntArray();
		final int[] edges = graph.getEdges().toIntArray();
		this.uniqueRelationTypes = new HashSet<String>();
		
		for (int e = 0; e < edges.length; e++) {
			String predicate = graph.getEdgeLabelProperty().getValueAsString(edges[e]).toLowerCase();
			if (!this.uniqueRelationTypes.contains(predicate)) this.uniqueRelationTypes.add(predicate);
		}
		
		this.iNumEntities = verts.length;
		this.iNumRelations = edges.length;
		this.iNumUniqueRelations = uniqueRelationTypes.size();

		boolean isKale = true;
		BookmarkColoring BCA = new BookmarkColoring(graph, config, isKale);
		boolean undirected = true;
		DataGenerator dataGenerator = new DataGenerator(graph, undirected,
										BCA.getInVertices(), BCA.getOutVertices(),
										BCA.getInEdges(), BCA.getOutEdges());
		this.fileExtension = ".txt";
		dataGenerator.Initialize();
		dataGenerator.setFileExtension(this.fileExtension);
		dataGenerator.setFilePath(FILEPATH);
		
		this.fnSaveFile = generateResultsFileDir();
		
		this.fnTrainingTriples = dataGenerator.fnTrainingTriples;
		this.fnValidateTriples = dataGenerator.fnValidateTriples;
		this.fnTestingTriples = dataGenerator.fnTestingTriples;
		this.fnTrainingRules = dataGenerator.fnTrainingRules;
		this.fnGloveVectors = dataGenerator.fnGloveVectors;
		
		this.kale = new KaleModel();
		this.kale.Initialization(this.iNumUniqueRelations, 
				this.iNumEntities, 
				this.fnTrainingTriples, 
				this.fnValidateTriples, 
				this.fnTestingTriples, 
				this.fnTrainingRules,
				this.fnGloveVectors);
		this.kale.CochezLearn();
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	public void CochezLearn() throws Exception {
		this.kale.CochezLearn();
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	public void TransELearn() throws Exception {
		this.kale.TransE_Learn();
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
			throw new Exception("Provided file extension in KaleRunner has invalid format.");
		}
		this.fileExtension = fileExtension;
		this.kale.fileExtension = fileExtension;

		this.fnSaveFile = generateResultsFileDir();
	}

	
	/**
	 * Generates file directory without overwriting existing
	 * files by iterating through numbers at start of file.
	 * 
	 * @param fnDirName
	 * @return
	 */
	public String generateResultsFileDir() {
		int num = 0;
		String fnTail = "_result-k" + this.m_NumFactor 
				+ "-d" + this.decimalFormat.format(this.m_Delta)
				+ "-ge" + this.decimalFormat.format(this.m_GammaE) 
				+ "-gr" + this.decimalFormat.format(this.m_GammaR)
				+ "-w" +  this.decimalFormat.format(this.m_Weight) + this.fileExtension;
		String fnDir = this.FILEPATH + num + fnTail;
		
		File f = new File(fnDir);
		while (f.exists()) {
			num++;
			fnDir = this.FILEPATH + num + fnTail;
			f = new File(fnDir);
		}
		return fnDir;
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
