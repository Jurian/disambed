package org.uu.nl.embedding.opt.grad;

import org.apache.commons.math.util.FastMath;
import org.uu.nl.embedding.opt.CostFunction;
import org.uu.nl.embedding.opt.OptimizeJob;
import org.uu.nl.embedding.opt.Optimizer;
import org.uu.nl.embedding.util.CoOccurrenceMatrix;
import org.uu.nl.embedding.util.config.Configuration;

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
@SuppressWarnings("DuplicatedCode")
public class Adam extends Optimizer {

	/**
	 * Contains the decaying averages of the past first moments w.r.t. to all parameters
	 */
	private final float[] M1focus, M1context;
	private final float[] M1fBias, M1cBias;
	/**
	 * Contains the decaying averages of the past second moments w.r.t. to all parameters
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
	 * Mainly used to prevent divisions by zero, in some cases setting this to 0.1 or 1 can help improve stability
	 */
	private final float epsilon = 1e-7f;
	
	public Adam(CoOccurrenceMatrix coMatrix, Configuration config, CostFunction costFunction) {
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
		return "Adam";
	}

	@Override
	public OptimizeJob createJob(int id, int iteration) {
		return () -> {

			int i, d, u, v, bu, bv, d1, d2;
			float Xij, m1, m2, v1, v2, grad_u, grad_v;
			float cost = 0, innerCost, weightedCost;
			final int offset = coCount / numThreads * id;

			// From the paper, a slight improvement of efficiency can be obtained this way
			final double correction = learningRate * FastMath.sqrt(1 - FastMath.pow(beta2, iteration + 1)) / (1 - FastMath.pow(beta1, iteration + 1));

			for (i = 0; i < linesPerThread[id]; i++) {

				bu = coMatrix.cIdx_I(i + offset); // Index of focus bias
				bv = coMatrix.cIdx_J(i + offset); // Index of context bias
				u = bu * dimension; // Index of focus vector
				v = bv * dimension; // Index of bias vector
				Xij = coMatrix.cIdx_C(i + offset); // Co-occurrence

				/* Calculate cost, save diff for gradients */
				innerCost = costFunction.innerCost(this, Xij, u, v, bu, bv);
				weightedCost = costFunction.weightedCost(this, innerCost, Xij);
				cost += 0.5 * weightedCost * innerCost; // weighted squared error

				/*---------------------------
				 * Adaptive gradient updates *
				 ---------------------------*/

				// Update the moments for the word vectors
				for (d = 0; d < dimension; d++) {

					d1 = d + u; // Index of specific dimension in focus vector
					d2 = d + v; // Index of specific dimension in context vector

					// Compute gradients
					grad_u = weightedCost * context[d2];
					grad_v = weightedCost * focus[d1];
					// Update biased first and second moment estimates
					m1 = beta1 * M1focus[d1] + (1 - beta1) * grad_u;
					m2 = beta1 * M1context[d2] + (1 - beta1) * grad_v;
					v1 = beta2 * M2focus[d1] + (1 - beta2) * (grad_u * grad_u);
					v2 = beta2 * M2context[d2] + (1 - beta2) * (grad_v * grad_v);
					// Compute and apply updates
					focus[d1] -= correction * m1 / (FastMath.sqrt(v1) + epsilon);
					context[d2] -= correction * m2 / (FastMath.sqrt(v2) + epsilon);
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
				v1 = beta2 * M2fBias[bu] + (1 - beta2) * (weightedCost * weightedCost);
				v2 = beta2 * M2cBias[bv] + (1 - beta2) * (weightedCost * weightedCost);
				// Perform updates on bias terms
				fBias[bu] -= correction * m1 / (FastMath.sqrt(v1) + epsilon);
				cBias[bv] -= correction * m2 / (FastMath.sqrt(v2) + epsilon);
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
