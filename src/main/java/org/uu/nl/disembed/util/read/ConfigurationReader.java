package org.uu.nl.disembed.util.read;

import org.uu.nl.disembed.util.config.Configuration;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ConfigurationReader implements Reader<Configuration> {

    @Override
    public Configuration load(File file) throws IOException {
        Yaml yaml = new Yaml();
        InputStream inputStream =
                new FileInputStream(file);

        return yaml.loadAs(inputStream, Configuration.class);
    }
}