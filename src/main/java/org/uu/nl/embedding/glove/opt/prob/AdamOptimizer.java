package org.uu.nl.embedding.glove.opt.prob;

import org.apache.commons.math.util.FastMath;
import org.uu.nl.embedding.glove.GloveModel;
import org.uu.nl.embedding.glove.opt.GloveJob;
import org.uu.nl.embedding.glove.opt.GloveOptimizer;

/**
 * <p>
 * Adaptive Moment Estimation (Adam) is another method that computes adaptive
 * learning rates for each parameter. In addition to storing an exponentially
 * decaying average of past squared gradients vt like Adadelta and RMSprop, Adam
 * also keeps an exponentially decaying average of past gradients mt, similar to
 * momentum.
 * </p>
 * <p>
 * Whereas momentum can be seen as a ball running down a slope, Adam behaves
 * like a heavy ball with friction, which thus prefers flat minima in the error
 * surface.
 * </p>
 * 
 * @see <a href="https://arxiv.org/pdf/1412.6980.pdf">Adam paper</a>
 * @see <a href="http://nlp.stanford.edu/projects/glove/">Stanford GloVe page</a>
 * @author Jurian Baas
 */
@SuppressWarnings("Duplicates")
public class AdamOptimizer extends GloveOptimizer {

	/**
	 * Contains the decaying averages of the past first momentums w.r.t. to all parameters
	 */
	private final double[] M1;
	/**
	 * Contains the decaying averages of the past second momentums w.r.t. to all parameters
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
	 * Mainly used to prevent divisions by zero, in some cases setting this to 0.1 or 1 can help improve stability
	 */
	private final double epsilon = 1e-8;
	
	public AdamOptimizer(GloveModel glove, int maxIterations, double tolerance) {
		super(glove, maxIterations, tolerance);
		
		// Increase dimension to make room for bias terms
		int dimension = this.dimension + 1;
		this.M1 = new double[2 * vocabSize * dimension];
		this.M2 = new double[2 * vocabSize * dimension];
	}
	
	@Override
	public String getName() {
		return "Adam";
	}
	
	@Override
	public GloveJob createJob(int id, int iteration) {
		return () -> {
			int a, d, l1, l2;
			double m1, m2, v1, v2, grad_u, grad_v;
			double cost = 0, innerCost, weightedCost;
			// From the paper, a slight improvement of efficiency can be obtained this way
			final double correction = learningRate * FastMath.sqrt(1 - FastMath.pow(beta2, iteration+1)) / (1 - FastMath.pow(beta1, iteration+1));
			final int offset = crecCount / numThreads * id;

			for (a = 0; a < linesPerThread[id]; a++) {
				int crWord1 = crecs.cIdx_I(a + offset);
				int crWord2 = crecs.cIdx_J(a + offset);
				double crVal = crecs.cIdx_C(a + offset);

				assert crVal > 0 && crVal < 1: "Co-occurrence is not between 0 and 1";

				l1 = crWord1 * (dimension + 1);
				l2 = (crWord2 + vocabSize) * (dimension + 1);

				/* Calculate cost, save diff for gradients */
				innerCost = 0;
				for (d = 0; d < dimension; d++)
					innerCost += W[d + l1] * W[d + l2]; // dot product of node and context node vector
				// Add separate bias for each node
				innerCost += W[dimension + l1] + W[dimension + l2] - FastMath.log(crVal/(1-crVal));

				// Check for NaN and inf() in the diffs.
				if (Double.isNaN(innerCost)  || Double.isInfinite(innerCost)) {
					System.err.println("Caught NaN in diff for kdiff for thread. Skipping update");
					continue;
				}

				weightedCost = crVal * innerCost;
				cost += 0.5 * weightedCost * innerCost; // weighted squared error

				/*---------------------------
				 * Adaptive gradient updates *
				 ---------------------------*/

				// Update the moments for the word vectors
				for (d = 0; d < dimension; d++) {
					// Compute gradients
					grad_u = weightedCost * W[d + l2];
					grad_v = weightedCost * W[d + l1];
					// Update biased first and second moment estimates
					m1 = beta1 * M1[d + l1] + (1 - beta1) * grad_u;
					m2 = beta1 * M1[d + l2] + (1 - beta1) * grad_v;
					v1 = beta2 * M2[d + l1] + (1 - beta2) * (grad_u*grad_u);
					v2 = beta2 * M2[d + l2] + (1 - beta2) * (grad_v*grad_v);
					// Compute and apply updates
					W[d + l1] -= correction * m1 / (FastMath.sqrt(v1) + epsilon);
					W[d + l2] -= correction * m2 / (FastMath.sqrt(v2) + epsilon);
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
				v1 = beta2 * M2[dimension + l1] + (1 - beta2) * (weightedCost*weightedCost);
				v2 = beta2 * M2[dimension + l2] + (1 - beta2) * (weightedCost*weightedCost);
				// Perform updates on bias terms
				W[dimension + l1] -= correction * m1 / (FastMath.sqrt(v1) + epsilon);
				W[dimension + l2] -= correction * m2 / (FastMath.sqrt(v2) + epsilon);
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
