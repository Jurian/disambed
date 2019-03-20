package org.uu.nl.embedding.util.load;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.log4j.Logger;

import java.io.File;

public class JenaLoader implements Loader<Model> {

    final static Logger logger = Logger.getLogger(JenaLoader.class);
    public Model load(File file) {
        return getDataDump(file);
    }

    private Model getDataDump(File dumpFile) {

        Model model = null;

        if(dumpFile.isFile()) {
            logger.info( "Loading " + dumpFile.getName());
            model = ModelFactory.createDefaultModel();
            RDFDataMgr.read(model, dumpFile.getPath()) ;
        } else {
           // System.out.println("Loading in all files in directory " + dumpFile.getPath());
           // Dataset ds = TDBFactory.createDataset(dumpFile.getPath());
           // model = ds.getDefaultModel();
        }
        logger.info( "Done loading, final model size: " + model.size());
        return model;
    }
}
