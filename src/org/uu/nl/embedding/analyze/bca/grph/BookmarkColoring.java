package org.uu.nl.embedding.analyze.bca.grph;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.uu.nl.embedding.analyze.CooccurenceMatrix;
import org.uu.nl.embedding.analyze.bca.grph.util.GraphStatistics;
import org.uu.nl.embedding.analyze.bca.util.BCV;
import org.uu.nl.embedding.analyze.bca.util.Options;
import org.uu.nl.embedding.analyze.bca.util.OrderedIntegerPair;
import org.uu.nl.embedding.analyze.progress.Progress;
import org.uu.nl.embedding.analyze.progress.ProgressType;
import org.uu.nl.embedding.analyze.progress.Publisher;

import grph.Grph;

public class BookmarkColoring implements CooccurenceMatrix {

	private final String[] dict;
	private final GraphStatistics stats;
	private final byte[] types;
	private final int[] cooccurrenceIdx_I;
	private final int[] cooccurrenceIdx_J;
	private final double[] cooccurence;
	private final double alpha, epsilon;
	private double max;
	private final int vocabSize;
	private final int cooccurenceCount;
	private final boolean normalize, includeReverse;

	public BookmarkColoring(Grph graph, Options options, Publisher publisher) {

		this.normalize = options.isNormalize();
		this.includeReverse = options.isReverse();
		this.alpha = options.getAlpha();
		this.epsilon = options.getEpsilon();
		
		this.stats = new GraphStatistics(graph);
		this.types = stats.types;
		this.dict = stats.dict;
		this.vocabSize = stats.keys.length;
		
		final Map<OrderedIntegerPair, Double> cooccurrence_map = new ConcurrentHashMap<>(vocabSize);
		final ExecutorService es = Executors.newFixedThreadPool(options.getnThreads());
		final Map<Integer, BCV> computedBCV = new ConcurrentHashMap<>();
		
		//final int[][] in = graph.getInNeighborhoods();
		final int[][] out = graph.getOutNeighborhoods();
		
		try {
			CompletionService<BCV> completionService = new ExecutorCompletionService<>(es);
			System.out.println("Submitting " + stats.jobs.length + " jobs");
			for(int bookmark : stats.jobs) {
				completionService.submit(new SimpleBCAJob(computedBCV, bookmark, includeReverse, normalize, alpha, epsilon, graph, out));
			}
			
			//now retrieve the futures after computation (auto wait for it)
			int received = 0;

			Progress progress = new Progress(ProgressType.BCA);
			
			while(received < stats.jobs.length) {
		
				try {
					BCV bcv = completionService.take().get();
					// We have to collect all the BCV's first before we can store them
					// in a more efficient lookup friendly way below
					System.out.println("Adding BCV for "+stats.dict[bcv.rootNode]+" of size " + bcv.size());
					bcv.addTo(cooccurrence_map);
					// It is possible to use this maximum value in GloVe, although in the
					// literature they set this value to 100 and leave it at that
					setMax(bcv.max());
					
					received ++;
					double p = (received / (double)stats.jobs.length * 100);
					if(p - progress.getValue() > 0.5) {
						progress.setValue(p);
						publisher.updateProgress(progress);	
					}

				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			}
			progress.setValue(100);
			progress.setFinished(true);
			publisher.updateProgress(progress);
			
		} finally {
			es.shutdown();
		}
		
		this.cooccurenceCount = cooccurrence_map.size();
		this.cooccurence = new double[cooccurenceCount];
		this.cooccurrenceIdx_I = new int[cooccurenceCount];
		this.cooccurrenceIdx_J = new int[cooccurenceCount];
		
		int i = 0;
		for(Entry<OrderedIntegerPair, Double> entry : cooccurrence_map.entrySet()) {
			this.cooccurrenceIdx_I[i] = entry.getKey().getIndex1();
			this.cooccurrenceIdx_J[i] = entry.getKey().getIndex2();
			this.cooccurence[i] = entry.getValue();
			i++;
		}
	}
	
	public int cIdx_I(int i) {
		return this.cooccurrenceIdx_I[i];
	}
	
	public int cIdx_J(int j) {
		return this.cooccurrenceIdx_J[j];
	}
	
	public double cIdx_C(int i) {
		return this.cooccurence[i];
	}
	
	public byte getType(int index) {
		return this.types[index];
	}
	
	public int cooccurrenceCount() {
		return this.cooccurenceCount;
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
	
	@Override
	public String[] getKeys() {
		return this.dict;
	}
	
	@Override
	public byte[] getTypes() {
		return this.types;
	}
	
	private void setMax(double newMax) {
		if(newMax > max) max = newMax;
	}
	
	@Override
	public int uriNodeCount() {
		return this.stats.getUriNodeCount();
	}

	@Override
	public int predicateNodeCount() {
		return this.stats.getPredicateNodeCount();
	}

	@Override
	public int blankNodeCount() {
		return this.stats.getBlankNodeCount();
	}

	@Override
	public int literalNodeCount() {
		return this.stats.getLiteralNodeCount();
	}

	public boolean isNormalize() {
		return normalize;
	}

	public boolean isIncludeReverse() {
		return includeReverse;
	}

}
