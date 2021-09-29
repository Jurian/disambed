package org.uu.nl.embedding.compare;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.uu.nl.embedding.util.config.Condition;
import org.uu.nl.embedding.util.config.Similarity;
import org.uu.nl.embedding.util.config.SimilarityGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.DoubleStream;

public class CompareResourceJob implements Callable<CompareResourceJob.CompareResult> {

    private final Model model;
    private final SimilarityGroup group;
    private final Op optimizedQuery;
    private final String uri;
    private final Var[] vars;

    public CompareResourceJob(Model model, String uri, Op op, Var[] vars, SimilarityGroup group) {
        this.model = model;
        this.group = group;
        this.uri = uri;
        this.optimizedQuery = op;
        this.vars = vars;
    }

    public CompareResult call() {

        Map<String, Float> result = new HashMap<>();

        int nrOfSimilarities = group.getSimilarities().size();
        int nrOfConditions = group.getConditions() == null ? 0 : group.getConditions().size();

        QueryIterator qIter = Algebra.exec(optimizedQuery, model);
        try {
            entityLoop:
            while(qIter.hasNext()) {
                Binding s = qIter.nextBinding();

                final String entity2 = s.get(vars[0]).getURI();

                for(int i = 0, j = 0; i < nrOfConditions; i+=2, j++) {
                    final Condition condition = group.getConditions().get(j);
                    final Node c1 = s.get(vars[1+i]);
                    final Node c2 = s.get(vars[2+i]);

                    if(c1 == null || c2 == null) continue;

                    if(condition.getConditionFunction().isValid(c1.toString(false), c2.toString(false))) {
                        continue entityLoop;
                    }
                }
                List<Double> simList = new ArrayList<>(nrOfSimilarities);
                for(int i = 0, j = 0; i < nrOfSimilarities; i+=2, j++) {
                    final Similarity similarity = group.getSimilarities().get(j);
                    final Node s1 = s.get(vars[(nrOfConditions*2)+i+1]);
                    final Node s2 = s.get(vars[(nrOfConditions*2)+i+2]);

                    if(s1  != null && s2 != null)
                        simList.add(similarity.getSimilarityFunction().similarity(s1.toString(false), s2.toString(false)));
                }
                if(simList.size() == 0) continue;
                final float finalSim = computeResultSim(simList);

                if(finalSim >= group.getThreshold()) {
                    result.compute(entity2, (k,v) -> (v == null) ? finalSim : Math.max(v, finalSim));
                }
            }
        } finally {
            qIter.close();
        }

        return new CompareResult(result, uri);
    }

    private float computeResultSim(List<Double> simList) {
        if(simList.size() == 1) {
            return simList.get(0).floatValue();
        }
        DoubleStream ds = simList.stream().mapToDouble(d -> d);
        float finalSim = 0f;
        switch (group.getResultFunctionEnum()) {
            case MIN: finalSim = (float) ds.min().orElse(0d); break;
            case MAX: finalSim = (float) ds.max().orElse(0d); break;
            case AVG: finalSim = (float) ds.average().orElse(0d); break;
            case HRM: finalSim = (float) (simList.size() / ds.map(d -> 1/d).sum()); break;
        }
        return finalSim;
    }

    static class CompareResult {

        final Map<String, Float> similarities;
        final String uri;

        CompareResult(Map<String, Float> similarities, String uri) {
            this.similarities = similarities;
            this.uri = uri;
        }
    }

}
