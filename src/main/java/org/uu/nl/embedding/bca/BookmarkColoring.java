package org.uu.nl.embedding.bca;

import me.tongfei.progressbar.ProgressBar;
import org.uu.nl.embedding.bca.jobs.DirectedWeighted;
import org.uu.nl.embedding.bca.jobs.HybridWeighted;
import org.uu.nl.embedding.bca.jobs.UndirectedWeighted;
import org.uu.nl.embedding.bca.util.BCV;
import org.uu.nl.embedding.convert.util.InEdgeNeighborhoodAlgorithm;
import org.uu.nl.embedding.convert.util.NodeInfo;
import org.uu.nl.embedding.convert.util.OutEdgeNeighborhoodAlgorithm;
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
	private Permutation permutation;
	private final InMemoryRdfGraph graph;
	private final Map<Integer, Integer> focusIndexMap;

	public BookmarkColoring(final InMemoryRdfGraph graph, final Configuration config) {

		final double alpha = config.getBca().getAlpha();
		final double epsilon = config.getBca().getEpsilon();
		final int[] verts = graph.getVertices().toIntArray();
		final boolean[] performBCA = new boolean[verts.length];

		final Configuration.Output output = config.getOutput();

		this.focusIndexMap = new HashMap<>();
		int notSkipped = 0;

		for(int i = 0; i < verts.length; i++) {

			final int vert = verts[i];
			final byte type = (byte) graph.getVertexTypeProperty().getValueAsInt(vert);
			final String key = graph.getVertexLabelProperty().getValueAsString(vert);
			final NodeInfo nodeInfo = NodeInfo.fromByte(type);

			switch (nodeInfo) {
				case URI:
					if(output.outputUriNodes() && !output.getUri().isEmpty() && output.getUri().stream().anyMatch(key::startsWith)) {
						performBCA[i] = true;
						notSkipped++;
					}
					break;
				case BLANK:
					if(output.outputBlankNodes() && !output.getBlank().isEmpty() && output.getBlank().stream().anyMatch(key::startsWith)) {
						performBCA[i] = true;
						notSkipped++;
					}
					break;
				case LITERAL:
					if(output.outputLiteralNodes() && !output.getLiteral().isEmpty() && output.getLiteral().stream().anyMatch(key::startsWith)) {
						performBCA[i] = true;
						notSkipped++;
					}
					break;
			}
		}

		this.graph = graph;
		this.focusVectors = notSkipped;
		this.contextVectors = verts.length;
		this.coOccurrenceIdx_I = new ArrayList<>(notSkipped);
		this.coOccurrenceIdx_J = new ArrayList<>(notSkipped);
		this.coOccurrenceValues = new ArrayList<>(notSkipped);

		final int numThreads = config.getThreads();

		final ExecutorService es = Executors.newWorkStealingPool(numThreads);

		final int[][] inVertex = graph.getInNeighborhoods();
		final int[][] outVertex = graph.getOutNeighborhoods();
		final int[][] inEdge = new InEdgeNeighborhoodAlgorithm(config).compute(graph);
		final int[][] outEdge = new OutEdgeNeighborhoodAlgorithm(config).compute(graph);

		CompletionService<BCV> completionService = new ExecutorCompletionService<>(es);

		for(int i = 0, j = 0; i < verts.length; i++) {

			if(!performBCA[i]) continue;

			final int bookmark = verts[i];
			focusIndexMap.put(bookmark, j);
			j++;

			// Choose a graph neighborhood algorithm
			switch (config.getBca().getTypeEnum()){

				case DIRECTED:
					completionService.submit(new DirectedWeighted(
							graph, bookmark,
							alpha, epsilon,
							inVertex, outVertex, inEdge, outEdge));
					break;
				case UNDIRECTED:
					completionService.submit(new UndirectedWeighted(
							graph, bookmark,
							alpha, epsilon,
							inVertex, outVertex, inEdge, outEdge));
					break;
				case HYBRID:
					completionService.submit(new HybridWeighted(
							graph, bookmark,
							alpha, epsilon,
							inVertex, outVertex, inEdge, outEdge));
					break;
			}
		}

		try(ProgressBar pb = Configuration.progressBar("BCA", notSkipped, "nodes")) {

			//now retrieve the futures after computation (auto wait for it)
			int received = 0;

			while(received < notSkipped) {

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
		return this.focusIndexMap.get(coOccurrenceIdx_I.get(permutation.randomAccess(i)));
	}
	
	public int cIdx_J(int j) {
		return this.coOccurrenceIdx_J.get(permutation.randomAccess(j));
	}
	
	public float cIdx_C(int i) {
		return this.coOccurrenceValues.get(permutation.randomAccess(i));
	}
	
	public byte getType(int index) {
		return (byte) this.graph.getVertexTypeProperty().getValueAsInt(this.focusIndexMap.get(index));
	}
	
	public int coOccurrenceCount() {
		return this.coOccurrenceCount;
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
		return this.graph.getVertexLabelProperty().getValueAsString(this.focusIndexMap.get(index));
	}
	
	private void setMax(double newMax) {
		this.max = Math.max(max, newMax);
	}
}
