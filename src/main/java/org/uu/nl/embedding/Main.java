package org.uu.nl.embedding;

import grph.GrphWebNotifications;
import org.apache.log4j.Logger;
import org.uu.nl.embedding.bca.BookmarkColoring;
import org.uu.nl.embedding.convert.Rdf2GrphConverter;
import org.uu.nl.embedding.opt.*;
import org.uu.nl.embedding.opt.grad.AMSGrad;
import org.uu.nl.embedding.opt.grad.Adagrad;
import org.uu.nl.embedding.opt.grad.Adam;
import org.uu.nl.embedding.util.CoOccurrenceMatrix;
import org.uu.nl.embedding.util.InMemoryRdfGraph;
import org.uu.nl.embedding.util.config.Configuration;
import org.uu.nl.embedding.util.config.InvalidConfigException;
import org.uu.nl.embedding.util.read.ConfigReader;
import org.uu.nl.embedding.util.read.JenaReader;
import org.uu.nl.embedding.util.write.EmbeddingWriter;
import org.uu.nl.embedding.util.write.GloVeWriter;
import org.uu.nl.embedding.util.write.SplitFileWriter;
import org.uu.nl.embedding.util.write.Word2VecWriter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * @author Jurian Baas
 */
public class Main {

    private final static Logger logger = Logger.getLogger("Graph Embeddings");

    private static void runProgram(Configuration config) throws IOException {

        logger.info("Starting the embedding creation process with following settings:");
        logger.info("Graph File: " + config.getGraph());
        logger.info("Embedding dimensions: " + config.getDim());
        logger.info("Threads: " + config.getThreads());
        logger.info("BCA Alpha: " + config.getBca().getAlpha());
        logger.info("BCA Epsilon: " + config.getBca().getEpsilon());
        logger.info("Gradient Descent Algorithm: " + config.getOpt().getMethod());
        logger.info(config.getMethod() + " Tolerance: " + config.getOpt().getTolerance());
        logger.info(config.getMethod() + " Maximum Iterations: " + config.getOpt().getMaxiter());
        switch(config.getPredicates().getTypeEnum()) {
            case NONE:
                logger.info("# Using no predicate weights:");
                for(String s : config.getPredicates().getFilter()) {
                    logger.info("# " + s + ": " + 1.0F);
                }
                break;
            case MANUAL:
                logger.info("# Using manual predicate weights:");
                for(String s : config.getPredicates().getFilter()) {
                    logger.info("# " + s + ": " + config.getPredicates().getWeights().getOrDefault(s, 1.0F));
                }
                break;
            case PAGERANK:
                logger.info("# Pagerank weights used");
                break;
            case FREQUENCY:
                logger.info("# Predicate frequency weights used");
                break;
            case INVERSE_FREQUENCY:
                logger.info("# Inverse predicate frequency weights used");
                break;
        }
        if(config.usingSimilarity()) {
            logger.info("Using the following similarity metrics:");
            config.getSimilarity().forEach(s -> logger.info(s.toString()));
        } else logger.info("No similarity matching will be performed");

        String outFileName = config.getOutput().getName();
        if(outFileName == null || outFileName.isEmpty()) {
            outFileName = createFileName(config);
        }
        logger.info("Writing files with prefix: " + outFileName);

        Configuration.setThreadLocalRandom();

        final JenaReader loader = new JenaReader();

        final Rdf2GrphConverter converter = new Rdf2GrphConverter(config);

        final InMemoryRdfGraph graph = converter.convert(loader.load(config.getGraphFile()));

        final CoOccurrenceMatrix bca = new BookmarkColoring(graph, config);

        final IOptimizer optimizer = createOptimizer(config, bca);

        final Optimum optimum = optimizer.optimize();

        final EmbeddingWriter writer = getWriter(outFileName, config);
        writer.write(optimum, bca, Paths.get("").toAbsolutePath().resolve("out"));
    }

    private static EmbeddingWriter getWriter(String outFileName, Configuration config) {
        switch(config.getOutput().getWriterEnum()) {
            case GLOVE: return new GloVeWriter(outFileName, config);
            case WORD2VEC: return new Word2VecWriter(outFileName, config);
            default:
            case SPLIT: return new SplitFileWriter(outFileName, config);
        }
    }

    private static String createFileName(Configuration config) {
        String outFileName = config.getGraphFile().getName().toLowerCase();
        if(outFileName.contains(".")) {
            outFileName = outFileName.substring(0, outFileName.lastIndexOf("."));
        }
        outFileName += "_" + config.getMethod().toLowerCase();

        if(config.getSimilarity() != null && !config.getSimilarity().isEmpty()) {
            outFileName += "_partial";
        } else {
            outFileName += "_exact";
        }

        outFileName += "_" + config.getBca().getAlpha() + "_" + config.getBca().getEpsilon();
        outFileName += "_" + config.getOpt().getMethod();
         outFileName += "_" + config.getDim();

        return outFileName;
    }

    private static IOptimizer createOptimizer(final Configuration config, final CoOccurrenceMatrix coMatrix) {

        CostFunction cf;
        switch (config.getMethodEnum()) {
            default:
                throw new IllegalArgumentException("Invalid cost function");
            case GLOVE:
                cf = new GloveCost();
                break;
            case PGLOVE:
                cf = new PGloveCost();
                break;
        }

        switch(config.getOpt().getMethodEnum()) {
            default:
                throw new IllegalArgumentException("Invalid optimization method");
            case ADAGRAD:
                return new Adagrad(coMatrix, config, cf);
            case ADAM:
                return new Adam(coMatrix, config, cf);
            case AMSGRAD:
                return new AMSGrad(coMatrix, config, cf);
        }
    }

    public static void main(String[] args) {

        for(int i = 0; i < args.length; i++) {
            if(args[i].equals("-c")) {
                if(i + 1 < args.length) {
                    try {

                        final File configFile = Paths.get("").toAbsolutePath().resolve( args[i + 1]).toFile();

                        if(!configFile.exists() || !configFile.isFile()) {
                            logger.error("Cannot find configuration file + " + configFile.getPath());
                            System.exit(1);
                        } else {
                            GrphWebNotifications.enabled = false;
                            Configuration config = new ConfigReader().load(configFile);
                            Configuration.check(config);
                            runProgram(config);
                        }
                    } catch (IOException | InvalidConfigException e) {
                        logger.error(e.getMessage(), e);
                        System.exit(1);
                    }
                } else {
                    logger.error("No configuration file specified, exiting...");
                    System.exit(1);
                }
            }
        }
    }
}
