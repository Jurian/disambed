package org.uu.nl.embedding.util.read;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class WeightsReader implements Reader<Map<String, Double>> {

    @Override
    public Map<String, Double> load(File file) throws IOException {

        if(!file.exists()) throw new FileNotFoundException();

        final Map<String, Double> map = new HashMap<>();

        try(FileReader fr = new FileReader(file); BufferedReader br = new BufferedReader(fr)) {

            String line;
            while((line = br.readLine()) != null) {
                map.putIfAbsent(
                        line.substring(0, line.indexOf('=')),
                        Double.parseDouble(line.substring(line.indexOf('=')+1))
                );
            }
        }


        return map;
    }
}
