package org.uu.nl.disembed.util.write;

import com.carrotsearch.hppc.IntArrayList;
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

public record LinksetWriter(Configuration config,
                           String[] dict, int[][] components, int[][] clusters) implements Writer {

    public final static Logger logger = Logger.getLogger(LinksetWriter.class);
    public static final String NEWLINE = "\n";
    public static final String NEXT = " ;\n";
    public static final String FINISHED_RECORD = " .\n";
    public static final String FILETYPE = ".ttl";
    public static final String SAME_AS = "\towl:sameAs\t";

    public static final String OUTPUT_DIRECTORY = "linksets";

    @Override
    public void write() throws IOException {

        ClusterConfiguration clusterConfig = config.getClustering();
        OutputConfiguration outputConfig = config.getOutput();

        String fileName = outputConfig.getClusters().getFilename() + FILETYPE;
        logger.info("Writing file: " + fileName);

        Path outputFolder = Paths.get("").toAbsolutePath().resolve(OUTPUT_DIRECTORY);
        Files.createDirectories(outputFolder);

        List<IntArrayList> validClusters = Util.getClusters(components, clusters, clusterConfig.getClustersize().min, clusterConfig.getClustersize().max);

        try (ProgressBar pb = Progress.progressBar("Writing to linkset file", validClusters.size(), "clusters");
             java.io.Writer w = new BufferedWriter(new FileWriter(outputFolder.resolve(fileName).toFile()))) {

            // Write configuration
            w.write(config.getClustering().toString());
            w.write(NEWLINE);

            // Write header
            w.write("@prefix owl: <http://www.w3.org/2002/07/owl#> .");

            w.write(NEWLINE);
            w.write(NEWLINE);

            for (IntArrayList cList : validClusters) {

                int[] cluster = cList.toArray();

                for (int i = 0; i < cluster.length - 1; i++) {

                    w.write('<');
                    w.write(dict[cluster[i]]);
                    w.write('>');
                    w.write(NEWLINE);

                    for(int j = i + 1; j < cluster.length; j++) {

                        w.write(SAME_AS);
                        w.write('<');
                        w.write(dict[cluster[j]]);
                        w.write('>');
                        w.write(j == (cluster.length-1) ? FINISHED_RECORD : NEXT);
                    }
                }
                w.write(NEWLINE);
                pb.step();
            }
        }
    }
}
