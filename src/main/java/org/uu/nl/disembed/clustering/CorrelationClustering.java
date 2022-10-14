package org.uu.nl.disembed.clustering;

import org.ojalgo.concurrent.Parallelism;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.optimisation.integer.IntegerStrategy;
import org.uu.nl.disembed.clustering.rules.RuleChecker;

import java.math.BigDecimal;

public class CorrelationClustering extends ClusterAlgorithm {

    public static final int MAX_SIZE = 100;

    public CorrelationClustering(int index, int[] component, RuleChecker ruleChecker, float[][] vectors, float theta, float epsilon, int threads) {
        super(index, component, ruleChecker, vectors, theta, epsilon, threads);
    }

    @Override
    public ClusterResult cluster() {

        final int n = component.length; // number of vertices
        final int edges = Util.nEdges(n); // number of edges

        final float[] penalties = usingRules() ? ruleChecker.checkComponent(component) : null;
        if(n < 3) return skip(n, penalties);

        final ExpressionsBasedModel model = new ExpressionsBasedModel();
        // Suggested by the author of ojalgo library to prevent concurrency bug
        model.options.integer(IntegerStrategy.newConfigurable().withParallelism(Parallelism.FOUR));

        // Set up binary variables (one for each edge) with weights
        final Variable[] vars = new Variable[edges];
        for(int i = 0, e = 0; i < n ; i++) {
            for(int j = i + 1; j < n; j++) {
                Variable x = model
                        .addVariable("x("+i+","+j+")")
                        .integer() // constrain to binary
                        .lower(0)  // constrain to binary
                        .upper(1)  // constrain to binary
                        .weight(Util.weight(component[i], component[j], vectors, theta, epsilon, penalties != null ? penalties[e] : 0));
                vars[e] = x;
                e++;
            }
        }

        // Add 3 constraints for every triangle in this component
        for(int[] triangle : Util.triangles(n)) {

            model.addExpression().upper(1)
                    .set(vars[triangle[0]], 1)
                    .set(vars[triangle[1]], 1)
                    .set(vars[triangle[2]], -1);

            model.addExpression().upper(1)
                    .set(vars[triangle[0]], 1)
                    .set(vars[triangle[1]], -1)
                    .set(vars[triangle[2]], 1);

            model.addExpression().upper(1)
                    .set(vars[triangle[0]], -1)
                    .set(vars[triangle[1]], 1)
                    .set(vars[triangle[2]], 1);
        }


        Optimisation.Result result = model.maximise();
        // The solution is an array with false for no edge and true for an edge
        boolean[] solution = new boolean[edges];
        int solutionEdges = 0;
        for(int e = 0; e < edges; e++) {
            solution[e] = result.get(e).equals(BigDecimal.ONE);
            if(solution[e]) {
                solutionEdges++;
            }
        }

        // We have to find the components (clusters) in our solution
        // Components are guaranteed to be cliques due to the constraints above
        int[][] pairs = new int[solutionEdges][2];
        for(int i = 0, e = 0, k = 0; i < n ; i++) {
            for(int j = i + 1; j < n; j++) {
                if(solution[e]) {
                    pairs[k][0] = i;
                    pairs[k][1] = j;
                    k++;
                }
                e++;
            }
        }
        int[][] solutionComponents = Util.connectedComponents(n, pairs);

        // Convert the components in the solution to a clustering of each entity
        // Two entities are in the same cluster if they have the same number
        // For example: component = [0, 1, 2, 3, 4]
        //              clustering = [0, 1, 0, 1, 4]
        // First and third entities are clustered together
        // Second and fourth entities are clustered together
        // Fifth element is in singleton cluster
        int[] clustering = component.clone();
        for (int[] c : solutionComponents) {
            for (int i : c) {
                clustering[i] = component[c[0]];
            }
        }

        return new ClusterResult(index, clustering);
    }
}
