package org.uu.nl.disembed;

import grph.GrphWebNotifications;
import org.apache.jena.rdf.model.Model;
import org.apache.log4j.Logger;
import org.uu.nl.disembed.clustering.PerformClustering;
import org.uu.nl.disembed.embedding.bca.BookmarkColoring;
import org.uu.nl.disembed.embedding.bca.CoOccurrenceMatrix;
import org.uu.nl.disembed.embedding.convert.InMemoryRdfGraph;
import org.uu.nl.disembed.embedding.convert.Rdf2GrphConverter;
import org.uu.nl.disembed.embedding.opt.*;
import org.uu.nl.disembed.embedding.opt.grad.AMSGrad;
import org.uu.nl.disembed.embedding.opt.grad.Adagrad;
import org.uu.nl.disembed.embedding.opt.grad.Adam;
import org.uu.nl.disembed.util.config.*;
import org.uu.nl.disembed.util.read.BCAReader;
import org.uu.nl.disembed.util.read.ConfigurationReader;
import org.uu.nl.disembed.util.read.EmbeddingReader;
import org.uu.nl.disembed.util.read.JenaReader;
import org.uu.nl.disembed.util.write.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * @author Jurian Baas
 */
public class Main {

    public final static Logger logger = Logger.getLogger("Graph Embeddings");

    private static void runProgram(Configuration config) throws IOException {

        IntermediateInputConfiguration inputConfig = config.getIntermediateInput();
        EmbeddingConfiguration embeddingConfig = config.getEmbedding();
        ClusterConfiguration  clusterConfig = config.getClustering();
        OutputConfiguration outputConfig = config.getOutput();
        IntermediateOutputConfiguration intermediateOutputConfig = config.getIntermediateOutput();

        if((outputConfig == null || outputConfig.isEmpty()) && (intermediateOutputConfig == null || intermediateOutputConfig.isEmpty())) {
            logger.error("No (valid) output configuration found, exiting...");
            System.exit(1);
        }

        logger.info(config.toString());

        boolean precomputedBCA = inputConfig != null && inputConfig.getBca().getFilename() != null;
        boolean precomputedEmbedding = inputConfig != null && inputConfig.getEmbedding().getFilename() != null;
        //boolean precomputedHNSW = inputConfig != null && inputConfig.getHnsw().getFilename() != null;

        Embedding embedding;

        {
            Configuration.setThreadLocalRandom();
            {



                if(precomputedEmbedding) {

                    // check cluster config for location of embedding to load
                    EmbeddingReader reader = new EmbeddingReader();
                    logger.info("Loading embedding from file...");
                    embedding = reader.load(inputConfig.getEmbedding().getImportFile());

                } else {

                    logger.info("Creating new embedding...");

                    BookmarkColoring bca;

                    if(precomputedBCA) {
                        logger.info("Loading in pre-computed co-occurrence matrix...");
                        bca = new BookmarkColoring(new BCAReader().load(inputConfig.getBca().getImportFile()), config);
                    } else {

                        InMemoryRdfGraph graph;
                        {
                            JenaReader reader = new JenaReader();
                            Rdf2GrphConverter converter = new Rdf2GrphConverter(config);
                            graph = converter.convert(reader.load(embeddingConfig.getGraphFile()));
                        }

                        System.gc();

                        logger.info("Loaded in graph, approximate RAM usage: " + graph.calculateMemoryMegaBytes() + " MB");
                        bca = new BookmarkColoring(graph, config);

                        if(config.getIntermediateOutput().getBca() != null) {
                            new BCAWriter(config, bca).write();
                        }
                    }
                    logger.info("Loaded in BCA sparse matrix, approximate RAM usage: " + bca.calculateMemoryMegaBytes() + " MB");


                    embedding = createOptimizer(config, bca).optimize();

                    if(intermediateOutputConfig.getEmbedding() != null) {
                        getWriter(embedding, config).write();
                    }
                }
                // Hopefully GC is smart enough to remove BCA data here, as it is no longer needed
                // This should happen as the bca object goes out of scope!
                System.gc();
            }
        }

        // Embedding phase is over

        if(clusterConfig == null) {
            // In case the user only wanted to do the embedding phase
            logger.info("No clustering configuration found, exiting...");
            System.exit(0);
        }

        // Start clustering phase

        {

            if(outputConfig == null) {
                logger.info("No output configuration found, exiting...");
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

                PerformClustering clustering = new PerformClustering(config);
                result = clustering.perform(model, embedding);
            }

            System.gc();

            if(outputConfig.getClusters() != null) {
                new ClusterWriter(config, embedding.getKeys(), result.clusters()).write();
            }

            if(outputConfig.getLinkset() != null) {
                new LinksetWriter(config, embedding.getKeys(), result.clusters()).write();
            }
        }
    }

    private static EmbeddingWriter getWriter(Embedding embedding, Configuration config) {
        return switch (config.getIntermediateOutput().getEmbedding().getWriterEnum()) {
            case GLOVE -> new GloVeWriter(embedding, config);
            case WORD2VEC -> new Word2VecWriter(embedding, config);
        };
    }

    private static IOptimizer createOptimizer(final Configuration config, final CoOccurrenceMatrix coMatrix) {

        CostFunction cf = switch (config.getEmbedding().getMethodEnum()) {
            case GLOVE -> new GloveCost();
            case PGLOVE -> new PGloveCost();
        };

        return switch (config.getEmbedding().getOpt().getMethodEnum()) {
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
