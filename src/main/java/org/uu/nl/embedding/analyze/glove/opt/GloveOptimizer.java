package org.uu.nl.embedding.analyze.glove.opt;

import org.apache.commons.math3.util.FastMath;
import org.uu.nl.embedding.analyze.CooccurenceMatrix;
import org.uu.nl.embedding.analyze.glove.GloveModel;
import org.uu.nl.embedding.progress.DoNothingPublisher;
import org.uu.nl.embedding.progress.ProgressState;
import org.uu.nl.embedding.progress.ProgressType;
import org.uu.nl.embedding.progress.Publisher;

import java.util.Random;
import java.util.concurrent.*;


public abstract class GloveOptimizer implements Optimizer {

    final CooccurenceMatrix crecs;
	final int dimension;
    final int vocabSize;
    private final int maxIterations;
    final int numThreads;
    final int crecCount;
	private final double  tolerance;
    final double xMax;
    final double alpha;
    final double learningRate = 0.05;
	final double[] W;
	final int[] linesPerThread;
	private final Publisher publisher;
	private final ExecutorService es;

	GloveOptimizer(GloveModel glove, int maxIterations, int numThreads, double tolerance) {
		this(glove, maxIterations, numThreads, tolerance, new DoNothingPublisher());
	}

	GloveOptimizer(GloveModel glove, int maxIterations, int numThreads, double tolerance, Publisher publisher) {
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
        final Random r = new Random();

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
		
		//System.out.println("Starting " + getName() + " optimizer");
		Optimum opt = new Optimum(this.dimension);
		CompletionService<Double> completionService = new ExecutorCompletionService<>(es);

		publisher.setNewMax(maxIterations);
		final ProgressState progressState = new ProgressState(ProgressType.GLOVE);
		double finalCost = 0;
		try {
			double prevCost = 0;
			double iterDiff;
			for(int iteration = 0; iteration < maxIterations; iteration++ ) {
				
				for(int id = 0; id < numThreads; id++) 
					completionService.submit(createJob(id, iteration));
				
				int received = 0;
				double localCost = 0;

				while(received < numThreads) {
					try {
						localCost += completionService.take().get();
						received++;
					} catch (InterruptedException | ExecutionException e) {
						e.printStackTrace();
					}
				}

				localCost = (localCost / crecCount);
				iterDiff= FastMath.abs(prevCost - localCost);
				if(iterDiff <= tolerance) {
					finalCost = localCost;
					break;
				}
				prevCost = localCost;
				//opt.addCost(prevCost);

				progressState.setN(iteration);
				progressState.setValue(prevCost);
				publisher.updateProgress(progressState);
				publisher.setExtraMessage(String.format("%.8f", iterDiff) + "/" + String.format("%.5f", tolerance));
			}
			
		} finally {
			es.shutdown();
		}

		opt.setResult(extractResult());
		opt.setFinalCost(finalCost);

		progressState.setValue(finalCost);
		progressState.setFinished(true);
		publisher.updateProgress(progressState);

		return opt;
	}

	/**
	 * Create a new double array containing the averaged values between the focus and context vectors
	 * @return a new double array containing the averaged values between the focus and context vectors
	 */
	private double[] extractResult() {
		double[] U = new double[vocabSize * dimension];
		int l1, l2, l3;
		for (int a = 0; a < vocabSize; a++) {
			l1 = a * (dimension + 1); // Index for focus node
			l2 = (a + vocabSize) * (dimension + 1); // Index for context node
			l3 = (a * dimension); // Index for output node
			for(int d = 0; d < dimension; d++)  {
				// For each node, take the average between the focus and context value
				U[d + l3] = (W[d + l1] + W[d + l2]) / 2;
			}
		}

		return U;
	}
	
	protected abstract GloveJob createJob(int id, int iteration);

}
