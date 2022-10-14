package org.uu.nl.embedding.cluster;

import me.tongfei.progressbar.ProgressBar;
import org.apache.jena.rdf.model.Model;
import org.apache.log4j.Logger;
import org.uu.nl.embedding.cluster.rules.RuleChecker;
import org.uu.nl.embedding.opt.Embedding;
import org.uu.nl.embedding.opt.Optimizer;
import org.uu.nl.embedding.util.Progress;
import org.uu.nl.embedding.util.config.ClusterConfiguration;

import java.util.concurrent.*;

public record PerformClustering(ClusterConfiguration config) {

    public static final float EPSILON = 1e-6f;
    private final static Logger logger = Logger.getLogger(PerformClustering.class);

    public ClusteringResult perform(Model model, Embedding embedding) {

        try {
            final int n = embedding.getSize();
            final int dim = embedding.getDimension();
            final int k = config.getK();
            final int numThreads = config.getThreads();
            final float theta = config.getTheta();
            final int maxQuerySize = config.getRules().getMaxQuerySize();

            RuleChecker ruleChecker = new RuleChecker(config);
            CandidatePairs cp = new CandidatePairs(embedding);

            for (Optimizer.EmbeddedEntity embeddedEntity : embedding) {
                cp.add(embeddedEntity);
            }

            int[][] candidatePairs = cp.getNearestNeighborPairs(embedding, k, theta);

            if (config.getRules().hasDefiniteRules()) {
                candidatePairs = ruleChecker.pruneCandidatePairs(model, candidatePairs, embedding.getKeys(), maxQuerySize);
            }

            final int[][] components = Util.connectedComponents(n, candidatePairs);

            final int nComponents = components.length;

            if (nComponents == 1) {
                logger.warn("Only 1 connected component found, consider increasing value of theta > " + theta);
            }

            final float[][] penalties = ruleChecker.checkComponents(model, components, embedding.getKeys(), maxQuerySize);

            final ExecutorService es = Executors.newWorkStealingPool(numThreads);
            CompletionService<ClusterAlgorithm.ClusterResult> completionService = new ExecutorCompletionService<>(es);

            int ccJobs = 0;
            int vJobs = 0;
            int maxSize = 0;

            for (int i = 0; i < nComponents; i++) {
                int size = components[i].length;
                maxSize = Math.max(size, maxSize);
                if(size <= config.getCorrelationClusteringMaxSize()) {
                    ccJobs++;
                    completionService.submit(new CorrelationClustering(i, components[i], penalties[i], embedding.getVectors(), theta, EPSILON));
                } else {
                    vJobs++;
                    completionService.submit(new VoteClustering(i, components[i], penalties[i], embedding.getVectors(), theta, EPSILON));
                }
            }

            logger.info("Submitted " + ccJobs + " correlation clustering jobs");
            logger.info("Submitted " + vJobs + " vote clustering jobs");
            logger.info("Largest component: " + maxSize + " elements");

            int[][] clusters = new int[nComponents][];

            try (ProgressBar pb = Progress.progressBar("Clustering", nComponents, "components")) {

                //now retrieve the futures after computation (auto wait for it)
                int received = 0;

                while (received < nComponents) {

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

            return new ClusteringResult(components, clusters);
        } finally {
            model.close();
        }

    }

    public static record ClusteringResult(int[][] components, int[][] clusters) {
    }
}
