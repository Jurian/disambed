package org.uu.nl.embedding.glove.opt.impl;

import org.apache.commons.math.util.FastMath;
import org.uu.nl.embedding.glove.GloveModel;
import org.uu.nl.embedding.glove.opt.GloveJob;
import org.uu.nl.embedding.glove.opt.GloveOptimizer;

/**
 * <p>
 * As adaptive learning rate methods have become the norm in training neural
 * networks, practitioners noticed that in some cases, e.g. for object
 * recognition or machine translation they fail to converge to an
 * optimal solution and are outperformed by SGD with momentum.
 * </p>
 * <p>
 * Reddi et al. (2018) formalize this issue and pinpoint the exponential
 * moving average of past squared gradients as a reason for the poor
 * generalization behaviour of adaptive learning rate methods. Recall that the
 * introduction of the exponential average was well-motivated: It should prevent
 * the learning rates to become infinitesimally small as training progresses,
 * the key flaw of the Adagrad algorithm. However, this short-term memory of the
 * gradients becomes an obstacle in other scenarios.
 * </p>
 * <p>
 * In settings where Adam converges to a suboptimal solution, it has been
 * observed that some minibatches provide large and informative gradients, but
 * as these minibatches only occur rarely, exponential averaging diminishes
 * their influence, which leads to poor convergence. The authors provide an
 * example for a simple convex optimization problem where the same behaviour can
 * be observed for Adam.
 * </p>
 * <p>
 * To fix this behaviour, the authors propose a new algorithm, AMSGrad that uses
 * the maximum of past squared gradients v t rather than the exponential average
 * to update the parameters.
 * </p>
 * 
 * @see <a href="https://arxiv.org/pdf/1904.09237.pdf">AMSGrad paper</a>
 * @see <a href="http://nlp.stanford.edu/projects/glove/">Stanford GloVe
 *      page</a>
 * @author Jurian Baas
 */
@SuppressWarnings("Duplicates")
public class AMSGradOptimizer extends GloveOptimizer {

	/**
	 * Contains the decaying averages of the past first momentums w.r.t. to all
	 * parameters
	 */
	private final double[] M1;
	/**
	 * Contains the decaying averages of the past second momentums w.r.t. to all
	 * parameters
	 */
	private final double[] M2;
	/**
	 * Decay rate for first momentum
	 */
	private final double beta1 = 0.9;
	/**
	 * Decay rate for second momentum
	 */
	private final double beta2 = 0.999;
	/**
	 * Mainly used to prevent divisions by zero, in some cases setting this to 0.1
	 * or 1 can help improve stability
	 */
	private final double epsilon = 0.1;

	public AMSGradOptimizer(GloveModel glove, int maxIterations, double tolerance) {
		super(glove, maxIterations, tolerance);

		// Increase dimension to make room for bias terms
		int dimension = this.dimension + 1;
		this.M1 = new double[2 * vocabSize * dimension];
		this.M2 = new double[2 * vocabSize * dimension];
	}
	
	@Override
	public String getName() {
		return "AMSGrad";
	}

	@Override
	public GloveJob createJob(int id, int iteration) {
		return () -> {
			int a, d, l1, l2;
			double m1, m2, v1, v2, grad_u, grad_v;
			double cost = 0, innerCost, weightedCost;

			final int offset = crecCount / numThreads * id;

			for (a = 0; a < linesPerThread[id]; a++) {
				int crWord1 = crecs.cIdx_I(a + offset);
				int crWord2 = crecs.cIdx_J(a + offset);
				double crVal = crecs.cIdx_C(a + offset);

				l1 = crWord1 * (dimension + 1);
				l2 = (crWord2 + vocabSize) * (dimension + 1);

				/* Calculate cost, save diff for gradients */
				innerCost = 0;
				for (d = 0; d < dimension; d++)
					innerCost += W[d + l1] * W[d + l2]; // dot product of word and context word vector
				// Add separate bias for
				innerCost += W[dimension + l1] + W[dimension + l2] - FastMath.log(crVal);

				// Check for NaN and inf() in the diffs.
				if (Double.isNaN(innerCost) || Double.isInfinite(innerCost)) {
					System.err.println("Caught NaN in diff for kdiff for thread. Skipping update");
					continue;
				}

				// multiply weighting function (f) with diff
				weightedCost = (crVal > xMax) ? innerCost : FastMath.pow(crVal / xMax, alpha) * innerCost;
				cost += 0.5 * weightedCost * innerCost; // weighted squared error


				/*---------------------------
				 * Adaptive gradient updates *
				 ---------------------------*/

				// Compute for word vectors
				for (d = 0; d < dimension; d++) {
					// Compute gradients
					grad_u = weightedCost * W[d + l2];
					grad_v = weightedCost * W[d + l1];
					// Update biased first and second moment estimates
					m1 = beta1 * M1[d + l1] + (1 - beta1) * grad_u;
					m2 = beta1 * M1[d + l2] + (1 - beta1) * grad_v;
					v1 = FastMath.max(M2[d + l1], beta2 * M2[d + l1] + (1 - beta2) * (grad_u * grad_u));
					v2 = FastMath.max(M2[d + l2], beta2 * M2[d + l2] + (1 - beta2) * (grad_v * grad_v));

					// Compute and apply updates
					W[d + l1] -= learningRate / (FastMath.sqrt(v1) + epsilon) * m1;
					W[d + l2] -= learningRate / (FastMath.sqrt(v2) + epsilon) * m2;
					// Store new moments
					M1[d + l1] = m1;
					M1[d + l2] = m2;
					M2[d + l1] = v1;
					M2[d + l2] = v2;
				}

				/*---------------------
				 * Compute for biases *
				 ---------------------*/

				// Update the first, second moment for the biases
				m1 = beta1 * M1[dimension + l1] + (1 - beta1) * weightedCost;
				m2 = beta1 * M1[dimension + l2] + (1 - beta1) * weightedCost;
				v1 = FastMath.max(M2[dimension + l1],
						beta2 * M2[dimension + l1] + (1 - beta2) * (weightedCost * weightedCost));
				v2 = FastMath.max(M2[dimension + l2],
						beta2 * M2[dimension + l2] + (1 - beta2) * (weightedCost * weightedCost));
				// Perform updates on bias terms
				W[dimension + l1] -= learningRate / (FastMath.sqrt(v1) + epsilon) * m1;
				W[dimension + l2] -= learningRate / (FastMath.sqrt(v2) + epsilon) * m2;
				// Store new moments
				M1[dimension + l1] = m1;
				M1[dimension + l2] = m2;
				M2[dimension + l1] = v1;
				M2[dimension + l2] = v2;
			}
			return cost;
		};
	}

}
