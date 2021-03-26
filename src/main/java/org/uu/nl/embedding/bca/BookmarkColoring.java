package org.uu.nl.embedding.bca;

import me.tongfei.progressbar.ProgressBar;
import org.uu.nl.embedding.bca.jobs.UndirectedWeighted;
import org.uu.nl.embedding.bca.util.BCV;
import org.uu.nl.embedding.convert.util.EdgeNeighborhoodAlgorithm;
import org.uu.nl.embedding.convert.util.VertexNeighborhoodAlgorithm;
import org.uu.nl.embedding.util.CoOccurrenceMatrix;
import org.uu.nl.embedding.util.InMemoryRdfGraph;
import org.uu.nl.embedding.util.config.Configuration;
import org.uu.nl.embedding.util.rnd.Permutation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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
	private final int focusVectors, contextVectors;
	private int coOccurrenceCount;
	private final Permutation permutation;
	private final InMemoryRdfGraph graph;
	private final Map<Integer, Integer> context2focus;
	private final Map<Integer, Integer> focus2context;

	public BookmarkColoring(final InMemoryRdfGraph graph, final Configuration config) {

		final double alpha = config.getBca().getAlpha();
		final double epsilon = config.getBca().getEpsilon();
		final int[] vertices = graph.getVertices().toIntArray();

		final Configuration.Output output = config.getOutput();

		this.context2focus = new HashMap<>();
		this.focus2context = new HashMap<>();

		int notSkipped = config.getOutput().getNodeIndex().size();

		this.graph = graph;
		this.focusVectors = notSkipped;
		this.contextVectors = vertices.length;
		this.coOccurrenceIdx_I = new ArrayList<>(notSkipped);
		this.coOccurrenceIdx_J = new ArrayList<>(notSkipped);
		this.coOccurrenceValues = new ArrayList<>(notSkipped);

		final int numThreads = config.getThreads();

		final ExecutorService es = Executors.newWorkStealingPool(numThreads);

		final int[][] edgeNeighborhoods = new EdgeNeighborhoodAlgorithm(config).compute(graph);
		final int[][] vertexNeighborhoods = new VertexNeighborhoodAlgorithm(config).compute(graph);

		CompletionService<BCV> completionService = new ExecutorCompletionService<>(es);

		int j = 0;
		for(int bookmark : config.getOutput().getNodeIndex()) {

			context2focus.put(bookmark, j);
			focus2context.put(j, bookmark);
			j++;

			completionService.submit(new UndirectedWeighted(
					graph, bookmark,
					alpha, epsilon,
					vertexNeighborhoods, edgeNeighborhoods));
		}

		try(ProgressBar pb = Configuration.progressBar("BCA", notSkipped, "nodes")) {

			//now retrieve the futures after computation (auto wait for it)
			int received = 0;

			while(received < notSkipped) {

				try {

					final BCV bcv = completionService.take().get().toUnity();

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
		return contextIndex2Focus(coOccurrenceIdx_I.get(permutation.randomAccess(i)));
	}
	
	public int cIdx_J(int j) {
		return this.coOccurrenceIdx_J.get(permutation.randomAccess(j));
	}
	
	public float cIdx_C(int i) {
		return this.coOccurrenceValues.get(permutation.randomAccess(i));
	}
	
	public byte getType(int index) {
		return (byte) this.graph.getVertexTypeProperty().getValueAsInt(focusIndex2Context(index));
	}
	
	public int coOccurrenceCount() {
		return this.coOccurrenceCount;
	}


	@Override
	public InMemoryRdfGraph getGraph() {
		return graph;
	}

	@Override
	public int contextIndex2Focus(int i) {
		return context2focus.get(i);
	}

	@Override
	public int focusIndex2Context(int i) {
		return focus2context.get(i);
	}

	@Override
	public int nrOfContextVectors() {
		return contextVectors;
	}

	@Override
	public int nrOfFocusVectors() {
		return focusVectors;
	}

	@Override
	public double max() {
		return this.max;
	}
	
	@Override
	public String getKey(int index) {
		return this.graph.getVertexLabelProperty().getValueAsString(focusIndex2Context(index));
	}
	
	private void setMax(double newMax) {
		this.max = Math.max(max, newMax);
	}
}
