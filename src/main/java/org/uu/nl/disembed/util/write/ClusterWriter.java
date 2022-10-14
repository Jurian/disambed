package org.uu.nl.embedding.util.write;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.cursors.IntCursor;
import me.tongfei.progressbar.ProgressBar;
import org.apache.log4j.Logger;
import org.uu.nl.embedding.util.Progress;
import org.uu.nl.embedding.util.config.ClusterConfiguration;
import org.uu.nl.embedding.util.config.Configuration;
import org.uu.nl.embedding.util.config.OutputConfiguration;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

        Map<Integer, IntArrayList> clusterMap = new HashMap<>();

        for (int i = 0; i < components.length; i++) {

            int[] component = components[i];
            int[] clustering = clusters[i];

            final int compSize = component.length;
            boolean[] clustered = new boolean[compSize];

            for (int j = 0; j < compSize; j++) {

                if (clustered[j]) continue;

                IntArrayList ial = new IntArrayList();
                ial.add(component[j]);
                clusterMap.put(component[j], ial);

                clustered[j] = true;

                for (int k = j + 1; k < compSize; k++) {

                    if (clustered[k]) continue;

                    if (clustering[j] == clustering[k]) {

                        IntArrayList sv = clusterMap.get(component[j]);
                        sv.add(component[k]);
                        clusterMap.put(component[j], sv);
                        clustered[k] = true;
                    }
                }
            }
        }

        final int minSize = clusterConfig.getClustersize().min;
        final int maxSize = clusterConfig.getClustersize().max;

        List<IntArrayList> validClusters = clusterMap.values().stream()
                .filter(cluster -> {
                    int size = cluster.size();
                    return size >= minSize && (maxSize <= 0 || size <= maxSize);
                }).collect(Collectors.toList());

        try (ProgressBar pb = Progress.progressBar("Writing to file", validClusters.size(), "clusters");
             java.io.Writer w = new BufferedWriter(new FileWriter(outputFolder.resolve(fileName).toFile()))) {

            int id = 1;
            for (IntArrayList cluster : validClusters) {

                int i = 1;
                for (IntCursor c : cluster) {
                    String uri = dict[c.value];
                    w.write(uri);
                    w.write(DELIMITER);
                    w.write(Integer.toString(id));
                    w.write(DELIMITER);
                    w.write(Integer.toString(i));
                    w.write(NEWLINE);
                    i++;
                }
                w.write(NEWLINE);
                id++;
                pb.step();
            }
        }
    }
}
