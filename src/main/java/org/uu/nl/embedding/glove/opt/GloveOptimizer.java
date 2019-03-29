package org.uu.nl.embedding.glove.opt;

import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;
import org.apache.commons.math.util.FastMath;
import org.uu.nl.embedding.CooccurenceMatrix;
import org.uu.nl.embedding.glove.GloveModel;

import java.util.Random;
import java.util.concurrent.*;


public abstract class GloveOptimizer implements Optimizer {

	protected final CooccurenceMatrix crecs;
	protected final int dimension;
	protected final int vocabSize;
    private final int maxIterations;
	protected final int numThreads;
	protected final int crecCount;
	private final double  tolerance;
	protected final double xMax;
	protected final double alpha;
	protected final double learningRate = 0.05;
	protected final double[] W;
	protected final int[] linesPerThread;
	private final ExecutorService es;

    private static final int PB_UPDATE_INTERVAL = 250;
    private static final ProgressBarStyle PB_STYLE = ProgressBarStyle.COLORFUL_UNICODE_BLOCK;

	protected GloveOptimizer(GloveModel glove, int maxIterations, double tolerance) {
		this.crecs = glove.getCoMatrix();
		this.xMax = glove.getxMax();
		this.alpha = glove.getAlpha();
		this.maxIterations = maxIterations;
		this.tolerance = tolerance;
		this.vocabSize = glove.getVocabSize();
		this.numThreads = Runtime.getRuntime().availableProcessors() - 1;
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
		this.es = Executors.newWorkStealingPool(numThreads);
	}
	
	@Override
	public Optimum optimize() {

		Optimum opt = new Optimum();
		CompletionService<Double> completionService = new ExecutorCompletionService<>(es);

		double finalCost = 0;
		try(ProgressBar pb = new ProgressBar(getName(), maxIterations, PB_UPDATE_INTERVAL, System.out, PB_STYLE, " iterations", 1, true )) {
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
					pb.step();
					pb.setExtraMessage(formatMessage(iterDiff));
					break;
				}
				prevCost = localCost;

				pb.step();
				pb.setExtraMessage(formatMessage(iterDiff));
			}
			
		} finally {
			es.shutdown();
		}

		opt.setResult(extractResult());
		opt.setFinalCost(finalCost);

		return opt;
	}

	private String formatMessage(double iterDiff) {
		return String.format("%.8f", iterDiff) + "/" + String.format("%.5f", tolerance);
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
