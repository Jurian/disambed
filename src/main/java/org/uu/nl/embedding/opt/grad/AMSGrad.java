package org.uu.nl.embedding.opt.grad;

import org.apache.commons.math.util.FastMath;
import org.uu.nl.embedding.opt.*;
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
@SuppressWarnings("DuplicatedCode")
public class AMSGrad extends Optimizer {

	/**
	 * Contains the maximum of the past first momentums w.r.t. to all parameters
	 */
	private final float[] M1focus, M1context;
	private final float[] M1fBias, M1cBias;
	/**
	 * Contains the maximum of the past second momentums w.r.t. to all parameters
	 */
	private final float[] M2focus, M2context;
	private final float[] M2fBias, M2cBias;
	/**
	 * Decay rate for first momentum
	 */
	private final float beta1 = 0.9f;
	/**
	 * Decay rate for second momentum
	 */
	private final float beta2 = 0.999f;
	/**
	 * Mainly used to prevent divisions by zero, in some cases setting this to 0.1
	 * or 1 can help improve stability
	 */
	private final float epsilon = 1e-7f;

	public AMSGrad(OptimizerModel optimizerModel, Configuration config, CostFunction costFunction) {
		super(optimizerModel, config, costFunction);

		this.M1focus = new float[vocabSize * dimension];
		this.M2focus = new float[vocabSize * dimension];
		this.M1context = new float[vocabSize * dimension];
		this.M2context = new float[vocabSize * dimension];
		this.M1fBias = new float[vocabSize];
		this.M2fBias = new float[vocabSize];
		this.M1cBias = new float[vocabSize];
		this.M2cBias = new float[vocabSize];
	}
	
	@Override
	public String getName() {
		return "AMSGrad";
	}

	@Override
	public OptimizeJob createJob(int id, int iteration) {

		return () -> {
			int a, d, u, v, bu, bv, d1, d2;
			float Xij, m1, m2, v1, v2, grad_u, grad_v;
			float cost = 0, innerCost, weightedCost;

			final int offset = coCount / numThreads * id;
			for (a = 0; a < linesPerThread[id]; a++) {

				bu = coMatrix.cIdx_I(a + offset); // Index of focus bias
				bv = coMatrix.cIdx_J(a + offset); // Index of context bias
				u = bu * dimension; // Index of focus vector
				v = bv * dimension; // Index of bias vector
				Xij = coMatrix.cIdx_C(a + offset); // Co-occurrence

				/* Calculate cost, save diff for gradients */
				innerCost = costFunction.innerCost(this, Xij, u, v, bu, bv);
				weightedCost = costFunction.weightedCost(this, innerCost, Xij);
				cost += 0.5 * weightedCost * innerCost; // weighted squared error

				/*---------------------------
				 * Adaptive gradient updates *
				 ---------------------------*/

				// Compute for node vectors
				for (d = 0; d < dimension; d++) {

					d1 = d + u; // Index of specific dimension in focus vector
					d2 = d + v; // Index of specific dimension in context vector

					// Compute gradients
					grad_u = weightedCost * context[d2];
					grad_v = weightedCost * focus[d1];
					// Update biased first and second moment estimates
					m1 = beta1 * M1focus[d1] + (1 - beta1) * grad_u;
					m2 = beta1 * M1context[d2] + (1 - beta1) * grad_v;
					v1 = FastMath.max(M2focus[d1], beta2 * M2focus[d1] + (1 - beta2) * (grad_u * grad_u));
					v2 = FastMath.max(M2context[d2], beta2 * M2context[d2] + (1 - beta2) * (grad_v * grad_v));

					// Compute and apply updates
					focus[d1] -= learningRate / (FastMath.sqrt(v1) + epsilon) * m1;
					context[d2] -= learningRate / (FastMath.sqrt(v2) + epsilon) * m2;
					// Store new moments
					M1focus[d1] = m1;
					M1context[d2] = m2;
					M2focus[d1] = v1;
					M2context[d2] = v2;
				}

				/*---------------------
				 * Compute for biases *
				 ---------------------*/

				// Update the first, second moment for the biases
				m1 = beta1 * M1fBias[bu] + (1 - beta1) * weightedCost;
				m2 = beta1 * M1cBias[bv] + (1 - beta1) * weightedCost;
				v1 = FastMath.max(M2fBias[bu], beta2 * M2fBias[bu] + (1 - beta2) * (weightedCost * weightedCost));
				v2 = FastMath.max(M2cBias[bv], beta2 * M2cBias[bv] + (1 - beta2) * (weightedCost * weightedCost));
				// Perform updates on bias terms
				fBias[bu] -= learningRate / (FastMath.sqrt(v1) + epsilon) * m1;
				cBias[bv] -= learningRate / (FastMath.sqrt(v2) + epsilon) * m2;
				// Store new moments
				M1fBias[bu] = m1;
				M1cBias[bv] = m2;
				M2fBias[bu] = v1;
				M2cBias[bv] = v2;
			}
			return cost;
		};
	}
}
