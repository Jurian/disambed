package org.uu.nl.embedding.glove.opt.prob;

import org.apache.commons.math.util.FastMath;
import org.uu.nl.embedding.glove.GloveModel;
import org.uu.nl.embedding.glove.opt.GloveJob;
import org.uu.nl.embedding.glove.opt.GloveOptimizer;
import org.uu.nl.embedding.util.config.Configuration;


/**
 * <p>
 * Adagrad is an algorithm for gradient-based optimization that does just this:
 * It adapts the learning rate to the parameters, performing smaller updates
 * (i.e. low learning rates) for parameters associated with frequently occurring
 * features, and larger updates (i.e. high learning rates) for parameters
 * associated with infrequent features. For this reason, it is well-suited for
 * dealing with sparse data. Dean et al. have found that Adagrad greatly
 * improved the robustness of SGD.
 * </p>
 * <p>
 * Moreover, Pennington et al. used Adagrad to train GloVe word embeddings, as
 * infrequent words require much larger updates than frequent ones.
 * </p>
 *
 * @see <a href="http://nlp.stanford.edu/projects/glove/">Stanford GloVe page</a>
 * @see <a href="https://github.com/stanfordnlp/GloVe/blob/master/src/glove.c">Original C version by Jeffrey Pennington</a>
 * @author Jurian Baas
 */
@SuppressWarnings("Duplicates")
public class AdagradOptimizer extends GloveOptimizer {

	/**
	 * Contains the sum of the squares of the past gradients w.r.t. to all parameters
	 */
	private final float[] gradSqFocus, gradSqContext;

	public AdagradOptimizer(GloveModel glove, Configuration config) {
		super(glove, config);

		// Increase dimension to make room for bias terms
		int dimension = this.dimension + 1;
		this.gradSqFocus = new float[vocabSize * dimension];
		this.gradSqContext = new float[vocabSize * dimension];
		for (int i = 0; i < vocabSize; i++) {
			for (int d = 0; d < dimension; d++) {
				// So initial value of eta is equal to initial learning rate
				gradSqFocus[i * dimension + d] = gradSqContext[i * dimension + d] = 1;
			}

		}
	}
	
	@Override
	public String getName() {
		return "pGloVe-Adagrad";
	}

	@Override
	public GloveJob createJob(int id, int iteration) {
		return () -> {
			int a, d, l1, l2;
			float cost = 0, innerCost, weightedCost, grad1, grad2;

			final int offset = crecCount / numThreads * id;

			for (a = 0; a < linesPerThread[id]; a++) {

				int node1 = crecs.cIdx_I(a + offset);
				int node2 = crecs.cIdx_J(a + offset);
				float Xij = crecs.cIdx_C(a + offset);

				assert Xij >= 0 && Xij <= 1 : "Co-occurrence is not between 0 and 1: " + Xij;

				l1 = node1 * (dimension + 1);
				l2 = node2 * (dimension + 1);

				/* Calculate cost, save diff for gradients */
				innerCost = 0;

				for (d = 0; d < dimension; d++)
					innerCost += focus[d + l1] * context[d + l2]; // dot product of node and context node vector
				// Add separate bias for each node
				innerCost += focus[dimension + l1] + context[dimension + l2] - FastMath.log(Xij / (1 - Xij));

				weightedCost = Xij * innerCost;
				cost += 0.5 * weightedCost * innerCost; // weighted squared error

				/*---------------------------
				 * Adaptive gradient updates *
				 ---------------------------*/

				// Compute for word vectors
				for (d = 0; d < dimension; d++) {
					// Compute gradients
					grad1 = weightedCost * context[d + l2];
					grad2 = weightedCost * focus[d + l1];
					// Compute and apply updates
					focus[d + l1] -= grad1 / FastMath.sqrt(gradSqFocus[d + l1]) * learningRate;
					context[d + l2] -= grad2 / FastMath.sqrt(gradSqContext[d + l2]) * learningRate;
					// Store squared gradients
					gradSqFocus[d + l1] += grad1 * grad1;
					gradSqContext[d + l2] += grad2 * grad2;
				}

				/*---------------------
				 * Compute for biases *
				 ---------------------*/

				// Compute updates (gradient of bias is the weighted cost)
				focus[dimension + l1] -= weightedCost / FastMath.sqrt(gradSqFocus[dimension + l1]);
				context[dimension + l2] -= weightedCost / FastMath.sqrt(gradSqContext[dimension + l2]);
				weightedCost *= weightedCost;
				// Store squared gradients
				gradSqFocus[dimension + l1] += weightedCost;
				gradSqContext[dimension + l2] += weightedCost;

			}
			return cost;
		};
	}

}
