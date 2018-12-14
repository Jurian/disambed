package org.uu.nl.analyze.glove.opt;

import java.util.Random;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.math3.util.FastMath;
import org.uu.nl.analyze.CooccurenceMatrix;
import org.uu.nl.analyze.glove.GloveModel;
import org.uu.nl.analyze.progress.Progress;
import org.uu.nl.analyze.progress.ProgressType;
import org.uu.nl.analyze.progress.Publisher;


public abstract class GloveOptimizer implements Optimizer {
	
	protected final Random r = new Random();
	protected final CooccurenceMatrix crecs;
	protected final int dimension, vocabSize, maxIterations, numThreads, crecCount;
	protected final double  tolerance, xMax, alpha, learningRate = 0.05; 
	protected final double[] W;
	protected final int[] linesPerThread;
	private final Publisher publisher;
	private final ExecutorService es;
	
	public GloveOptimizer(GloveModel glove, int maxIterations, int numThreads, double tolerance, Publisher publisher) {
		this.publisher = publisher;
		this.crecs = glove.getCoMatrix();
		this.xMax = glove.getxMax();
		this.alpha = glove.getAlpha();
		this.maxIterations = maxIterations;
		this.tolerance = tolerance;
		this.vocabSize = glove.getVocabSize();
		this.numThreads = numThreads;
		this.crecCount = crecs.cooccurrenceCount();
		int dimension = glove.getDimension() + 1;
		
		this.W = new double[2 * vocabSize * dimension];
		for (int i = 0; i < 2 * vocabSize; i++) {
			for (int d = 0; d < dimension; d++) 
				W[i * dimension + d] = (r.nextDouble() - 0.5) / dimension;
		}
		
		this.linesPerThread = new int[numThreads];
		for(int i = 0; i < numThreads-1; i++) {
			linesPerThread[i] = crecCount / numThreads;
		}
		linesPerThread[numThreads-1] =  crecCount / numThreads + crecCount % numThreads;
		
		this.dimension = dimension-1;
		this.es = Executors.newFixedThreadPool(numThreads);
	}
	
	@Override
	public Optimum optimize() {
		
		System.out.println("Starting " + getName() + " optimizer");
		Optimum opt = new Optimum(this.dimension);
		CompletionService<Double> completionService = new ExecutorCompletionService<>(es);
		
		Progress progress = new Progress(ProgressType.GLOVE);
		
		try {
			double iterCost = 0;
			for(int iteration = 0; iteration < maxIterations; ) {
				
				for(int id = 0; id < numThreads; id++) 
					completionService.submit(createJob(id, iteration));
				
				int received = 0;
				double totalCost = 0;
				while(received < numThreads) {
					try {
						totalCost += completionService.take().get();
						received++;
					} catch (InterruptedException | ExecutionException e) {
						e.printStackTrace();
					}
				}
				totalCost = (totalCost / crecCount);
				if(FastMath.abs(iterCost - totalCost) <= tolerance) break;
				iterCost = totalCost;
				opt.addCost(iterCost);
				
				iteration++;

				progress.setValue(iterCost);
				publisher.updateProgress(progress);

			}
			
		} finally {
			es.shutdown();
		}
		
		progress.setValue(opt.finalResult());
		progress.setFinished(true);
		publisher.updateProgress(progress);
		
		opt.setResult(extractResult());
		System.out.println("Converged with final cost " + opt.finalResult());
		return opt;
	}
	
	private double[] extractResult() {
		double[] U = new double[vocabSize * dimension];
		int l1, l2, l3;
		for (int a = 0; a < vocabSize; a++) {
			l1 = a * (dimension + 1);
			l2 = (a + vocabSize) * (dimension + 1);
			l3 = (a * dimension);
			for(int d = 0; d < dimension; d++)  {
				U[d + l3] = (W[d + l1] + W[d + l2]) / 2;
			}
		}

		return U;
	}
	
	public abstract GloveJob createJob(int id, int iteration);
	
}
