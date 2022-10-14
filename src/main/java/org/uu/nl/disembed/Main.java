package org.uu.nl.embedding;

import grph.GrphWebNotifications;
import org.apache.jena.rdf.model.Model;
import org.apache.log4j.Logger;
import org.uu.nl.embedding.bca.BookmarkColoring;
import org.uu.nl.embedding.cluster.PerformClustering;
import org.uu.nl.embedding.convert.Rdf2GrphConverter;
import org.uu.nl.embedding.opt.*;
import org.uu.nl.embedding.opt.grad.AMSGrad;
import org.uu.nl.embedding.opt.grad.Adagrad;
import org.uu.nl.embedding.opt.grad.Adam;
import org.uu.nl.embedding.util.CoOccurrenceMatrix;
import org.uu.nl.embedding.util.InMemoryRdfGraph;
import org.uu.nl.embedding.util.config.*;
import org.uu.nl.embedding.util.read.ConfigurationReader;
import org.uu.nl.embedding.util.read.EmbeddingReader;
import org.uu.nl.embedding.util.read.JenaReader;
import org.uu.nl.embedding.util.write.ClusterWriter;
import org.uu.nl.embedding.util.write.EmbeddingWriter;
import org.uu.nl.embedding.util.write.GloVeWriter;
import org.uu.nl.embedding.util.write.Word2VecWriter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * @author Jurian Baas
 */
public class Main {

    public final static Logger logger = Logger.getLogger("Graph Embeddings");

    private static void runProgram(Configuration config) throws IOException {

        EmbeddingConfiguration embeddingConfig = config.getEmbedding();
        ClusterConfiguration  clusterConfig = config.getClustering();
        OutputConfiguration outputConfig = config.getOutput();

        Embedding embedding = null;

        if(embeddingConfig == null) {

            logger.info("No embedding configuration file specified, skipping to clustering...");

        } else {

            logger.info("Starting the embedding creation process with following settings:");
            logger.info("Graph File: " + embeddingConfig.getGraph());
            logger.info("Embedding dimensions: " + embeddingConfig.getDim());
            logger.info("Threads: " + embeddingConfig.getThreads());
            logger.info("BCA Alpha: " + embeddingConfig.getBca().getAlpha());
            logger.info("BCA Epsilon: " + embeddingConfig.getBca().getEpsilon());
            logger.info("Gradient Descent Algorithm: " + embeddingConfig.getOpt().getMethod());
            logger.info(embeddingConfig.getMethod() + " Tolerance: " + embeddingConfig.getOpt().getTolerance());
            logger.info(embeddingConfig.getMethod() + " Maximum Iterations: " + embeddingConfig.getOpt().getMaxiter());
            switch (embeddingConfig.getPredicates().getTypeEnum()) {
                case NONE -> {
                    logger.info("# Using no predicate weights:");
                    for (String s : embeddingConfig.getPredicates().getFilter()) {
                        logger.info("# " + s + ": " + 1.0F);
                    }
                }
                case MANUAL -> {
                    logger.info("# Using manual predicate weights:");
                    for (String s : embeddingConfig.getPredicates().getFilter()) {
                        logger.info("# " + s + ": " + embeddingConfig.getPredicates().getWeights().getOrDefault(s, 1.0F));
                    }
                }
                case PAGERANK -> logger.info("# Pagerank weights used");
                case FREQUENCY -> logger.info("# Predicate frequency weights used");
                case INVERSE_FREQUENCY -> logger.info("# Inverse predicate frequency weights used");
            }
            if(embeddingConfig.usingSimilarity()) {
                logger.info("Using the following similarity metrics:");
                embeddingConfig.getSimilarity().forEach(s -> logger.info(s.toString()));
            } else logger.info("No similarity matching will be performed");


            EmbeddingConfiguration.setThreadLocalRandom();

            {
                InMemoryRdfGraph graph;
                {
                    JenaReader reader = new JenaReader();
                    Rdf2GrphConverter converter = new Rdf2GrphConverter(embeddingConfig);
                    graph = converter.convert(reader.load(embeddingConfig.getGraphFile()));
                }

                System.gc();

                logger.info("Loaded in graph, approximate RAM usage: " + graph.calculateMemoryMegaBytes() + " MB");
                CoOccurrenceMatrix bca = new BookmarkColoring(graph, embeddingConfig);
                logger.info("Loaded in BCA sparse matrix, approximate RAM usage: " + bca.calculateMemoryMegaBytes() + " MB");
                embedding = createOptimizer(embeddingConfig, bca).optimize();

                System.gc();
            }

            if(outputConfig != null && outputConfig.getEmbedding() != null) {

                if(embedding == null) {
                    logger.error("No embedding to write, exiting...");
                } else {
                    getWriter(embedding, config).write();
                }
            }
        }


        if(clusterConfig == null) {
            logger.info("No clustering configuration found, skipping...");
        } else {

            if(embeddingConfig == null) {
                // check cluster config for location of embedding to load
                EmbeddingReader reader = new EmbeddingReader();
                logger.info("Loading embedding from file...");
                embedding = reader.load(clusterConfig.getEmbeddingFile());
            }

            if(embedding == null) {
                logger.error("No embedding to perform clustering on, exiting...");
                System.exit(1);
            }

            PerformClustering.ClusteringResult result;
            {
                Model model = null;

                if(clusterConfig.getRules().hasEndPoint()) {
                    throw new UnsupportedOperationException("Querying from endpoint not supported at the moment");
                } else if(clusterConfig.getRules().hasGraph()) {
                    JenaReader reader = new JenaReader();
                    model = reader.load(clusterConfig.getRules().getGraphFile());
                }

                PerformClustering clustering = new PerformClustering(clusterConfig);
                result = clustering.perform(model, embedding);
            }

            System.gc();

            if(outputConfig != null) {
                if(outputConfig.getClusters() != null) {
                    new ClusterWriter(config, embedding.getKeys(), result.components(), result.clusters()).write();
                }

                if(outputConfig.getLinkset() != null) {
                    throw new UnsupportedOperationException("Writing a linkset not supported at the moment");
                }
            }
        }
    }

