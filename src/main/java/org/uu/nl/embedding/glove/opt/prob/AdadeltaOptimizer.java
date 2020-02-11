package org.uu.nl.embedding.glove.opt.prob;

import org.apache.commons.math.util.FastMath;
import org.uu.nl.embedding.glove.GloveModel;
import org.uu.nl.embedding.glove.opt.GloveJob;
import org.uu.nl.embedding.glove.opt.GloveOptimizer;


/**
 * <p>
 * Adadelta is an extension of Adagrad that seeks to reduce its aggressive,
 * monotonically decreasing learning rate. Instead of accumulating all past
 * squared gradients, Adadelta restricts the window of accumulated past
 * gradients to some fixed size w.
 * </p>
 * <p>
 * Instead of inefficiently storing previous squared gradients, the sum of
 * gradients is recursively defined as a decaying average of all past squared
 * gradients. The running average E[g2]t at time step then depends (as a
 * fraction γ similarly to the Momentum term) only on the previous average and
 * the current gradient.
 * </p>
 * <p>
 *     With Adadelta, we do not need to set a default learning rate, as it has been eliminated from the update rule.
 * </p>
 * @see <a href="https://arxiv.org/pdf/1212.5701.pdf">Adadelta paper</a>
 * @see <a href="http://nlp.stanford.edu/projects/glove/">Stanford GloVe page</a>
 * @author Jurian Baas
 */
@SuppressWarnings("Duplicates")
public class AdadeltaOptimizer extends GloveOptimizer {

	/**
	 * Contains the decaying averages of the past gradients w.r.t. to all parameters
	 */
	private final double[] G;
	/**
	 * Contains the decaying averages of the past updates w.r.t. to all parameters
	 */
	private final double[] D;
	/**
	 * Decay rate for accumulated gradients and updates
	 */
	private final double gamma = 0.9;
	/**
	 * Mainly used to prevent division by zero
	 */
	private final double epsilon = 1e-8;

	public AdadeltaOptimizer(GloveModel glove, int maxIterations, double tolerance) {
		super(glove, maxIterations, tolerance);

		// Increase dimension to make room for bias terms
		int dimension = this.dimension + 1;
		this.G = new double[2 * vocabSize * dimension];
		this.D = new double[2 * vocabSize * dimension];
	}
	
	@Override
	public String getName() {
		return "pGloVe-Adadelta";
	}
	
	@Override
	public GloveJob createJob(int id, int iteration) {
		return () -> {
			int a, d, l1, l2;
			double cost = 0, innerCost, weightedCost, grad1, grad2;
			double RMSx1, RMSx2, accg1, accg2, RMSg1, RMSg2, deltax1, deltax2, accx1, accx2;

			final int offset = crecCount / numThreads * id;

			for (a = 0; a < linesPerThread[id]; a++) {

				int node1 = crecs.cIdx_I(a + offset);
				int node2 = crecs.cIdx_J(a + offset);
				double Xij = crecs.cIdx_C(a + offset);

				assert Xij >= 0 && Xij <= 1: "Co-occurrence is not between 0 and 1: " + Xij;

				l1 = node1 * (dimension + 1);
				l2 = (node2 + vocabSize) * (dimension + 1);

				/* Calculate cost, save diff for gradients */
				innerCost = 0;

				for (d = 0; d < dimension; d++)
					innerCost += W[d + l1] * W[d + l2]; // dot product of node and context node vector
				// Add separate bias for each node
				innerCost += W[dimension + l1] + W[dimension + l2] - FastMath.log(Xij/(1-Xij));

				weightedCost = Xij * innerCost;
				cost += 0.5 * weightedCost * innerCost; // weighted squared error

				/*---------------------------
				 * Adaptive gradient updates *
				 ---------------------------*/

				for (d = 0; d < dimension; d++) {
					// Compute gradients
					grad1 = weightedCost * W[d + l2];
					grad2 = weightedCost * W[d + l1];
					// Compute RMS of previous accumulated updates
					RMSx1 = FastMath.sqrt(D[d + l1] + epsilon);
					RMSx2 = FastMath.sqrt(D[d + l2] + epsilon);
					// Accumulate gradients
					accg1 = gamma * G[d + l1] + (1 - gamma) * (grad1 * grad1);
					accg2 = gamma * G[d + l2] + (1 - gamma) * (grad2 * grad2);
					// Compute RMS of current accumulated gradients
					RMSg1 = FastMath.sqrt(accg1 + epsilon);
					RMSg2 = FastMath.sqrt(accg2 + epsilon);
					// Compute updates
					deltax1 = RMSx1 / RMSg1 * grad1;
					deltax2 = RMSx2 / RMSg2 * grad2;
					// Accumulate updates
					accx1 = gamma * D[d + l1] + (1 - gamma) * (deltax1 * deltax1);
					accx2 = gamma * D[d + l2] + (1 - gamma) * (deltax2 * deltax2);
					// Apply updates
					W[d + l1] -= deltax1;
					W[d + l2] -= deltax2;
					// Store accumulated gradients and updates
					G[d + l1] = accg1;
					G[d + l2] = accg2;
					D[d + l1] = accx1;
					D[d + l2] = accx2;
				}

				/*---------------------
				 * Compute for biases *
				 ---------------------*/

				// Compute RMS of previous accumulated updates
				RMSx1 = FastMath.sqrt(D[dimension + l1] + epsilon);
				RMSx2 = FastMath.sqrt(D[dimension + l2] + epsilon);
				// Accumulate gradients
				accg1 = gamma * G[dimension + l1] + (1 - gamma) * (weightedCost * weightedCost);
				accg2 = gamma * G[dimension + l2] + (1 - gamma) * (weightedCost * weightedCost);
				// Compute RMS of current accumulated gradients
				RMSg1 = FastMath.sqrt(accg1 + epsilon);
				RMSg2 = FastMath.sqrt(accg2 + epsilon);
				// Compute update
				deltax1 = RMSx1 / RMSg1 * weightedCost;
				deltax2 = RMSx2 / RMSg2 * weightedCost;
				// Accumulate updates
				accx1 = gamma * D[dimension + l1] + (1 - gamma) * (deltax1 * deltax1);
				accx2 = gamma * D[dimension + l2] + (1 - gamma) * (deltax2 * deltax2);
				// Apply updates
				W[dimension + l1] -= deltax1;
				W[dimension + l2] -= deltax2;
				// Store accumulated gradients and updates
				G[dimension + l1] = accg1;
				G[dimension + l2] = accg2;
				D[dimension + l1] = accx1;
				D[dimension + l2] = accx2;
			}
			return cost;
		};
	}
}
