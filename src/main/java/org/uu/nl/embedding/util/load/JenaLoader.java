package org.uu.nl.embedding.util.load;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;

import java.io.File;

public class JenaLoader implements Loader<Model> {


    public Model load(File file) {
        return getDataDump(file);
    }

    private Model getDataDump(File dumpFile) {

        Model model = null;

        if(dumpFile.isFile()) {
            System.out.println( "Loading " + dumpFile.getName());
            model = ModelFactory.createDefaultModel();
            RDFDataMgr.read(model, dumpFile.getPath()) ;
        } else {
           // System.out.println("Loading in all files in directory " + dumpFile.getPath());
           // Dataset ds = TDBFactory.createDataset(dumpFile.getPath());
           // model = ds.getDefaultModel();
        }
        System.out.println( "Done loading, final model size: " + model.size());
        return model;
    }
}
