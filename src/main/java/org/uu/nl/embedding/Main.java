package org.uu.nl.embedding;

import org.apache.log4j.Logger;
import org.uu.nl.embedding.bca.BookmarkColoring;
import org.uu.nl.embedding.convert.Rdf2GrphConverter;
import org.uu.nl.embedding.opt.*;
import org.uu.nl.embedding.opt.grad.AMSGrad;
import org.uu.nl.embedding.opt.grad.Adagrad;
import org.uu.nl.embedding.opt.grad.Adam;
import org.uu.nl.embedding.pca.PCA;
import org.uu.nl.embedding.util.InMemoryRdfGraph;
import org.uu.nl.embedding.util.config.Configuration;
import org.uu.nl.embedding.util.read.ConfigReader;
import org.uu.nl.embedding.util.read.JenaReader;
import org.uu.nl.embedding.util.write.EmbeddingTextWriter;
import org.uu.nl.embedding.util.write.EmbeddingWriter;

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
        logger.info("BCA Directed: " + config.getBca().isDirected());
        logger.info("BCA normalize: " + config.getBca().getNormalize());
        logger.info("Gradient Descent Algorithm: " + config.getOpt().getMethod());
        logger.info(config.getMethod() + " Tolerance: " + config.getOpt().getTolerance());
        logger.info(config.getMethod() + " Maximum Iterations: " + config.getOpt().getMaxiter());

        if(config.usingPca()) logger.info("PCA Minimum Variance: " + config.getPca().getVariance());
        else logger.info("No PCA will be performed");

        if(config.usingWeights()) {
            logger.info("Using weights, predicates that are not listed are ignored:");
            config.getWeights().forEach((k, v) -> logger.info( k + ": " + v));
        } else logger.info("No weights specified, using linear weight");

        if(config.usingSimilarity()) {
            logger.info("Using the following similarity metrics:");
            config.getSimilarity().forEach(s -> logger.info(s.toString()));
        } else logger.info("No similarity matching will be performed");

        Configuration.setThreadLocalRandom();

        final JenaReader loader = new JenaReader();

        final Rdf2GrphConverter converter = new Rdf2GrphConverter(config);

        final InMemoryRdfGraph graph = converter.convert(loader.load(config.getGraphFile()));

        final BookmarkColoring bca = new BookmarkColoring(graph, config);

        final OptimizerModel model = new OptimizerModel(bca, config);

        final IOptimizer optimizer = createOptimizer(config, model);

        assert optimizer != null;
        model.setOptimum(optimizer.optimize());

        if(config.usingPca()) {
            logger.info("Starting PCA...");
            PCA pca = new PCA(model.getOptimum().getResult(), false, config);
            model.updateOptimum(pca.project(config.getPca().getVariance()));
        }

        String outFileName = config.getOutput().getName();
        if(outFileName == null || outFileName.isEmpty()) {
            outFileName = createFileName(config, model);
        }
        logger.info("Writing files with prefix: " + outFileName);
        final EmbeddingWriter writer = new EmbeddingTextWriter(outFileName, config);
        writer.write(model, Paths.get("").toAbsolutePath().resolve("out"));
    }

    private static String createFileName(Configuration config, OptimizerModel model) {
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

        if(config.getBca().isDirected()){
            outFileName += "_directed";
        } else {
            outFileName += "_undirected";
        }

        outFileName += "_" + config.getBca().getAlpha() + "_" + config.getBca().getEpsilon();
        outFileName += "_" + config.getOpt().getMethod();
        if(config.usingPca()) outFileName += "_pca_" + model.getDimension();
        else outFileName += "_" + model.getDimension();

        return outFileName;
    }

    private static IOptimizer createOptimizer(final Configuration config, final OptimizerModel model) {

        CostFunction cf;
        switch (config.getMethodEnum()) {
            default:
            case GLOVE:
                cf = new GloveCost();
                break;
            case PGLOVE:
                cf = new PGloveCost();
                break;
        }

        switch(config.getOpt().getMethodEnum()) {
            default:
            case ADAGRAD:
                return new Adagrad(model, config, cf);
            case ADAM:
                return new Adam(model, config, cf);
            case AMSGRAD:
                return new AMSGrad(model, config, cf);
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
                            Configuration config = new ConfigReader().load(configFile);
                            Configuration.check(config);
                            runProgram(config);
                        }
                    } catch (IOException | Configuration.InvalidConfigurationException e) {
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
