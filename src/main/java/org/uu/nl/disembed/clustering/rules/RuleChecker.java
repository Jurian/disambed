package org.uu.nl.disembed.clustering.rules;

import me.tongfei.progressbar.ProgressBar;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.log4j.Logger;
import org.uu.nl.disembed.clustering.Util;
import org.uu.nl.disembed.util.config.ClusterConfiguration;
import org.uu.nl.disembed.util.progress.Progress;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class RuleChecker {

    private final static Logger logger = Logger.getLogger(RuleChecker.class);

    private final int nRules;

    private final float[] weights;
    private final boolean[] definite;

    private final String[] dict;

    private final Model model;
    private final ClusterConfiguration ruleConfig;
    private final String queryFormat;
    public static final float DEFINITE_PENALTY = 1e6f;

    public RuleChecker(Model model, String[] dict, ClusterConfiguration config) {

        this.model = model;
        this.ruleConfig = config;
        this.nRules = ruleConfig.getRules().ruleCount();
        this.weights = new float[nRules];
        this.definite = new boolean[nRules];
        this.dict = dict;
        this.queryFormat = constructBatchQueryBase(false);

        int r = 0;
        for (ClusterConfiguration.Rule rule : ruleConfig.getRules()) {
            if (rule instanceof ClusterConfiguration.DefiniteRule) {
                definite[r] = true;
                weights[r] = 1;
            } else if (rule instanceof ClusterConfiguration.ProbabilisticRule) {
                definite[r] = false;
                weights[r] = ((ClusterConfiguration.ProbabilisticRule) rule).getProbability();
            }
        }
    }

    public int[][] pruneCandidatePairs(final Model model, int[][] pairs, int maxQuerySize) {

        int currentPair = 0;
        int nPairs = pairs.length;
        int removedCount = 0;

        boolean[] removed = new boolean[nPairs];

        String queryFormat = constructBatchQueryBase(true);

        try (ProgressBar pb = Progress.progressBar("Prune candidate pairs", nPairs, "pairs")) {
            while (currentPair < nPairs) {
                PruneQueryInfo info = constructPruneQuery(queryFormat, pairs, maxQuerySize, currentPair);

                final ParameterizedSparqlString query = new ParameterizedSparqlString();
                query.setCommandText(info.pruneQuery);
                query.setNsPrefixes(model.getNsPrefixMap());

                try (QueryExecution exec = QueryExecutionFactory.create(query.asQuery(), model)) {


                    final ResultSet pairViolations = exec.execSelect();

                    while (pairViolations.hasNext()) {
                        final QuerySolution solution = pairViolations.nextSolution();

                        int i = solution.get("i").asLiteral().getInt();
                        if (removed[i]) continue;

                        for (Iterator<String> it = solution.varNames(); it.hasNext(); ) {

                            String varName = it.next();

                            if (varName.startsWith("r")) {
                                boolean v = solution.get(varName).asLiteral().getBoolean();
                                if (v) {
                                    removed[i] = true;
                                    removedCount++;
                                    break;
                                }
                            }
                        }
                    }
                }

                currentPair += info.processedPairs;
                pb.stepTo(currentPair);
                pb.setExtraMessage(removedCount + " pruned");
            }
        }

        int[][] cleanPairs = new int[nPairs - removedCount][];
        for (int i = 0, j = 0; i < nPairs; i++) {
            if (!removed[i]) {
                cleanPairs[j++] = pairs[i];
            }
        }

        logger.info("Done, pruned " + removedCount + " candidate pairs");

        return cleanPairs;
    }

    public float[] checkComponent(int[] component) {

        final int n = component.length;

        // Result of Integer.MAX_VALUE = (n * (n - 1)) / 2; solve for n
        if(n >= 65536) {
            return null;
        }

        final int e = Util.nEdges(n);

        if(e > ruleConfig.getRules().getMaxQuerySize()) {
            return null;
        }

        float[] penalties = new float[e];

        final ParameterizedSparqlString query = new ParameterizedSparqlString();
        query.setCommandText(constructComponentQuery(component, dict));
        query.setNsPrefixes(model.getNsPrefixMap());

        try (QueryExecution exec = QueryExecutionFactory.create(query.asQuery(), model)) {

            final ResultSet pairViolations = exec.execSelect();

            while (pairViolations.hasNext()) {

                final QuerySolution solution = pairViolations.nextSolution();

                int i = solution.get("i").asLiteral().getInt();

                boolean[] violations = new boolean[nRules];

                for (Iterator<String> it = solution.varNames(); it.hasNext(); ) {

                    String name = it.next();
                    if (name.startsWith("r")) {

                        int r = Integer.parseInt(name.substring(1));
                        boolean v = solution.get(name).asLiteral().getBoolean();
                        violations[r - 1] = v;
                    }
                }

                penalties[i] = violationsToPenalties(violations);
            }
        }

        return penalties;
    }

    public float[][] checkComponents(int[][] components, int maxQuerySize) {

        final int nRules = ruleConfig.getRules().ruleCount();
        final int nComponents = components.length;
        final long totalPairs = totalPairs(components);

        final float[][] penalties = new float[nComponents][];

        for (int c = 0; c < nComponents; c++) {
            int n = components[c].length;
            penalties[c] = new float[Util.nEdges(n)];
        }

        int currentComponent = 0;
        int currentPair = 0;
        int processedPairs = 0;

        int i, c, r;

        try (ProgressBar pb = Progress.progressBar("Querying violations", totalPairs, "pairs")) {
            while (currentComponent < nComponents) {

                BatchQueryInfo info = constructBatchQuery(components, maxQuerySize, currentComponent, currentPair);

                final ParameterizedSparqlString query = new ParameterizedSparqlString();
                query.setCommandText(info.batchQuery);
                query.setNsPrefixes(model.getNsPrefixMap());

                try (QueryExecution exec = QueryExecutionFactory.create(query.asQuery(), model)) {

                    final ResultSet pairViolations = exec.execSelect();

                    while (pairViolations.hasNext()) {

                        final QuerySolution solution = pairViolations.nextSolution();

                        i = solution.get("i").asLiteral().getInt();
                        c = solution.get("c").asLiteral().getInt();

                        boolean[] violations = new boolean[nRules];

                        for (Iterator<String> it = solution.varNames(); it.hasNext(); ) {

                            String name = it.next();
                            if (name.startsWith("r")) {

                                r = Integer.parseInt(name.substring(1));
                                boolean v = solution.get(name).asLiteral().getBoolean();
                                violations[r - 1] = v;
                            }
                        }

                        penalties[c][i] = violationsToPenalties(violations);
                    }
                }

                currentComponent = info.currentComponent;
                currentPair = info.currentPair;
                processedPairs += info.processedPairs;

                pb.stepTo(processedPairs);
            }
        }

        return penalties;
    }

    public String constructComponentQuery(int[] component, String[] dict) {

        StringBuilder builder = new StringBuilder();

        final int n = component.length;
        final int nPairs = ((n - 1) * n) / 2;

        int[][] pairs = Util.possiblePairs(component);

        for (int i = 0; i < nPairs; i++) {
            builder.append("\t\t(")
                    .append(0)
                    .append(" ")
                    .append(i)
                    .append(" <")
                    .append(dict[pairs[i][0]])
                    .append("><")
                    .append(dict[pairs[i][1]])
                    .append(">)\n");
        }
        return String.format(queryFormat, builder);
    }

    public BatchQueryInfo constructBatchQuery(int[][] components, final int maxQuerySize, int currentComponent, int currentPair) {

        int processedPairs;

        final int nComp = components.length;

        StringBuilder builder = new StringBuilder();
        boolean finished = false;

        int j = 0;
        int i = currentPair;
        int c = currentComponent;

        for (; c < nComp; c++) {

            int[] component = components[c];
            int[][] pairs = Util.possiblePairs(component);

            final int n = component.length;
            final int nPairs = ((n - 1) * n) / 2;

            for (; i < nPairs; i++, j++) {

                if (j == maxQuerySize) {
                    finished = true;
                    break;
                }

                builder.append("\t\t(")
                        .append(c)
                        .append(" ")
                        .append(i)
                        .append(" <")
                        .append(dict[pairs[i][0]])
                        .append("><")
                        .append(dict[pairs[i][1]])
                        .append(">)\n");
            }

            if (finished) break;

            i = 0;
        }

        currentPair = i;
        currentComponent = c;
        processedPairs = j;

        return new BatchQueryInfo(currentPair, currentComponent, processedPairs, String.format(queryFormat, builder));
    }

    public record BatchQueryInfo(int currentPair, int currentComponent, int processedPairs, String batchQuery) {}

    public PruneQueryInfo constructPruneQuery(String queryFormat, int[][] pairs, final int maxQuerySize, int currentPair) {

        final int nPairs = pairs.length;

        StringBuilder builder = new StringBuilder();

        final int c = 0;
        int j = 0;
        int i = currentPair;

        for (; i < nPairs && j < maxQuerySize; i++, j++) {
            builder.append("\t\t(")
                    .append(c)
                    .append(" ")
                    .append(i)
                    .append(" <")
                    .append(dict[pairs[i][0]])
                    .append("><")
                    .append(dict[pairs[i][1]])
                    .append(">)\n");
        }

        return new PruneQueryInfo(j, String.format(queryFormat, builder));
    }

    public record PruneQueryInfo(int processedPairs, String pruneQuery) {
    }

    private String constructBatchQueryBase(boolean onlyDefinite) {

        final String e1 = "e1";
        final String e2 = "e2";

        final StringBuilder builder = new StringBuilder();
        final Set<String> previousRules = new HashSet<>();

        for (Map.Entry<String, String> prefix : ruleConfig.getRules().getPrefixes().entrySet()) {
            builder.append("PREFIX ")
                    .append(prefix.getKey())
                    .append(":")
                    .append(prefix.getValue())
                    .append("\n");
        }

        builder.append("SELECT DISTINCT ?c ?i ?").append(e1).append(" ?").append(e2).append(" ");

        int i = 0;
        for (ClusterConfiguration.Rule rule : ruleConfig.getRules()) {
            if (onlyDefinite && !(rule instanceof ClusterConfiguration.DefiniteRule)) continue;
            builder.append("?r").append(i + 1).append(" ");
            i++;
        }

        String eType1 = ruleConfig.getRules().getTypeFrom();
        String eType2 = ruleConfig.getRules().getTypeTo();

        builder.append("WHERE {").append("\n");
        builder.append("\t?").append(e1).append(" a ").append(eType1).append(".\n");
        builder.append("\t?").append(e2).append(" a ").append(eType2).append(".\n");

        builder.append("\tVALUES (?c ?i ?").append(e1).append(" ?").append(e2).append(" ) {").append("\n").append("%s\t}").append("\n");

        for (ClusterConfiguration.Rule rule : ruleConfig.getRules()) {

            if (onlyDefinite && !(rule instanceof ClusterConfiguration.DefiniteRule)) continue;

            boolean hasTriples1 = rule.getTriples1() != null;
            boolean hasTriples2 = rule.getTriples2() != null;

            if (hasTriples1) {
                for (ClusterConfiguration.Pattern pattern : rule.getTriples1()) {
                    buildPattern(e1, builder, previousRules, pattern);
                }
            }

            if (hasTriples2) {
                for (ClusterConfiguration.Pattern pattern : rule.getTriples2()) {
                    buildPattern(e2, builder, previousRules, pattern);
                }
            }
        }

        i = 0;
        for (ClusterConfiguration.Rule rule : ruleConfig.getRules()) {

            if (onlyDefinite && !(rule instanceof ClusterConfiguration.DefiniteRule)) continue;

            String ruleString = String.join(" || ", rule.getRule());
            builder.append("\tBIND( ").append(ruleString).append(" AS ?r").append(i + 1).append(")\n");
            i++;
        }

        builder.append("} ORDER BY ASC(?c) ASC(?i)");

        return builder.toString();
    }

    private void buildPattern(String fallBack, StringBuilder builder, Set<String> previousRules, ClusterConfiguration.Pattern pattern) {
        StringBuilder patternBuilder = new StringBuilder();

        String subject = pattern.hasSubject() ? pattern.getSubject() : "?" + fallBack;
        String pred = pattern.getPredicate();
        String object = pattern.hasObject() ? pattern.getObject() : "?" + fallBack;

        patternBuilder.append(subject).append(" ").append(pred).append(" ").append(object);

        String patternString = patternBuilder.toString();

        if (!previousRules.contains(patternString)) {

            if (pattern.isOptional()) builder.append("\tOPTIONAL{").append(patternString).append(".}\n");
            else builder.append("\t").append(patternString).append(".\n");

            previousRules.add(patternString);
        }
    }

    private long totalPairs(int[][] components) {
        final int nComp = components.length;

        long totalPairs = 0;

        for (int[] index : components) {
            final int n = index.length;
            final int nPairs = ((n - 1) * n) / 2;
            totalPairs += nPairs;
        }

        return totalPairs;
    }


    float violationsToPenalties(boolean[] violations) {

        final int n = violations.length;
        double p = 1;

        for (int i = 0; i < n; i++) {

            if (violations[i]) {
                if (definite[i]) return DEFINITE_PENALTY;
                p = p * (1 - weights[i]);
            }
        }

        return (float) (1 - Math.sqrt(p));
    }
}
