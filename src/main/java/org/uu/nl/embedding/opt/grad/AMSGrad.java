package org.uu.nl.embedding.opt.grad;

import org.apache.commons.math.util.FastMath;
import org.uu.nl.embedding.opt.CostFunction;
import org.uu.nl.embedding.opt.OptimizeJob;
import org.uu.nl.embedding.opt.Optimizer;
import org.uu.nl.embedding.util.CoOccurrenceMatrix;
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
	 * Contains the maximum of the past first moments w.r.t. to all parameters
	 */
	private final float[] M1focus, M1context;
	private final float[] M1fBias, M1cBias;
	/**
	 * Contains the maximum of the past second moments w.r.t. to all parameters
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
	private final float epsilon = 1e-1f;

	public AMSGrad(CoOccurrenceMatrix coMatrix, Configuration config, CostFunction costFunction) {
		super(coMatrix, config, costFunction);

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

			int i, d, i_u, i_v, bu, bv, d1, d2;
			float Xij, m, v, grad_u, grad_v;
			float cost = 0, innerCost, weightedCost;
			final int offset = coCount / numThreads * id;

			for (i = 0; i < linesPerThread[id]; i++) {

				bu = coMatrix.cIdx_I(i + offset); // Index of focus bias
				bv = coMatrix.cIdx_J(i + offset); // Index of context bias
				i_u = bu * dimension; // Index of focus vector
				i_v = bv * dimension; // Index of bias vector
				Xij = coMatrix.cIdx_C(i + offset); // Co-occurrence

				/* Calculate cost, save diff for gradients */
				innerCost = costFunction.innerCost(this, Xij, i_u, i_v, bu, bv);
				weightedCost = costFunction.weightedCost(this, innerCost, Xij);
				cost += 0.5 * weightedCost * innerCost; // weighted squared error

				/*---------------------------
				 * Adaptive gradient updates *
				 ---------------------------*/

				// Compute for node vectors
				for (d = 0; d < dimension; d++) {

					d1 = d + i_u; // Index of specific dimension in focus vector
					d2 = d + i_v; // Index of specific dimension in context vector

					// Compute gradients
					grad_u = weightedCost * context[d2];
					grad_v = weightedCost * focus[d1];

					m = beta1 * M1focus[d1] + (1 - beta1) * grad_u;
					v = FastMath.max(M2focus[d1], beta2 * M2focus[d1] + (1 - beta2) * (grad_u * grad_u));
					focus[d1] -= learningRate / (FastMath.sqrt(v) + epsilon) * m;
					M1focus[d1] = m;
					M2focus[d1] = v;

					m = beta1 * M1context[d2] + (1 - beta1) * grad_v;
					v = FastMath.max(M2context[d2], beta2 * M2context[d2] + (1 - beta2) * (grad_v * grad_v));
					context[d2] -= learningRate / (FastMath.sqrt(v) + epsilon) * m;
					M1context[d2] = m;
					M2context[d2] = v;
				}

				/*---------------------
				 * Compute for biases *
				 ---------------------*/

				// Update the first, second moment for the biases
				m = beta1 * M1fBias[bu] + (1 - beta1) * weightedCost;
				v = FastMath.max(M2fBias[bu], beta2 * M2fBias[bu] + (1 - beta2) * (weightedCost * weightedCost));
				fBias[bu] -= learningRate / (FastMath.sqrt(v) + epsilon) * m;
				M1fBias[bu] = m;
				M2fBias[bu] = v;

				m = beta1 * M1cBias[bv] + (1 - beta1) * weightedCost;
				v = FastMath.max(M2cBias[bv], beta2 * M2cBias[bv] + (1 - beta2) * (weightedCost * weightedCost));
				cBias[bv] -= learningRate / (FastMath.sqrt(v) + epsilon) * m;
				M1cBias[bv] = m;
				M2cBias[bv] = v;
			}
			return cost;
		};
	}
}
