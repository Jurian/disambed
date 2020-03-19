package org.uu.nl.embedding.opt;

import me.tongfei.progressbar.ProgressBar;
import org.apache.commons.math.util.FastMath;
import org.uu.nl.embedding.util.CRecMatrix;
import org.uu.nl.embedding.util.config.Configuration;
import org.uu.nl.embedding.util.rnd.ExtendedRandom;

import java.math.BigDecimal;
import java.util.concurrent.*;

/**
 * @author Jurian Baas
 */
public abstract class Optimizer implements IOptimizer {

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
	protected final float[] fBias, cBias;
	protected final int[] linesPerThread;
	protected final CostFunction costFunction;
	private final ExecutorService es;
	private final int maxIterations;
	private final double tolerance;

	protected Optimizer(OptimizerModel optimizerModel, Configuration config, CostFunction costFunction) {

		this.costFunction = costFunction;
		this.coMatrix = optimizerModel.getCoMatrix();
		this.xMax = optimizerModel.getxMax();
		this.alpha = optimizerModel.getAlpha();
		this.maxIterations = config.getOpt().getMaxiter();
		this.tolerance = config.getOpt().getTolerance();
		this.vocabSize = optimizerModel.getVocabSize();
		this.numThreads = config.getThreads();
		this.coCount = coMatrix.coOccurrenceCount();

		this.dimension = optimizerModel.getDimension();

		this.focus = new float[vocabSize * dimension];
		this.context = new float[vocabSize * dimension];
		this.fBias = new float[vocabSize];
		this.cBias = new float[vocabSize];

		for (int i = 0; i < vocabSize; i++) {
			fBias[i] = (float) (random.nextFloat() - 0.5) / dimension;
			cBias[i] = (float) (random.nextFloat() - 0.5) / dimension;
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

				pb.step();
				pb.setExtraMessage(formatMessage(iterDiff));
				prevCost = localCost;

				if(iterDiff <= tolerance) {
					finalCost = localCost;
					break;
				}
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
		for (int a = 0; a < vocabSize; a++) {
			int i = a * dimension;
			for (int d = 0; d < dimension; d++) {
				// For each node, take the average between the focus and context value
				U[d + i] = (focus[d + i] + context[d + i]) / 2;
			}
		}

		return U;
	}

	protected abstract OptimizeJob createJob(int id, int iteration);

}
