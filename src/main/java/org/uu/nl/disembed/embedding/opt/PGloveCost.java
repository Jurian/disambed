package org.uu.nl.embedding.opt;

import org.apache.commons.math.util.FastMath;

public class PGloveCost implements CostFunction {

    public float innerCost(Optimizer opt, float Xij, int u, int v) {

        float innerCost = 0;
        for (int d = 0; d < opt.dimension; d++)
            innerCost += opt.focus[u][d] * opt.context[v][d]; // dot product of node and context node vector
        // Add separate bias for each node
        innerCost += opt.fBias[u] + opt.cBias[v] - FastMath.log(Xij / (1 - Xij));
        return innerCost;
    }

    @Override
    public float weightedCost(Optimizer opt, float innerCost, float Xij) {
        return Xij * innerCost;
    }
}
