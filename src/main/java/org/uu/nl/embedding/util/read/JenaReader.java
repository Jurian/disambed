package org.uu.nl.embedding.util.read;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.log4j.Logger;

import java.io.File;

/**
 * @author Jurian Baas
 */
public class JenaReader implements Reader<Model> {

    private final static Logger logger = Logger.getLogger(JenaReader.class);

    public Model load(File file) {

        Model model;

        if(file.isFile()) {
            logger.info( "Loading " + file.getName());
            model = ModelFactory.createDefaultModel();
            RDFDataMgr.read(model, file.getPath()) ;

            logger.info( "Done loading, number of triples: " + model.size());
        } else {
            throw new IllegalArgumentException("Supplied argument is not a file");
            // System.out.println("Loading in all files in directory " + dumpFile.getPath());
            // Dataset ds = TDBFactory.createDataset(dumpFile.getPath());
            // model = ds.getDefaultModel();
        }

        return model;
    }





}
