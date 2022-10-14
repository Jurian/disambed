package org.uu.nl.disembed.embedding.opt;

import com.github.jelmerk.knn.Item;
import me.tongfei.progressbar.ProgressBar;
import org.apache.commons.math.util.FastMath;
import org.apache.log4j.Logger;
import org.uu.nl.disembed.embedding.bca.CoOccurrenceMatrix;
import org.uu.nl.disembed.util.config.Configuration;
import org.uu.nl.disembed.util.progress.Progress;
import org.uu.nl.disembed.util.rnd.ExtendedRandom;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.concurrent.*;

/**
 * @author Jurian Baas
 */
public abstract class Optimizer implements IOptimizer {

	private final static Logger logger = Logger.getLogger(Optimizer.class);
	private static final ExtendedRandom random = Configuration.getThreadLocalRandom();

	protected final CoOccurrenceMatrix coMatrix;
	protected final int dimension;
	protected final int contextVectors, focusVectors;
	protected final int numThreads;
	protected final int coCount;
	protected final float learningRate = 0.05f;
	protected final float[][] focus, context;
	protected final float[] fBias, cBias;
	protected final int[] linesPerThread;
	protected final CostFunction costFunction;
	private final int maxIterations;
	private final double tolerance;

	/**
	 * Estimate RAM usage of this object.
	 * @return Approximate number of 32 numbers used
	 */
	@Override
	public double calculateMemoryMegaBytes() {
		int focusVec_mb = focusVectors * dimension;
		int contextVec_mb = contextVectors * dimension;
		double mb = (focusVec_mb + contextVec_mb + focusVectors + contextVectors) / 262144d;
		return (double) Math.round(mb * 100) / 100;
	}

    protected Optimizer(CoOccurrenceMatrix coMatrix, Configuration config, CostFunction costFunction) {

		this.costFunction = costFunction;
		this.coMatrix = coMatrix;
		this.maxIterations = config.getEmbedding().getOpt().getMaxiter();
		this.tolerance = config.getEmbedding().getOpt().getTolerance();
		this.contextVectors = coMatrix.nrOfContextVectors();
		this.focusVectors = coMatrix.nrOfFocusVectors();
		this.numThreads = config.getThreads();
		this.coCount = coMatrix.coOccurrenceCount();
		this.dimension = config.getEmbedding().getDim();

		double ramUsageMB = calculateMemoryMegaBytes();
		if(ramUsageMB < 1024) {
			logger.info("Allocating " + ramUsageMB + " MB of RAM...");
		} else {
			ramUsageMB = (double) Math.round(ramUsageMB * 100) / 100;
			logger.info("Allocating " + (ramUsageMB / 1024) + " GB of RAM...");
		}

		this.focus = new float[focusVectors][dimension];
		this.context = new float[contextVectors][dimension];
		this.fBias = new float[focusVectors];
		this.cBias = new float[contextVectors];

		for (int i = 0; i < focusVectors; i++) {
			fBias[i] = (float) (random.nextFloat() - 0.5) / dimension;

			for (int d = 0; d < dimension; d++) {
				focus[i][d] = (float) (random.nextFloat() - 0.5) / dimension;
			}
		}

		for (int i = 0; i < contextVectors; i++) {
			cBias[i] = (float) (random.nextFloat() - 0.5) / dimension;

			for (int d = 0; d < dimension; d++) {
				context[i][d] = (float) (random.nextFloat() - 0.5) / dimension;
			}
		}

		this.linesPerThread = new int[numThreads];
		for (int i = 0; i < numThreads - 1; i++) {
			linesPerThread[i] = coCount / numThreads;
		}
		linesPerThread[numThreads - 1] = coCount / numThreads + coCount % numThreads;
	}

	@Override
	public Embedding optimize() throws OptimizationFailedException {

		final Embedding embedding = new Embedding(dimension, focusVectors);
		final ExecutorService es = Executors.newWorkStealingPool(numThreads);
		final CompletionService<Float> completionService = new ExecutorCompletionService<>(es);

		try(ProgressBar pb = Progress.progressBar(getName(), maxIterations, "epochs")) {

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

				if(Double.isNaN(localCost) || Double.isInfinite(localCost)) {
					throw new OptimizationFailedException("Cost infinite or NAN");
				}

				localCost = (localCost / coCount);

				//opt.addIntermediaryResult(localCost);
				iterDiff = FastMath.abs(prevCost - localCost);

				pb.step();
				pb.setExtraMessage(formatMessage(iterDiff));
				prevCost = localCost;

				if(iterDiff <= tolerance) {
					break;
				}
			}
			
		} finally {
			es.shutdown();
		}

		for (EmbeddingIterator it = new EmbeddingIterator(); it.hasNext(); ) {
			EmbeddedEntity entity = it.next();
			int i = entity.index();
			embedding.setKey(i, entity.key);
			embedding.setVector(i, entity.vector);
		}

		return embedding;
	}

	private String formatMessage(double iterDiff) {
		return new BigDecimal(iterDiff).stripTrailingZeros().toPlainString();
	}

	/**
	 * Instead of wasting RAM by copying the entire embedding to a new double array,
	 * we can access it as a stream of float arrays with this iterator.
	 */
	private class EmbeddingIterator implements Iterator<EmbeddedEntity> {

		private int focusIndex = 0;

		@Override
		public boolean hasNext() {
			return focusIndex < focusVectors;
		}

		@Override
		public EmbeddedEntity next() {

			final int contextIndex = coMatrix.focusIndex2Context(focusIndex);
			final float[] vector = new float[dimension];
			float squaredSum = 0;
			for (int d = 0; d < dimension; d++)  {
				vector[d] = (focus[focusIndex][d] + context[contextIndex][d]) / 2;
				squaredSum += vector[d] * vector[d];
			}

			// Normalize the vector
			float magnitude = (float) Math.sqrt(squaredSum);
			for (int d = 0; d < dimension; d++)  {
				vector[d] /= magnitude;
			}

			final EmbeddedEntity entity = new EmbeddedEntity (
					focusIndex,
					coMatrix.getKey(focusIndex),
					vector
			);

			focusIndex++;
			return entity;
		}
	}

	/**
	 * View of an embedded entity
	 */
	public record EmbeddedEntity(int index, String key, float[] vector) implements Item<String, float[]> {
		@Override
		public String id() {
			return key;
		}

		@Override
		public int dimensions() {
			return vector.length;
		}
	}

}
