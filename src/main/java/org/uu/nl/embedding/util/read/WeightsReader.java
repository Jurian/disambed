package org.uu.nl.embedding.util.read;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Jurian Baas
 */
public class WeightsReader implements Reader<Map<String, Double>> {

    private static final Logger logger = Logger.getLogger(WeightsReader.class);

    @Override
    public Map<String, Double> load(File file) throws IOException {

        if(!file.exists()) throw new FileNotFoundException();

        final Map<String, Double> map = new HashMap<>();
        double sum = 0;
        try(FileReader fr = new FileReader(file); BufferedReader br = new BufferedReader(fr)) {

            String line;
            while((line = br.readLine()) != null) {

                line = line.replace(" ", "");
                if(line.length() == 0) continue; // Skip empty lines

                final int commentPos = line.indexOf('#');

                if(commentPos == 0) continue; // Whole line is comment
                if(commentPos >= 0) line = line.substring(0, commentPos); // Remove comment part


                try {

                    final int equals = line.indexOf('=');
                    final String key = line.substring(0, equals);
                    final String value = line.substring(equals + 1);

                    try {
                        double d = Double.parseDouble(value);
                        map.putIfAbsent(key, d);
                        sum += d;
                    } catch (NumberFormatException e) {
                        logger.error("Unable to parse value " + value);
                    }
                } catch (IndexOutOfBoundsException e) {
                    logger.error("Unable to parse line " + line);
                }
            }
        }

        // normalize the values
        //for(Map.Entry<String, Double> entry : map.entrySet()) {
        //    map.put(entry.getKey(), entry.getValue() / sum);
        //}

        return map;
    }
}
