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
import org.uu.nl.embedding.util.sparse.RandomAccessSparseMatrix;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.*;

/**
 * @author Jurian Baas
 */
public class BookmarkColoring implements CoOccurrenceMatrix {

	private final RandomAccessSparseMatrix<Float> sparseMatrix;
	private double max;
	private final int focusVectors, contextVectors;
	private final Permutation permutation;
	private final InMemoryRdfGraph graph;
	private final Map<Integer, Integer> context2focus;
	private final Map<Integer, Integer> focus2context;

	public BookmarkColoring(final InMemoryRdfGraph graph, final Configuration config) {

		final double alpha = config.getBca().getAlpha();
		final double epsilon = config.getBca().getEpsilon();
		final int[] vertices = graph.getVertices().toIntArray();

		this.context2focus = new HashMap<>();
		this.focus2context = new HashMap<>();

		this.graph = graph;
		this.focusVectors = config.getOutput().getNodeIndex().size();
		this.contextVectors = vertices.length;
		this.sparseMatrix = new RandomAccessSparseMatrix<>(focusVectors, contextVectors, focusVectors*10);

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

		try(ProgressBar pb = Configuration.progressBar("BCA", focusVectors, "nodes")) {

			//now retrieve the futures after computation (auto wait for it)
			int received = 0;

			while(received < focusVectors) {

				try {

					final BCV bcv = completionService.take().get().toUnity();

					// It is possible to use this maximum value in GloVe, although in the
					// literature they set this value to 100 and leave it at that
					setMax(bcv.max());

					for (Entry<Integer, Float> bcr : bcv.entrySet()) {
						sparseMatrix.add(bcv.getRootNode(), bcr.getKey(), bcr.getValue());
					}

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

		permutation = new Permutation(sparseMatrix.getNonZero());
	}

	@Override
	public void shuffle() {
		permutation.shuffle();
	}
	
	public int cIdx_I(int k) {
		return contextIndex2Focus(sparseMatrix.getRow(permutation.randomAccess(k)));
	}
	
	public int cIdx_J(int k) {
		return this.sparseMatrix.getColumn(permutation.randomAccess(k));
	}
	
	public float cIdx_C(int k) {
		return this.sparseMatrix.getValue(permutation.randomAccess(k));
	}
	
	public byte getType(int index) {
		return (byte) this.graph.getVertexTypeProperty().getValueAsInt(focusIndex2Context(index));
	}
	
	public int coOccurrenceCount() {
		return this.sparseMatrix.getNonZero();
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
