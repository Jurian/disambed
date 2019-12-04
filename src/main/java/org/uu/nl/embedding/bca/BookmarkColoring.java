package org.uu.nl.embedding.bca;

import grph.Grph;
import me.tongfei.progressbar.ProgressBar;
import org.uu.nl.embedding.CRecMatrix;
import org.uu.nl.embedding.Settings;
import org.uu.nl.embedding.bca.jobs.DirectedUnweighted;
import org.uu.nl.embedding.bca.jobs.DirectedWeighted;
import org.uu.nl.embedding.bca.jobs.DirectedWeightedLiteral;
import org.uu.nl.embedding.bca.jobs.UndirectedWeighted;
import org.uu.nl.embedding.bca.util.BCAOptions;
import org.uu.nl.embedding.bca.util.BCV;
import org.uu.nl.embedding.bca.util.GraphStatistics;
import org.uu.nl.embedding.convert.util.InEdgeNeighborhoodAlgorithm;
import org.uu.nl.embedding.convert.util.OutEdgeNeighborhoodAlgorithm;
import org.uu.nl.embedding.util.rnd.Permutation;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.*;

/**
 * @author Jurian Baas
 */
public class BookmarkColoring implements CRecMatrix {

	private final String[] dict;
	private final GraphStatistics stats;
	private final byte[] types;
	private final int[] coOccurrenceIdx_I;
	private final int[] coOccurrenceIdx_J;
	private final double[] coOccurrenceValues;
	private final double alpha, epsilon;
	private double max;
	private final int vocabSize;
	private int coOccurrenceCount;
	private final boolean includeReverse, usePredicates;
	private Permutation permutation;

	private static final Settings settings = Settings.getInstance();

	public BookmarkColoring(Grph graph, BCAOptions options) {

		this.includeReverse = options.isReverse();
		this.usePredicates = options.includePredicates();
		this.alpha = options.getAlpha();
		this.epsilon = options.getEpsilon();
		
		this.stats = new GraphStatistics(graph, options.getWeights(), usePredicates);
		this.types = stats.types;
		this.dict = stats.dict;
		this.vocabSize = usePredicates ? stats.nrOfVertices + stats.nrOfEdgeTypes : stats.nrOfVertices;

		final int numThreads = settings.threads();
		
		final Map<Integer, BCV> bcVectors = new ConcurrentHashMap<>(vocabSize);
		final ExecutorService es = Executors.newWorkStealingPool(numThreads);

		final int[][] inVertex = graph.getInNeighborhoods();
		final int[][] outVertex = graph.getOutNeighborhoods();
		final int[][] inEdge = new InEdgeNeighborhoodAlgorithm().compute(graph);
		final int[][] outEdge = new OutEdgeNeighborhoodAlgorithm().compute(graph);

		try(ProgressBar pb = settings.progressBar("BCA", stats.jobs.length, "nodes")) {

			CompletionService<BCV> completionService = new ExecutorCompletionService<>(es);
			// Choose a graph neighborhood algorithm
			for(int bookmark : stats.jobs) {
				switch(options.getType()) {
				default:
				case DIRECTED_UNWEIGHTED:
					completionService.submit(new DirectedUnweighted(
							graph, bookmark,
							includeReverse, usePredicates, alpha, epsilon,
							inVertex, outVertex, inEdge, outEdge));
					break;
				case DIRECTED_WEIGHTED:
					completionService.submit(new DirectedWeighted(
							graph, bookmark, stats.weights,
							includeReverse, usePredicates, alpha, epsilon,
							inVertex, outVertex, inEdge, outEdge));
					break;
				case DIRECTED_WEIGHTED_LITERAL:
					completionService.submit(new DirectedWeightedLiteral(
							graph, bookmark, stats.weights,
							includeReverse, usePredicates, alpha, epsilon,
							inVertex, outVertex, inEdge, outEdge));
					break;
				case UNDIRECTED_WEIGHTED:
					completionService.submit(new UndirectedWeighted(
							graph, bookmark, stats.weights,
							usePredicates, alpha, epsilon,
							inVertex, outVertex, inEdge, outEdge));
					break;
				}
			}
			
			//now retrieve the futures after computation (auto wait for it)
			int received = 0;

			while(received < stats.jobs.length) {

				try {
					BCV bcv = completionService.take().get();
					// We have to collect all the BCV's first before we can store them
					// in a more efficient lookup friendly way below

					bcv.normalize();

					bcv.negativeSampling(stats.nrOfVertices, options.getNegativeSamples());

					bcVectors.put(bcv.getRootNode(), bcv);
					coOccurrenceCount += bcv.size();

					// It is possible to use this maximum value in GloVe, although in the
					// literature they set this value to 100 and leave it at that
					setMax(bcv.max());

				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();

				} finally {

					received ++;
					pb.step();
				}
			}

			pb.setExtraMessage("Processed " + stats.jobs.length + " jobs");

		} finally {
			es.shutdown();
		}

		this.coOccurrenceValues = new double[coOccurrenceCount];
		this.coOccurrenceIdx_I = new int[coOccurrenceCount];
		this.coOccurrenceIdx_J = new int[coOccurrenceCount];
		
		{
			int i = 0;
			for(Entry<Integer, BCV> entry : bcVectors.entrySet()) {
				for (Entry<Integer, Double> bcr : entry.getValue().entrySet()) {
					this.coOccurrenceIdx_I[i] = entry.getKey();
					this.coOccurrenceIdx_J[i] = bcr.getKey();
					this.coOccurrenceValues[i] = bcr.getValue();
					i++;
				}
			}
		}

		permutation = new Permutation(coOccurrenceCount);
	}

	@Override
	public void shuffle() {
		permutation.shuffle();
	}

	
	public int cIdx_I(int i) {
		return this.coOccurrenceIdx_I[permutation.randomAccess(i)];
	}
	
	public int cIdx_J(int j) {
		return this.coOccurrenceIdx_J[permutation.randomAccess(j)];
	}
	
	public double cIdx_C(int i) {
		return this.coOccurrenceValues[permutation.randomAccess(i)];
	}
	
	public byte getType(int index) {
		return this.types[index];
	}
	
	public int coOccurrenceCount() {
		return this.coOccurrenceCount;
	}

	/**
	 * Retention coefficient
	 */
	public double getAlpha() {
		return alpha;
	}

	/**
	 * Tolerance threshold
	 */
	public double getEpsilon() {
		return epsilon;
	}
	
	@Override
	public int vocabSize() {
		return this.vocabSize;
	}
	
	@Override
	public double max() {
		return this.max;
	}
	
	@Override
	public String getKey(int index) {
		return this.dict[index];
	}
	
	private void setMax(double newMax) {
		if(newMax > max) max = newMax;
	}
	
	@Override
	public int getNrOfVertices() {
		return this.stats.nrOfVertices;
	}


}
