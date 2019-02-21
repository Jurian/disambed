package org.uu.nl.embedding;

import grph.Grph;
import org.apache.commons.cli.*;
import org.uu.nl.embedding.analyze.bca.grph.BookmarkColoring;
import org.uu.nl.embedding.analyze.bca.util.BCAOptions;
import org.uu.nl.embedding.analyze.glove.GloveModel;
import org.uu.nl.embedding.analyze.glove.opt.*;
import org.uu.nl.embedding.convert.Rdf2GrphConverter;
import org.uu.nl.embedding.progress.CommandLineProgress;
import org.uu.nl.embedding.util.load.JenaLoader;
import org.uu.nl.embedding.util.save.GloveTextWriter;
import org.uu.nl.embedding.util.save.GloveWriter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

    // BCA BCAOptions
    private static Option option_bca_file = Option.builder("bca_f")
            .required(true)
            .desc("The rdf file")
            .longOpt("bca_file")
            .hasArg()
            .build();
    private static Option option_bca_alpha = Option.builder("bca_a")
            .required(true)
            .desc("BCA alpha")
            .longOpt("bca_alpha")
            .hasArg()
            .build();
    private static Option option_bca_epsilon = Option.builder("bca_e")
            .required(true)
            .desc("BCA epsilon")
            .longOpt("bca_epsilon")
            .hasArg()
            .build();
    private static Option option_bca_threads = Option.builder("bca_t")
            .required(true)
            .desc("Number of threads to use with BCA")
            .longOpt("bca_threads")
            .hasArg()
            .build();

    // GloVe BCAOptions
    private static Option option_glove_alg = Option.builder( "glv_a")
            .required(true)
            .desc("Gradient descent algorithm")
            .longOpt("glv_algorithm")
            .hasArg()
            .build();

    private static Option option_glove_dim = Option.builder( "glv_d")
            .required(true)
            .desc("The number of dimensions of the final embedding.")
            .longOpt("glv_dimensions")
            .hasArg()
            .build();

    private static Option option_glove_tol = Option.builder( "glv_tol")
            .required(true)
            .desc("Minimum change between iterations in GloVe. When reached convergence is assumed.")
            .longOpt("glv_tolerance")
            .hasArg()
            .build();

    private static Option option_glove_maxiter = Option.builder( "glv_m")
            .required(true)
            .desc("Maximum number of iterations to use in GloVe.")
            .longOpt("glv_max_iter")
            .hasArg()
            .build();

    private static Option option_glove_threads = Option.builder( "glv_t")
            .required(true)
            .desc("Number of threads to use with GloVe")
            .longOpt("glv_threads")
            .hasArg()
            .build();


    private static Options options = new Options();
    private static CommandLineParser parser = new DefaultParser();
    private static HelpFormatter formatter = new HelpFormatter();

    static {
        options.addOption(option_bca_file);
        options.addOption(option_bca_alpha);
        options.addOption(option_bca_epsilon);
        options.addOption(option_bca_threads);
        options.addOption(option_glove_alg);
        options.addOption(option_glove_dim);
        options.addOption(option_glove_tol);
        options.addOption(option_glove_maxiter);
        options.addOption(option_glove_threads);
    }

    public static void main(String[] args) {

        try {

            CommandLine cmd = parser.parse(options, args);

            String bca_file_str = cmd.getOptionValue("bca_f");
            double bca_alpha = Double.parseDouble(cmd.getOptionValue("bca_a"));
            double bca_epsilon = Double.parseDouble(cmd.getOptionValue("bca_e"));
            int bca_threads = Integer.parseInt(cmd.getOptionValue("bca_t"));

            String glove_alg = cmd.getOptionValue("glv_a").toLowerCase();
            int glove_dim = Integer.parseInt(cmd.getOptionValue("glv_d"));
            double glove_tol = Double.parseDouble(cmd.getOptionValue("glv_tol"));
            int glove_max_iter = Integer.parseInt(cmd.getOptionValue("glv_m"));
            int glove_threads = Integer.parseInt(cmd.getOptionValue("glv_t"));

            Path currentRelativePath = Paths.get("").toAbsolutePath();
            Path bca_file_path = currentRelativePath.resolve(bca_file_str);
            File bca_file = bca_file_path.toFile();

            if(!bca_file.exists() || !bca_file.isFile()) {
                throw new FileNotFoundException("Cannot find file " + bca_file_str);
            }

            JenaLoader loader = new JenaLoader();
            Rdf2GrphConverter converter = new Rdf2GrphConverter();
            Grph graph = converter.convert(loader.load(bca_file));

            BCAOptions bcaOptions = new BCAOptions(BCAOptions.BCAType.VANILLA, false, true, true, bca_alpha, bca_epsilon, bca_threads);

            BookmarkColoring bca;
            try(CommandLineProgress bcaProgress = new CommandLineProgress("BCA")) {
                bca = new BookmarkColoring(graph, bcaOptions, bcaProgress );
            }

            GloveModel model = new GloveModel(glove_dim, bca);

            try(CommandLineProgress gloveProgress = new CommandLineProgress("GloVe")) {
                final Optimizer optimizer;
                switch(glove_alg) {
                    default:
                        throw new UnsupportedAlgorithmException("Unsupported optimization algorithm. Use one of: adagrad, adam, adadelta, amsgrad");
                    case "adagrad":
                        optimizer = new AdagradOptimizer(model, glove_max_iter, glove_threads, glove_tol, gloveProgress);
                        break;
                    case "adam":
                        optimizer = new AdamOptimizer(model, glove_max_iter, glove_threads, glove_tol, gloveProgress);
                        break;
                    case "adadelta":
                        optimizer = new AdadeltaOptimizer(model, glove_max_iter, glove_threads, glove_tol, gloveProgress);
                        break;
                    case "amsgrad":
                        optimizer = new AMSGradOptimizer(model, glove_max_iter, glove_threads, glove_tol, gloveProgress);
                        break;
                }
                model.setOptimum(optimizer.optimize());
            }

            try(CommandLineProgress writeProgress = new CommandLineProgress("Writing to file")) {
                GloveWriter writer = new GloveTextWriter(bca_file+"."+glove_alg+"."+glove_dim);
                writer.write(model, currentRelativePath, writeProgress);
            }

        } catch (ParseException | NumberFormatException | UnsupportedAlgorithmException exception) {
            System.err.print("Parse error: ");
            System.err.println(exception.getMessage());
            formatter.printHelp("Graph Embeddings", options);
            System.exit(1);
        } catch (IOException | FileNotFoundException exception) {
            System.err.print("IO error: ");
            System.err.println(exception.getMessage());
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
