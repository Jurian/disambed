package org.uu.nl.embedding.bca;

import com.carrotsearch.hppc.IntIntHashMap;
import com.carrotsearch.hppc.cursors.IntCursor;
import com.carrotsearch.hppc.cursors.IntFloatCursor;
import me.tongfei.progressbar.ProgressBar;
import org.uu.nl.embedding.bca.jobs.UndirectedWeighted;
import org.uu.nl.embedding.bca.util.BCV;
import org.uu.nl.embedding.convert.util.EdgeNeighborhoodAlgorithm;
import org.uu.nl.embedding.convert.util.VertexNeighborhoodAlgorithm;
import org.uu.nl.embedding.util.CoOccurrenceMatrix;
import org.uu.nl.embedding.util.InMemoryRdfGraph;
import org.uu.nl.embedding.util.Progress;
import org.uu.nl.embedding.util.config.EmbeddingConfiguration;
import org.uu.nl.embedding.util.rnd.Permutation;
import org.uu.nl.embedding.util.sparse.RandomAccessSparseMatrix;

import java.util.concurrent.*;

/**
 * @author Jurian Baas
 */
public class BookmarkColoring implements CoOccurrenceMatrix {

	private final RandomAccessSparseMatrix sparseMatrix;
	private float max;
	private final int focusVectors, contextVectors;
	private final Permutation permutation;
	private final InMemoryRdfGraph graph;
	private final IntIntHashMap context2focus;
	private final int[] focus2context;

	public BookmarkColoring(final InMemoryRdfGraph graph, final EmbeddingConfiguration config) {

		final float alpha = config.getBca().getAlpha();
		final float epsilon = config.getBca().getEpsilon();

		this.graph = graph;
		this.focusVectors = graph.getFocusNodes().size();
		this.contextVectors = graph.getNumberOfVertices();
		this.sparseMatrix = new RandomAccessSparseMatrix(focusVectors, contextVectors, focusVectors*100);

		this.context2focus = new IntIntHashMap();
		this.focus2context = new int[focusVectors];

		final int numThreads = config.getThreads();

		final ExecutorService es = Executors.newWorkStealingPool(numThreads);

		final int[][] edgeNeighborhoods = new EdgeNeighborhoodAlgorithm(numThreads).compute(graph);
		final int[][] vertexNeighborhoods = new VertexNeighborhoodAlgorithm(numThreads).compute(graph);

		CompletionService<BCV> completionService = new ExecutorCompletionService<>(es);

		int j = 0;

		for(IntCursor c : graph.getFocusNodes()) {
			int bookmark = c.value;
			context2focus.put(bookmark, j);
			focus2context[j] = bookmark;
			j++;

			completionService.submit(new UndirectedWeighted(
					graph, bookmark,
					alpha, epsilon,
					vertexNeighborhoods, edgeNeighborhoods));
		}

		double avgSize = 0;
		try(ProgressBar pb = Progress.progressBar("BCA", focusVectors, "nodes")) {

			//now retrieve the futures after computation (auto wait for it)
			int received = 0;

			while(received < focusVectors) {

				try {

					final BCV bcv = completionService.take().get().scale();

					// It is possible to use this maximum value in GloVe, although in the
					// literature they set this value to 100 and leave it at that
					setMax(bcv.max());

					for(IntFloatCursor c : bcv) {
						sparseMatrix.add(bcv.getRootNode(), c.key, c.value);
					}

					avgSize = avgSize + ((bcv.size() - avgSize) / (received + 1));

				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				} finally {
					received ++;
					pb.step();
					pb.setExtraMessage(Math.round(avgSize * 100.0) / 100.0 + " " + calculateMemoryMegaBytes() +"MB");
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

	/**
	 * Estimate RAM usage of this object.
	 * @return Approximate number of 32 numbers used
	 */
	@Override
	public double calculateMemoryMegaBytes() {
		int matrixRAM = sparseMatrix.count32BitNumbers(); // Approx number of 32-bit numbers in graph
		int mapRAM = (context2focus.size() * 3) + focus2context.length; // Approx number of 32-bit numbers in maps
		double mb = (matrixRAM + mapRAM) / 262144d;
		return (double) Math.round(mb * 100) / 100;
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
		return focus2context[i];
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
	public float max() {
		return this.max;
	}
	
	@Override
	public String getKey(int index) {
		return this.graph.getVertexLabelProperty().getValueAsString(focusIndex2Context(index));
	}
	
	private void setMax(float newMax) {
		this.max = Math.max(max, newMax);
	}
}
