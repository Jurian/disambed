package org.uu.nl.embedding.glove.opt.impl;

import org.apache.commons.math.util.FastMath;
import org.uu.nl.embedding.glove.GloveModel;
import org.uu.nl.embedding.glove.opt.GloveJob;
import org.uu.nl.embedding.glove.opt.GloveOptimizer;
import org.uu.nl.embedding.util.config.Configuration;

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
	 * Contains the decaying averages of the past first momentums w.r.t. to all parameters
	 */
	private final float[] M1focus, M1context;
	/**
	 * Contains the decaying averages of the past second momentums w.r.t. to all parameters
	 */
	private final float[] M2focus, M2context;
	/**
	 * Decay rate for first momentum
	 */
	private final float beta1 = 0.9F;
	/**
	 * Decay rate for second momentum
	 */
	private final float beta2 = 0.999F;
	/**
	 * Mainly used to prevent divisions by zero, in some cases setting this to 0.1
	 * or 1 can help improve stability
	 */
	private final double epsilon = 1e-8;

	public AMSGradOptimizer(GloveModel glove, Configuration config) {
		super(glove, config);

		// Increase dimension to make room for bias terms
		int dimension = this.dimension + 1;
		this.M1focus = new float[vocabSize * dimension];
		this.M2focus = new float[vocabSize * dimension];
		this.M1context = new float[vocabSize * dimension];
		this.M2context = new float[vocabSize * dimension];
	}
	
	@Override
	public String getName() {
		return "GloVe-AMSGrad";
	}

	@Override
	public GloveJob createJob(int id, int iteration) {
		return () -> {
			int a, d, l1, l2;
			float m1, m2, v1, v2, grad_u, grad_v;
			float cost = 0, innerCost, weightedCost;

			final int offset = coCount / numThreads * id;


			for (a = 0; a < linesPerThread[id]; a++) {

				int node1 = coMatrix.cIdx_I(a + offset);
				int node2 = coMatrix.cIdx_J(a + offset);
				float Xij = coMatrix.cIdx_C(a + offset);

				assert Xij >= 0 && Xij <= 1 : "Co-occurrence is not between 0 and 1: " + Xij;

				l1 = node1 * (dimension + 1);
				l2 = node2 * (dimension + 1);

				/* Calculate cost, save diff for gradients */
				innerCost = 0;

				for (d = 0; d < dimension; d++)
					innerCost += focus[d + l1] * context[d + l2]; // dot product of node and context node vector
				// Add separate bias for each node
				innerCost += focus[dimension + l1] + context[dimension + l2] - FastMath.log(Xij);

				weightedCost = Xij * innerCost;
				cost += 0.5 * weightedCost * innerCost; // weighted squared error

				/*---------------------------
				 * Adaptive gradient updates *
				 ---------------------------*/

				// Compute for node vectors
				for (d = 0; d < dimension; d++) {
					// Compute gradients
					grad_u = weightedCost * context[d + l2];
					grad_v = weightedCost * focus[d + l1];
					// Update biased first and second moment estimates
					m1 = beta1 * M1focus[d + l1] + (1 - beta1) * grad_u;
					m2 = beta1 * M1context[d + l2] + (1 - beta1) * grad_v;
					v1 = FastMath.max(M2focus[d + l1], beta2 * M2focus[d + l1] + (1 - beta2) * (grad_u * grad_u));
					v2 = FastMath.max(M2context[d + l2], beta2 * M2context[d + l2] + (1 - beta2) * (grad_v * grad_v));

					// Compute and apply updates
					focus[d + l1] -= learningRate / (FastMath.sqrt(v1) + epsilon) * m1;
					context[d + l2] -= learningRate / (FastMath.sqrt(v2) + epsilon) * m2;
					// Store new moments
					M1focus[d + l1] = m1;
					M1context[d + l2] = m2;
					M2focus[d + l1] = v1;
					M2context[d + l2] = v2;
				}

				/*---------------------
				 * Compute for biases *
				 ---------------------*/

				// Update the first, second moment for the biases
				m1 = beta1 * M1focus[dimension + l1] + (1 - beta1) * weightedCost;
				m2 = beta1 * M1context[dimension + l2] + (1 - beta1) * weightedCost;
				v1 = FastMath.max(M2focus[dimension + l1],
						beta2 * M2focus[dimension + l1] + (1 - beta2) * (weightedCost * weightedCost));
				v2 = FastMath.max(M2context[dimension + l2],
						beta2 * M2context[dimension + l2] + (1 - beta2) * (weightedCost * weightedCost));
				// Perform updates on bias terms
				focus[dimension + l1] -= learningRate / (FastMath.sqrt(v1) + epsilon) * m1;
				context[dimension + l2] -= learningRate / (FastMath.sqrt(v2) + epsilon) * m2;
				// Store new moments
				M1focus[dimension + l1] = m1;
				M1context[dimension + l2] = m2;
				M2focus[dimension + l1] = v1;
				M2context[dimension + l2] = v2;
			}
			return cost;
		};
	}

}
