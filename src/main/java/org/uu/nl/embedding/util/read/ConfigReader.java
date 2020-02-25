package org.uu.nl.embedding.util.read;


import org.uu.nl.embedding.util.config.Configuration;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ConfigReader implements Reader<Configuration> {

    @Override
    public Configuration load(File file) throws IOException {
        Yaml yaml = new Yaml();
        InputStream inputStream =
                new FileInputStream(file);

        return yaml.loadAs(inputStream, Configuration.class);
    }
}
