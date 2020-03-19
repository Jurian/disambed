package org.uu.nl.embedding.opt;

import org.apache.commons.math.util.FastMath;

public class GloveCost implements CostFunction {

    public float innerCost(Optimizer opt, float Xij, int l1, int l2) {

        float innerCost = 0;
        for (int d = 0; d < opt.dimension; d++)
            innerCost += opt.focus[d + l1] * opt.context[d + l2]; // dot product of node and context node vector
        // Add separate bias for each node
        innerCost += opt.fBias[l1] + opt.cBias[l2] - FastMath.log(Xij);
        return innerCost;
    }

    @Override
    public float weightedCost(Optimizer opt, float innerCost, float Xij) {
        return (Xij > opt.xMax) ? innerCost : (float) FastMath.pow(Xij / opt.xMax, opt.alpha) * innerCost;
    }
}