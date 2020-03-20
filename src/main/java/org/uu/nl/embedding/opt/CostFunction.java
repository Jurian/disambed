package org.uu.nl.embedding.opt;

public interface CostFunction {
    float innerCost(Optimizer opt, float Xij, int u, int v, int bu, int bv);
    float weightedCost(Optimizer opt, float innerCost, float Xij);
}
