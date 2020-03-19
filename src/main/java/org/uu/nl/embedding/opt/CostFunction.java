package org.uu.nl.embedding.opt;

public interface CostFunction {
    float innerCost(Optimizer opt, float Xij, int l1, int l2);
    float weightedCost(Optimizer opt, float innerCost, float Xij);
}
