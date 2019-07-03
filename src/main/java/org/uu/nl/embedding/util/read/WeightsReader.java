package org.uu.nl.embedding.util.read;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Jurian Baas
 */
public class WeightsReader implements Reader<Map<String, Integer>> {

    private static final Logger logger = Logger.getLogger(WeightsReader.class);

    @Override
    public Map<String, Integer> load(File file) throws IOException {

        if(!file.exists()) throw new FileNotFoundException();

        final Map<String, Integer> map = new HashMap<>();

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
                        map.putIfAbsent(key, Integer.parseInt(value));
                    } catch (NumberFormatException e) {
                        logger.error("Unable to parse integer " + value);
                    }
                } catch (IndexOutOfBoundsException e) {
                    logger.error("Unable to parse line " + line);
                }
            }
        }


        return map;
    }
}