    private static EmbeddingWriter getWriter(Embedding embedding, Configuration config) {
        return switch (config.getOutput().getEmbedding().getWriterEnum()) {
            case GLOVE -> new GloVeWriter(embedding, config);
            case WORD2VEC -> new Word2VecWriter(embedding, config);
        };
    }

    private static IOptimizer createOptimizer(final EmbeddingConfiguration config, final CoOccurrenceMatrix coMatrix) {

        CostFunction cf = switch (config.getMethodEnum()) {
            case GLOVE -> new GloveCost();
            case PGLOVE -> new PGloveCost();
        };

        return switch (config.getOpt().getMethodEnum()) {
            case ADAGRAD -> new Adagrad(coMatrix, config, cf);
            case ADAM -> new Adam(coMatrix, config, cf);
            case AMSGRAD -> new AMSGrad(coMatrix, config, cf);
        };
    }

    public static void main(String[] args) {

        Configuration configuration = null;

        for(int i = 0; i < args.length; i++) {
            if(args[i].equals("-c")) {
                if(i + 1 < args.length) {
                    try {

                        final File configFile = Paths.get("").toAbsolutePath().resolve(args[i + 1]).toFile();

                        if(!configFile.exists() || !configFile.isFile()) {
                            logger.error("Cannot find configuration file + " + configFile.getPath());
                            System.exit(1);
                        } else {
                            System.setProperty("shut.up.ojAlgo", "true");
                            GrphWebNotifications.enabled = false;
                            configuration = new ConfigurationReader().load(configFile);
                            configuration.check();
                        }
                    } catch (IOException | InvalidConfigException e) {
                        logger.error(e.getMessage(), e);
                        System.exit(1);
                    }
                } else {
                    logger.error("No embedding configuration file specified, exiting...");
                    System.exit(1);
                }
            }

        }

        if(configuration == null) {
            logger.error("No configuration file specified, exiting...");
            System.exit(1);
        }

        try {
            runProgram(configuration);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
