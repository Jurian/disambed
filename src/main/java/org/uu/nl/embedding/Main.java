package org.uu.nl.embedding;

import grph.Grph;
import org.apache.commons.cli.*;
import org.apache.log4j.Logger;
import org.uu.nl.embedding.bca.BookmarkColoring;
import org.uu.nl.embedding.bca.util.BCAOptions;
import org.uu.nl.embedding.glove.GloveModel;
import org.uu.nl.embedding.glove.opt.*;
import org.uu.nl.embedding.glove.opt.impl.*;
import org.uu.nl.embedding.convert.Rdf2GrphConverter;
import org.uu.nl.embedding.pca.PCA;
import org.uu.nl.embedding.util.read.JenaReader;
import org.uu.nl.embedding.util.read.WeightsReader;
import org.uu.nl.embedding.util.write.GloveTextWriter;
import org.uu.nl.embedding.util.write.GloveWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * @author Jurian Baas
 */
public class Main {

    private final static Logger logger = Logger.getLogger("Graph Embeddings");

    private static final Option option_config = Option.builder( "c")
            .required(true)
            .desc("Location of configuration file")
            .longOpt("config")
            .hasArg()
            .build();

    private static final Options options = new Options();
    private static final CommandLineParser parser = new DefaultParser();
    private static final HelpFormatter formatter = new HelpFormatter();

    static {
        options.addOption(option_config);
    }

    public static void main(String[] args) {

        try {

            CommandLine cmd = parser.parse(options, args);

            String config = cmd.getOptionValue("c");
            Properties prop = new Properties();

            try(InputStream is = new FileInputStream(config)) {
                prop.load(is);
            } catch (IOException e) {
                throw new FileNotFoundException("Cannot find config file " + config);
            }

            String bca_file_str = prop.getProperty("bca_filename");
            Path currentRelativePath = Paths.get("").toAbsolutePath();
            Path bca_file_path = currentRelativePath.resolve(bca_file_str);
            File bca_file = bca_file_path.toFile();

            if(!bca_file.exists() || !bca_file.isFile()) {
                throw new FileNotFoundException("Cannot find file " + bca_file_str);
            }

            double bca_alpha = Double.parseDouble(prop.getProperty("bca_alpha", "1e-2"));
            double bca_epsilon = Double.parseDouble(prop.getProperty("bca_epsilon", "1e-4"));

            BCAOptions.BCAType bca_alg;
            String bca_alg_str = prop.getProperty("bca_algorithm", "vanilla").toLowerCase();
            try{
                bca_alg = BCAOptions.BCAType.valueOf(bca_alg_str.toUpperCase());
            } catch(IllegalArgumentException e) {
                throw new UnsupportedAlgorithmException("Unsupported optimization algorithm. Use one of: vanilla, semantic");
            }

            boolean bca_reverse = Boolean.parseBoolean(prop.getProperty("bca_reverse", "true"));

            String glove_alg = prop.getProperty("glove_algorithm", "adam").toLowerCase();
            int glove_dim = Integer.parseInt(prop.getProperty("glove_dimensions", "50"));
            double glove_tol = Double.parseDouble(prop.getProperty("glove_tolerance", "1e-5"));
            int glove_max_iter = Integer.parseInt(prop.getProperty("glove_max-iter", "1000"));

            String weight_file = prop.getProperty("weight_file");

            if(weight_file == null) throw new ParseException("Weight file not specified");

            logger.info("Starting the embedding creation process with following settings:");
            logger.info("BCA File: " + bca_file_str);
            logger.info("BCA Alpha: " + bca_alpha);
            logger.info("BCA Epsilon: " + bca_epsilon);
            logger.info("BCA Algorithm: " + bca_alg_str);
            logger.info("BCA Reverse: " + bca_reverse);
            logger.info("GloVe Algorithm: " + glove_alg);
            logger.info("GloVe Dimensions: " + glove_dim);
            logger.info("GloVe Tolerance: " + glove_tol);
            logger.info("GloVe Maximum Iterations: " + glove_max_iter);
            logger.info("Weight File: " + weight_file);


            JenaReader loader = new JenaReader();
            Rdf2GrphConverter converter = new Rdf2GrphConverter(weight_file);
            logger.info("Converting RDF data into fast graph representation, predicates that are not weighted are ignored");
            Grph graph = converter.convert(loader.load(bca_file));

            BCAOptions bcaOptions = new BCAOptions(bca_alg, bca_reverse, bca_alpha, bca_epsilon);

            BookmarkColoring bca = new BookmarkColoring(graph, bcaOptions);

            GloveModel model = new GloveModel(glove_dim, bca);

            final Optimizer optimizer;
            switch(glove_alg) {
                default:
                    throw new UnsupportedAlgorithmException("Unsupported optimization algorithm. Use one of: adagrad, adam, adadelta, amsgrad");
                case "adagrad":
                    optimizer = new AdagradOptimizer(model, glove_max_iter, glove_tol);
                    break;
                case "adam":
                    optimizer = new AdamOptimizer(model, glove_max_iter, glove_tol);
                    break;
                case "adadelta":
                    optimizer = new AdadeltaOptimizer(model, glove_max_iter, glove_tol);
                    break;
                case "amsgrad":
                    optimizer = new AMSGradOptimizer(model, glove_max_iter, glove_tol);
                    break;
            }
            model.setOptimum(optimizer.optimize());

            logger.info("GloVe converged with final average cost " + model.getOptimum().getFinalCost());
            logger.info("Starting PCA...");
            PCA pca = new PCA(model.getOptimum().getResult(), model.getDimension(), false);
            model.updateOptimum(pca.project(0.95));

            String bca_fileName = bca_file.getName().toLowerCase();
            if(bca_fileName.contains(".")) {
                int idx = bca_fileName.lastIndexOf(".");
                bca_fileName = bca_fileName.substring(0, idx);
            }
            if(bca_reverse){
                bca_fileName += ".reverse";
            }
            GloveWriter writer = new GloveTextWriter(bca_fileName+"."+bca_alg.name().toLowerCase()+"."+glove_alg.toLowerCase()+"."+glove_dim);
            writer.write(model, currentRelativePath.resolve("out"));

        } catch (ParseException | NumberFormatException | UnsupportedAlgorithmException | IOException  e) {
            logger.error(e.getMessage(), e);
            formatter.printHelp("Graph Embeddings", options);
            System.exit(1);
        }
    }

    static class FileNotFoundException extends RuntimeException {
        FileNotFoundException(String message) {
            super(message);
        }
    }

    static class UnsupportedAlgorithmException extends RuntimeException {
        UnsupportedAlgorithmException(String message) {
            super(message);
        }
    }
}
