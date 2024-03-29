package org.uu.nl.disembed.embedding.opt;

public interface CostFunction {
    float innerCost(Optimizer opt, float Xij, int u, int v);
    float weightedCost(Optimizer opt, float innerCost, float Xij);
}
