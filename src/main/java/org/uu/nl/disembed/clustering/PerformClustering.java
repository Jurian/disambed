package org.uu.nl.disembed.clustering;

import me.tongfei.progressbar.ProgressBar;
import org.apache.jena.rdf.model.Model;
import org.apache.log4j.Logger;
import org.uu.nl.disembed.clustering.rules.RuleChecker;
import org.uu.nl.disembed.embedding.opt.Embedding;
import org.uu.nl.disembed.util.config.Configuration;
import org.uu.nl.disembed.util.progress.Progress;
import org.uu.nl.disembed.util.read.HnswIndexReader;
import org.uu.nl.disembed.util.write.HnswIndexWriter;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.*;

public record PerformClustering(Configuration config) {

    public static final float EPSILON = 1e-6f;
    private final static Logger logger = Logger.getLogger(PerformClustering.class);

    public ClusteringResult perform(Model model, Embedding embedding) throws IOException {

        try {
            final int n = embedding.getSize();
            final int dim = embedding.getDimension();
            final int k = config.getClustering().getK();
            final int numThreads = config.getThreads();
            final float theta = config.getClustering().getTheta();
            final int maxQuerySize = config.getClustering().getRules().getMaxQuerySize();

            final boolean usingRules = config.getClustering().getRules() != null;
            RuleChecker ruleChecker = usingRules ? new RuleChecker(model, embedding.getKeys(), config.getClustering()) : null;

            int[][] components;

            {
                CandidatePairs cp;
                if(config.getIntermediateInput() != null && config.getIntermediateInput().getHnsw() != null) {
                    logger.info("Loading pre-computed HNSW index...");
                    HnswIndexReader reader = new HnswIndexReader();
                    cp = new CandidatePairs(embedding, reader.load(new File(config.getIntermediateInput().getHnsw().getFilename())), config);
                } else {
                    cp = new CandidatePairs(embedding, config);
                    if(config.getIntermediateOutput().getHnsw() != null) {
                        HnswIndexWriter writer = new HnswIndexWriter(cp.getIndex(), config);
                        writer.write();
                    }
                }

                logger.info("Retrieving " + k + " approximate nearest neighbors");

                int[][] candidatePairs = cp.getNearestNeighborPairs(embedding, k, theta);

                if (usingRules && config.getClustering().getRules().hasDefiniteRules()) {
                    candidatePairs = ruleChecker.pruneCandidatePairs(model, candidatePairs, maxQuerySize);
                }

                logger.info("Finding connected components...");
                components = Util.connectedComponents(n, candidatePairs);
                logger.info("Found " + components.length + " components");

                int largestComponentSize = 0;
                for (int[] component : components) {
                    largestComponentSize = Math.max(component.length, largestComponentSize);
                }

                logger.info("Largest component is size " + largestComponentSize);

                System.gc();
            }

            final int nComponents = components.length;

            if (nComponents == 1) {
                logger.warn("Only 1 connected component found, consider increasing value of theta > " + theta);
            }

            final ExecutorService es = Executors.newWorkStealingPool(numThreads);
            CompletionService<ClusterAlgorithm.ClusterResult> completionService = new ExecutorCompletionService<>(es);

            int ccJobs = 0;
            int vJobs = 0;
            int maxSize = 0;
            int skipped = 0;

            for (int i = 0, j = 0; i < nComponents; i++) {
                int size = components[i].length;

                if(size > config.getClustering().getMaxComponentSize()) {
                    skipped++;
                    continue;
                }

                maxSize = Math.max(size, maxSize);

                if(size <= config.getClustering().getMaxCorrelationClusteringSize()) {
                    ccJobs++;
                    completionService.submit(
                            new CorrelationClustering(
                                    j,
                                    components[i],
                                    ruleChecker,
                                    embedding.getVectors(),
                                    theta,
                                    EPSILON,
                                    config.getThreads())
                    );
                } else {
                    vJobs++;
                    completionService.submit(
                            new VoteClustering(
                                    j,
                                    components[i],
                                    ruleChecker,
                                    embedding.getVectors(),
                                    theta,
                                    EPSILON,
                                    config.getThreads())
                    );
                }
                j++;
            }

            final int totalJobs = ccJobs + vJobs;

            logger.info("Submitted " + ccJobs + " correlation clustering jobs");
            logger.info("Submitted " + vJobs + " vote clustering jobs");
            logger.info("Skipped " + skipped + " components that were too large");
            logger.info("Largest valid component: " + maxSize + " entities");

            // We don't know how many clusters we will find, so we use a ragged array for each valid component (i.e. job)
            int[][][] clusters = new int[totalJobs][][];

            try (ProgressBar pb = Progress.progressBar("Clustering", totalJobs, "components")) {

                // Retrieve the futures after computation (auto wait for it)
                int received = 0;

                while (received < totalJobs) {

                    try {
                        ClusterAlgorithm.ClusterResult clusterResult = completionService.take().get();
                        clusters[clusterResult.index()] = clusterResult.cluster();
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        pb.step();
                        received++;
                    }
                }
            } finally {
                es.shutdown();
            }

            return new ClusteringResult(clusters);
        } finally {
            model.close();
        }
    }

    public record ClusteringResult(int[][][] clusters) { }
}
