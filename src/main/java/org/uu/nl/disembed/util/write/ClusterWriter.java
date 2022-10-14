package org.uu.nl.disembed.util.write;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.cursors.IntCursor;
import me.tongfei.progressbar.ProgressBar;
import org.apache.log4j.Logger;
import org.uu.nl.disembed.clustering.Util;
import org.uu.nl.disembed.util.config.ClusterConfiguration;
import org.uu.nl.disembed.util.config.Configuration;
import org.uu.nl.disembed.util.config.OutputConfiguration;
import org.uu.nl.disembed.util.progress.Progress;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public record ClusterWriter(Configuration config,
                            String[] dict, int[][] components, int[][] clusters) implements Writer {

    public final static Logger logger = Logger.getLogger(ClusterWriter.class);
    public static final String DELIMITER = "\t";
    public static final String NEWLINE = "\n";
    public static final String FILETYPE = ".tsv";

    public static final String OUTPUT_DIRECTORY = "clusters";

    @Override
    public void write() throws IOException {

        ClusterConfiguration clusterConfig = config.getClustering();
        OutputConfiguration outputConfig = config.getOutput();

        String fileName = outputConfig.getClusters().getFilename() + FILETYPE;
        logger.info("Writing file: " + fileName);

        Path outputFolder = Paths.get("").toAbsolutePath().resolve(OUTPUT_DIRECTORY);
        Files.createDirectories(outputFolder);

        List<IntArrayList> validClusters = Util.getClusters(components, clusters, clusterConfig.getClustersize().min, clusterConfig.getClustersize().max);

        try (ProgressBar pb = Progress.progressBar("Writing to file", validClusters.size(), "clusters");
             java.io.Writer w = new BufferedWriter(new FileWriter(outputFolder.resolve(fileName).toFile()))) {

            // Write configuration
            w.write(config.getClustering().toString());

            // Write header
            w.write("cluster_id");
            w.write(DELIMITER);
            w.write("uri");
            w.write(NEWLINE);

            // Write clusters
            int id = 1;
            for (IntArrayList cluster : validClusters) {

                //int i = 1;
                for (IntCursor c : cluster) {

                    w.write(Integer.toString(id));
                    w.write(DELIMITER);
                    w.write(dict[c.value]);

                    //w.write(DELIMITER);
                    //w.write(Integer.toString(i));
                    w.write(NEWLINE);
                    //i++;
                }
                w.write(NEWLINE);
                id++;
                pb.step();
            }
        }
    }
}
