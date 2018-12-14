package org.uu.nl.analyze.bca.jena;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.uu.nl.analyze.CooccurenceMatrix;
import org.uu.nl.analyze.bca.util.BCV;
import org.uu.nl.analyze.bca.util.Options;
import org.uu.nl.analyze.bca.util.OrderedIntegerPair;
import org.uu.nl.analyze.bca.util.jena.GraphStatistics;
import org.uu.nl.analyze.bca.util.jena.NodeType;
import org.uu.nl.analyze.progress.Progress;
import org.uu.nl.analyze.progress.ProgressType;
import org.uu.nl.analyze.progress.Publisher;


public class AdvancedBookmarkColoring implements CooccurenceMatrix {

	private final String[] dict;
	private final GraphStatistics stats;
	private final NodeType[] types;
	private final int[] cooccurrenceIdx_I;
	private final int[] cooccurrenceIdx_J;
	private final double[] cooccurence;
	private final double alpha, epsilon;
	private double max;
	private final int vocabSize;
	private final int cooccurenceCount;
	private final boolean normalize, includeReverse;

	public AdvancedBookmarkColoring(Model model, Options options, Publisher publisher) {
		
		if(!options.isLiterals()) removeLiterals(model);

		this.normalize = options.isNormalize();
		this.includeReverse = options.isReverse();
		this.alpha = options.getAlpha();
		this.epsilon = options.getEpsilon();
		
		this.stats = new GraphStatistics(model);
		this.types = stats.types;
		this.dict = stats.dict;
		this.vocabSize = stats.keys.size();
		
		final Map<OrderedIntegerPair, Double> cooccurrence_map = new ConcurrentHashMap<>(vocabSize);
		final ExecutorService es = Executors.newFixedThreadPool(options.getnThreads());
		final Map<Integer, BCV> computedBCV = new ConcurrentHashMap<>();
		 
		try {
			CompletionService<BCV> completionService = new ExecutorCompletionService<>(es);
			System.out.println("Submitting " + stats.jobs.length + " jobs");
			for(Node bookmark : stats.jobs) {
				completionService.submit(new AdvancedBCAJob(computedBCV, bookmark, includeReverse, normalize, alpha, epsilon, stats.keys, model));
			}
			
			//now retrieve the futures after computation (auto wait for it)
			int received = 0;

			Progress progress = new Progress(ProgressType.BCA);
			
			while(received < stats.jobs.length) {
		
				try {
					BCV bcv = completionService.take().get();
					System.out.println("Adding BCV for "+stats.dict[bcv.rootNode]+" of size " + bcv.size());
					computedBCV.put(bcv.rootNode, bcv);

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
		
		for(BCV bcv : computedBCV.values()) {
			if(normalize) bcv.normalize();
			setMax(bcv.max());
			bcv.addTo(cooccurrence_map);
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
	
	private void removeLiterals(Model model) {
		final ExtendedIterator<Triple> triples = model.getGraph().find();
		model.enterCriticalSection(false);
		try {
			while(triples.hasNext()) {
				final Triple t = triples.next();
				if(t.getObject().isLiteral()) triples.remove();
			}
		} finally {
			triples.close();
			model.leaveCriticalSection();
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
	
	public NodeType getType(int index) {
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
	public NodeType[] getTypes() {
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
