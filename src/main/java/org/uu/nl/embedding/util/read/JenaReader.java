package org.uu.nl.embedding.util.read;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.log4j.Logger;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdtjena.HDTGraph;

import java.io.File;
import java.io.IOException;

/**
 * @author Jurian Baas
 */
public class JenaReader implements Reader<Model> {

    private final static Logger logger = Logger.getLogger(JenaReader.class);

    public Model load(File file) throws IOException {

        Model model;

        if(file.isFile()) {
            logger.info( "Loading " + file.getName());

            if(file.getName().endsWith(".hdt")) {
                HDT hdt = HDTManager.loadHDT(file.getPath(), null);
                HDTGraph graph = new HDTGraph(hdt);
                model = ModelFactory.createModelForGraph(graph);
            } else {
                model = ModelFactory.createDefaultModel();
                RDFDataMgr.read(model, file.getPath()) ;
            }
            logger.info( "Done loading, number of triples: " + model.size());
        } else {
            throw new IllegalArgumentException("Supplied argument is not a file");
        }

        return model;
    }
}
