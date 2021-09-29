package org.uu.nl.embedding.compare;

import me.tongfei.progressbar.ProgressBar;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.vocabulary.RDF;
import org.uu.nl.embedding.util.config.Condition;
import org.uu.nl.embedding.util.config.Configuration;
import org.uu.nl.embedding.util.config.Similarity;
import org.uu.nl.embedding.util.config.SimilarityGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class CompareResources {

    private final Model model;
    private final SimilarityGroup group;
    private final Configuration config;
    private static final String QUERY_FORMAT = "SELECT * WHERE {\n\t?entity2 a %s.\n%s%s%s\n\tVALUES(?entity1){(<%s>)}\n}";
    private static final String QUERY_ENTITY_FORMAT = "%s %s ?%s";
    private static final String QUERY_COUNT_FORMAT = "SELECT (COUNT(DISTINCT ?entity) AS ?count) WHERE {?entity a %s.}";
    private static final String SIM_1 = "s1_", SIM_2 = "s2_", CON_1 = "c1_", CON_2 = "c2_";

    public CompareResources(Model model, SimilarityGroup group, Configuration config) {
        this.model = model;
        this.group = group;
        this.config = config;
    }

    public Map<String, Map<String, Float>> doCompare() {

        List<String> patterns1 = new ArrayList<>();
        List<String> patterns2 = new ArrayList<>();

        int propertyID = 0;
        for(Similarity similarity : group.getSimilarities()) {
            if(similarity.isOptional()) {
                patterns1.add("\tOPTIONAL{" + String.format(QUERY_ENTITY_FORMAT, "?entity1", similarity.getSourcePredicate(), SIM_1 + propertyID) + ".}\n");
                patterns2.add("\tOPTIONAL{" + String.format(QUERY_ENTITY_FORMAT, "?entity2", similarity.getTargetPredicate(), SIM_2 + propertyID) + ".}\n");
            } else {
                patterns1.add(String.format("\t"+QUERY_ENTITY_FORMAT, "?entity1", similarity.getSourcePredicate(), SIM_1 + propertyID + ".\n"));
                patterns2.add(String.format("\t"+QUERY_ENTITY_FORMAT, "?entity2", similarity.getTargetPredicate(), SIM_2 + propertyID + ".\n"));
            }
            propertyID++;
        }

        propertyID = 0;
        if(group.getConditions() != null)
            for(Condition condition : group.getConditions()) {
                patterns1.add("\tOPTIONAL{" + String.format(QUERY_ENTITY_FORMAT, "?entity1", condition.getSourcePredicate(), CON_1 + propertyID) + ".}\n");
                patterns2.add("\tOPTIONAL{" + String.format(QUERY_ENTITY_FORMAT, "?entity2", condition.getTargetPredicate(), CON_2 + propertyID) + ".}\n");
                propertyID++;
            }

        int nrOfSimilarities = group.getSimilarities().size();
        int nrOfConditions = group.getConditions() == null ? 0 : group.getConditions().size();

        Var[] vars = new Var[1 + (nrOfConditions*2) + (nrOfSimilarities*2)];
        vars[0] = Var.alloc("entity2");
        for(int i = 0, j = 0; i < nrOfConditions*2; i+=2, j++) {
            vars[i+1] = Var.alloc(CON_1 + j);
            vars[i+2] = Var.alloc(CON_2 + j);
        }
        for(int i = 0, j = 0; i < nrOfSimilarities*2; i+=2, j++) {
            vars[(nrOfConditions*2)+i+1] = Var.alloc(SIM_1 + j);
            vars[(nrOfConditions*2)+i+2] = Var.alloc(SIM_2 + j);
        }

        Map<String, Map<String, Float>> result = new HashMap<>();
        ExecutorService es = Executors.newWorkStealingPool(config.getThreads());
        CompletionService<CompareResourceJob.CompareResult> completionService = new ExecutorCompletionService<>(es);
        int nrOfResources = countResources(group.getSourceType());
        StmtIterator stmtIterator = model.listStatements(null, RDF.type, model.getResource(model.expandPrefix(group.getTargetType())));

        String queryString = String.format(
                QUERY_FORMAT,
                group.getTargetType(),
                String.join("", patterns1),
                String.join("", patterns2),
                group.getSourceType().equals(group.getTargetType()) ? "\tFILTER(STR(?entity1) < STR(?entity2))" : "",
                "%s"
        );

        ParameterizedSparqlString paramQuery = new ParameterizedSparqlString();
        paramQuery.setNsPrefixes(model.getNsPrefixMap());

        while(stmtIterator.hasNext()) {
            String uri = stmtIterator.nextStatement().getSubject().asNode().getURI();
            paramQuery.setCommandText(String.format(queryString, uri));
            Op op = Algebra.optimize(Algebra.compile(paramQuery.asQuery()));
            completionService.submit(new CompareResourceJob(model, uri, op, vars, group));
        }

        int processed = 0;
        int createdLinks = 0;
        try(ProgressBar pb = Configuration.progressBar("Comparing", nrOfResources, "resources")) {
            while (processed < nrOfResources) {
                try {

                    CompareResourceJob.CompareResult compareResult = completionService.take().get();
                    createdLinks += compareResult.similarities.size();
                    result.put(compareResult.uri, compareResult.similarities);

                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                } finally {
                    processed++;
                    pb.setExtraMessage(createdLinks+"");
                    pb.step();
                }
            }
        }

        return result;
    }

    private int countResources(String type) {
        String countQuery = String.format(QUERY_COUNT_FORMAT, type);
        ParameterizedSparqlString paramQuery = new ParameterizedSparqlString();
        paramQuery.setCommandText(countQuery);
        paramQuery.setNsPrefixes(model.getNsPrefixMap());
        try (QueryExecution execCount = QueryExecutionFactory.create(paramQuery.asQuery(), model)) {
            final ResultSet result = execCount.execSelect();
            return result.nextSolution().get("count").asLiteral().getInt();
        }
    }

}
