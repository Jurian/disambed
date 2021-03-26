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
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jurian Baas
 */
public class JenaReader implements Reader<Model> {

    private final static Logger logger = Logger.getLogger(JenaReader.class);

    public Model load(File file) throws IOException {

        final Model model = ModelFactory.createDefaultModel();

        if(file.isDirectory()) {

            logger.info( "Directory specified, loading all files recursively");
            List<File> files = new ArrayList<>();
            listf(file, files);

            for(File subFile: files) {
                logger.info( "Loading " + subFile.getName());
                model.add(addToModel(subFile));
            }

        } else {
            logger.info( "Loading " + file.getName());
            model.add(addToModel(file));
        }

        logger.info( "Done loading, number of triples: " + model.size());
        return model;
    }

    private Model addToModel(File file) throws IOException {
        Model model;
        if(file.getName().endsWith(".hdt")) {
            HDT hdt = HDTManager.loadHDT(file.getPath(), null);
            HDTGraph graph = new HDTGraph(hdt);
            model = ModelFactory.createModelForGraph(graph);
        } else {
            model = ModelFactory.createDefaultModel();
            RDFDataMgr.read(model, file.getPath()) ;
        }
        return model;
    }

    private void listf(File directory, List<File> files) {

        // Get all files from a directory.
        File[] fList = directory.listFiles();
        if(fList != null)
            for (File file : fList) {
                if (file.isFile()) {
                    files.add(file);
                } else if (file.isDirectory()) {
                    listf(file, files);
                }
            }
    }

}
