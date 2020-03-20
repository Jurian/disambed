package org.uu.nl.embedding.bca;

import me.tongfei.progressbar.ProgressBar;
import org.uu.nl.embedding.bca.jobs.DirectedWeighted;
import org.uu.nl.embedding.bca.jobs.UndirectedWeighted;
import org.uu.nl.embedding.bca.util.BCV;
import org.uu.nl.embedding.convert.util.InEdgeNeighborhoodAlgorithm;
import org.uu.nl.embedding.convert.util.OutEdgeNeighborhoodAlgorithm;
import org.uu.nl.embedding.util.CoOccurrenceMatrix;
import org.uu.nl.embedding.util.InMemoryRdfGraph;
import org.uu.nl.embedding.util.config.Configuration;
import org.uu.nl.embedding.util.rnd.Permutation;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.concurrent.*;

/**
 * @author Jurian Baas
 */
public class BookmarkColoring implements CoOccurrenceMatrix {

	private final ArrayList<Integer> coOccurrenceIdx_I;
	private final ArrayList<Integer> coOccurrenceIdx_J;
	private final ArrayList<Float> coOccurrenceValues;
	private double max;
	private final int vocabSize;
	private int coOccurrenceCount;
	private Permutation permutation;
	private final InMemoryRdfGraph graph;

	public BookmarkColoring(final InMemoryRdfGraph graph, final Configuration config) {

		final double alpha = config.getBca().getAlpha();
		final double epsilon = config.getBca().getEpsilon();
		final int[] jobs = graph.getVertices().toIntArray();

		this.graph = graph;
		this.vocabSize = jobs.length;

		this.coOccurrenceIdx_I = new ArrayList<>(vocabSize);
		this.coOccurrenceIdx_J = new ArrayList<>(vocabSize);
		this.coOccurrenceValues = new ArrayList<>(vocabSize);

		final int numThreads = config.getThreads();

		final ExecutorService es = Executors.newWorkStealingPool(numThreads);

		final int[][] inVertex = graph.getInNeighborhoods();
		final int[][] outVertex = graph.getOutNeighborhoods();
		final int[][] inEdge = new InEdgeNeighborhoodAlgorithm(config).compute(graph);
		final int[][] outEdge = new OutEdgeNeighborhoodAlgorithm(config).compute(graph);

		try(ProgressBar pb = Configuration.progressBar("BCA", jobs.length, "nodes")) {

			CompletionService<BCV> completionService = new ExecutorCompletionService<>(es);
			// Choose a graph neighborhood algorithm
			for(int bookmark : jobs) {

				if(config.getBca().isDirected()) {
					completionService.submit(new DirectedWeighted(
							graph, bookmark,
							alpha, epsilon,
							inVertex, outVertex, inEdge, outEdge));
				} else {
					completionService.submit(new UndirectedWeighted(
							graph, bookmark,
							alpha, epsilon,
							inVertex, outVertex, inEdge, outEdge));
				}
			}
			
			//now retrieve the futures after computation (auto wait for it)
			int received = 0;

			while(received < jobs.length) {

				try {
					final BCV bcv = completionService.take().get();

					switch (config.getBca().getNormalizeEnum()) {
						case UNITY:
							bcv.toUnity();
							break;
						case COUNTS:
							bcv.toCounts();
							break;
						default:
						case NONE:
							break;
					}

					// It is possible to use this maximum value in GloVe, although in the
					// literature they set this value to 100 and leave it at that
					setMax(bcv.max());

					for (Entry<Integer, Float> bcr : bcv.entrySet()) {
						coOccurrenceIdx_I.add(bcv.getRootNode());
						coOccurrenceIdx_J.add(bcr.getKey());
						coOccurrenceValues.add(bcr.getValue());
					}

					coOccurrenceCount += bcv.size();

				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				} finally {
					received ++;
					pb.step();
				}
			}

		} finally {
			es.shutdown();
		}

		permutation = new Permutation(coOccurrenceCount);
	}

	@Override
	public void shuffle() {
		permutation.shuffle();
	}
	
	public int cIdx_I(int i) {
		return this.coOccurrenceIdx_I.get(permutation.randomAccess(i));
	}
	
	public int cIdx_J(int j) {
		return this.coOccurrenceIdx_J.get(permutation.randomAccess(j));
	}
	
	public float cIdx_C(int i) {
		return this.coOccurrenceValues.get(permutation.randomAccess(i));
	}
	
	public byte getType(int index) {
		return (byte) this.graph.getVertexTypeProperty().getValueAsInt(index);
	}
	
	public int coOccurrenceCount() {
		return this.coOccurrenceCount;
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
		return this.graph.getVertexLabelProperty().getValueAsString(index);
	}
	
	private void setMax(double newMax) {
		this.max = Math.max(max, newMax);
	}

}
