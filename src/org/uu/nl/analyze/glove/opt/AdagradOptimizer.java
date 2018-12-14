package org.uu.nl.analyze.glove.opt;

import org.apache.commons.math3.util.FastMath;
import org.uu.nl.analyze.glove.GloveModel;
import org.uu.nl.analyze.progress.Publisher;


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
public class AdagradOptimizer extends GloveOptimizer {

	/**
	 * Contains the sum of the squares of the past gradients w.r.t. to all parameters
	 */
	private final double[] gradsq;

	public AdagradOptimizer(GloveModel glove, int maxIterations, int numThreads, double tolerance, Publisher publisher) {
		super(glove, maxIterations, numThreads, tolerance, publisher);

		// Increase dimension to make room for bias terms
		int dimension = this.dimension + 1;
		this.gradsq = new double[2 * vocabSize * dimension];
		for (int i = 0; i < 2 * vocabSize; i++) {
			for (int d = 0; d < dimension; d++)
				gradsq[i * dimension + d] = 1; // So initial value of eta is equal to initial learning rate
		}
	}
	
	@Override
	public String getName() {
		return "Adagrad";
	}
	
	@Override
	public GloveJob createJob(int id, int iteration) {
		return new GloveJob(id) {

			@Override
			public Double call() {
				
				int a, d, l1, l2;
				double cost = 0, innerCost, weightedCost, grad1, grad2;

				final int offset = crecCount / numThreads * id;

				for (a = 0; a < linesPerThread[id]; a++) {
					int crWord1 = crecs.cIdx_I(a + offset);
					int crWord2 = crecs.cIdx_J(a + offset);
					double crVal = crecs.cIdx_C(a + offset);

					l1 = crWord1 * (dimension + 1);
					l2 = (crWord2 + vocabSize) * (dimension + 1);

					/* Calculate cost, save diff for gradients */
					innerCost = 0;
					// dot product of word and context word vector
					for (d = 0; d < dimension; d++)
						innerCost += W[d + l1] * W[d + l2];
					// add separate bias for each word
					innerCost += W[dimension + l1] + W[dimension + l2] - FastMath.log(crVal);
					// multiply weighting function (f) with diff
					weightedCost = (crVal > xMax) ? innerCost : FastMath.pow(crVal / xMax, alpha) * innerCost;

					// Check for NaN and inf() in the diffs.
					if (Double.isNaN(innerCost) || Double.isNaN(weightedCost) || Double.isInfinite(innerCost)
							|| Double.isInfinite(weightedCost)) {
						System.err.println("Caught NaN in diff for kdiff for thread. Skipping update");
						continue;
					}

					cost += 0.5 * weightedCost * innerCost; // weighted squared error

					/* Adaptive gradient updates */
					//weightedCost *= learningRate; // for ease in calculating gradient
					
					/****************************
					 * Compute for word vectors *
					 ***************************/
					
					for (d = 0; d < dimension; d++) {
						// Compute gradients
						grad1 = weightedCost * W[d + l2];
						grad2 = weightedCost * W[d + l1];
						// Compute and apply updates
						W[d + l1] -= grad1 / FastMath.sqrt(gradsq[d + l1]) * learningRate;
						W[d + l2] -= grad2 / FastMath.sqrt(gradsq[d + l2]) * learningRate;
						// Store squared gradients
						gradsq[d + l1] += grad1 * grad1;
						gradsq[d + l2] += grad2 * grad2;
					}
					
					/**********************
					 * Compute for biases *
					 **********************/
					
					// Compute updates (gradient of bias is the weighted cost)
					W[dimension + l1] -= weightedCost / FastMath.sqrt(gradsq[dimension + l1]);
					W[dimension + l2] -= weightedCost / FastMath.sqrt(gradsq[dimension + l2]);
					weightedCost *= weightedCost;
					// Store squared gradients
					gradsq[dimension + l1] += weightedCost;
					gradsq[dimension + l2] += weightedCost;

				}
				return cost;
			}
		};
	}

}
