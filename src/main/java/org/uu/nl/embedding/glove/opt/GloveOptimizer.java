package org.uu.nl.embedding.glove.opt;

import me.tongfei.progressbar.ProgressBar;
import org.apache.commons.math.util.FastMath;
import org.uu.nl.embedding.glove.GloveModel;
import org.uu.nl.embedding.util.CRecMatrix;
import org.uu.nl.embedding.util.config.Configuration;
import org.uu.nl.embedding.util.rnd.ExtendedRandom;

import java.math.BigDecimal;
import java.util.concurrent.*;

/**
 * @author Jurian Baas
 */
public abstract class GloveOptimizer implements Optimizer {

	private static final ExtendedRandom random = Configuration.getThreadLocalRandom();

	protected final CRecMatrix coMatrix;
	protected final int dimension;
	protected final int vocabSize;
	protected final int numThreads;
	protected final int coCount;
	protected final double xMax;
	protected final double alpha;
	protected final double learningRate = 0.05;
	protected final float[] focus, context;
	protected final int[] linesPerThread;
	private final ExecutorService es;
	private final int maxIterations;
	private final double tolerance;

	protected GloveOptimizer(GloveModel glove, Configuration config) {

		this.coMatrix = glove.getCoMatrix();
		this.xMax = glove.getxMax();
		this.alpha = glove.getAlpha();
		this.maxIterations = config.getOpt().getMaxiter();
		this.tolerance = config.getOpt().getTolerance();
		this.vocabSize = glove.getVocabSize();
		this.numThreads = config.getThreads();
		this.coCount = coMatrix.coOccurrenceCount();

		// Make room for the bias terms
		int dimension = glove.getDimension() + 1;

		this.focus = new float[vocabSize * dimension];
		this.context = new float[vocabSize * dimension];

		for (int i = 0; i < vocabSize; i++) {
			for (int d = 0; d < dimension; d++) {
				focus[i * dimension + d] = (float) (random.nextFloat() - 0.5) / dimension;
				context[i * dimension + d] = (float) (random.nextFloat() - 0.5) / dimension;
			}
		}

		this.linesPerThread = new int[numThreads];
		for (int i = 0; i < numThreads - 1; i++) {
			linesPerThread[i] = coCount / numThreads;
		}
		linesPerThread[numThreads - 1] = coCount / numThreads + coCount % numThreads;

		this.dimension = glove.getDimension();
		this.es = Executors.newWorkStealingPool(numThreads);
	}
	
	@Override
	public Optimum optimize() {

		Optimum opt = new Optimum();
		CompletionService<Float> completionService = new ExecutorCompletionService<>(es);

		double finalCost = 0;
		try(ProgressBar pb = Configuration.progressBar(getName(), maxIterations, "epochs")) {
			double prevCost = 0;
			double iterDiff;
			for (int iteration = 0; iteration < maxIterations; iteration++) {

				coMatrix.shuffle();

				for (int id = 0; id < numThreads; id++)
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

				localCost = (localCost / coCount);
				opt.addIntermediaryResult(localCost);
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
		return new BigDecimal(iterDiff).stripTrailingZeros().toPlainString() + "/" + new BigDecimal(tolerance).stripTrailingZeros().toPlainString();
	}

	/**
	 * Create a new double array containing the averaged values between the focus and context vectors
	 * @return a new double array containing the averaged values between the focus and context vectors
	 */
	private double[] extractResult() {
		double[] U = new double[vocabSize * dimension];
		int l1, l2;
		for (int a = 0; a < vocabSize; a++) {
			// Take into account that we included the bias term
			l1 = a * (dimension + 1); // Index for focus and context nodes
			l2 = (a * dimension); // Index for output node
			for (int d = 0; d < dimension; d++) {
				// For each node, take the average between the focus and context value
				U[d + l2] = (focus[d + l1] + context[d + l1]) / 2;
			}
		}

		return U;
	}

	protected abstract GloveJob createJob(int id, int iteration);

}
