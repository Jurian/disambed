package org.uu.nl.embedding;

import grph.Grph;
import org.apache.log4j.Logger;
import org.uu.nl.embedding.bca.BookmarkColoring;
import org.uu.nl.embedding.convert.Rdf2GrphConverter;
import org.uu.nl.embedding.glove.GloveModel;
import org.uu.nl.embedding.glove.opt.Optimizer;
import org.uu.nl.embedding.pca.PCA;
import org.uu.nl.embedding.util.InMemoryRdfGraph;
import org.uu.nl.embedding.util.config.Configuration;
import org.uu.nl.embedding.util.read.ConfigReader;
import org.uu.nl.embedding.util.read.JenaReader;
import org.uu.nl.embedding.util.write.GloveTextWriter;
import org.uu.nl.embedding.util.write.GloveWriter;

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
        if(config.getBca().isDirected()) logger.info("BCA Reverse: " + config.getBca().isReverse());
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

        JenaReader loader = new JenaReader();

        Rdf2GrphConverter converter = new Rdf2GrphConverter(config);

        InMemoryRdfGraph graph = converter.convert(loader.load(config.getGraphFile()));

        BookmarkColoring bca = new BookmarkColoring(graph, config);

        GloveModel model = new GloveModel(bca, config);

        Optimizer optimizer = null;

        switch (config.getMethodEnum()) {
            case GLOVE:
                switch(config.getOpt().getMethodEnum()) {
                    case ADAGRAD:
                        optimizer = new org.uu.nl.embedding.glove.opt.impl.AdagradOptimizer(model, config);
                        break;
                    case ADAM:
                        optimizer = new org.uu.nl.embedding.glove.opt.impl.AdamOptimizer(model, config);
                        break;
                    case AMSGRAD:
                        optimizer = new org.uu.nl.embedding.glove.opt.impl.AMSGradOptimizer(model, config);
                        break;
                }
                break;
            case PGLOVE:
                switch(config.getOpt().getMethodEnum()) {
                    case ADAGRAD:
                        optimizer = new org.uu.nl.embedding.glove.opt.prob.AdagradOptimizer(model, config);
                        break;
                    case ADAM:
                        optimizer = new org.uu.nl.embedding.glove.opt.prob.AdamOptimizer(model, config);
                        break;
                    case AMSGRAD:
                        optimizer = new org.uu.nl.embedding.glove.opt.prob.AMSGradOptimizer(model, config);
                        break;
                }
                break;
        }

        model.setOptimum(optimizer.optimize());

        if(config.usingPca()) {
            logger.info("Starting PCA...");
            PCA pca = new PCA(model.getOptimum().getResult(), false, config);
            model.updateOptimum(pca.project(config.getPca().getVariance()));
        }

        String bca_fileName = config.getGraphFile().getName().toLowerCase();
        if(bca_fileName.contains(".")) {
            bca_fileName = bca_fileName.substring(0, bca_fileName.lastIndexOf("."));
        }
        bca_fileName += "_" + config.getMethod().toLowerCase();


        if(config.getSimilarity() != null && !config.getSimilarity().isEmpty()) {
            bca_fileName += "_partial";
        } else {
            bca_fileName += "_exact";
        }

        if(config.getBca().isDirected()){
            bca_fileName += "_directed";
            if(config.getBca().isReverse()) bca_fileName += "_reverse";
        } else {
            bca_fileName += "_undirected";
        }
        bca_fileName += "_" + config.getBca().getAlpha() + "_" + config.getBca().getEpsilon();
        bca_fileName += "_" + config.getOpt().getMethod();
        if(config.usingPca()) bca_fileName += "_pca_" + model.getDimension();
        else bca_fileName += "_" + model.getDimension();
        GloveWriter writer = new GloveTextWriter(bca_fileName, config);
        writer.write(model, Paths.get("").toAbsolutePath().resolve("out"));
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
