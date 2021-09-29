package org.uu.nl.embedding.util.config;

public class Opt {

    private String method;
    private double tolerance;
    private int maxiter;

    public Configuration.OptimizationMethod getMethodEnum() {
        return Configuration.OptimizationMethod.valueOf(method.toUpperCase());
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public double getTolerance() {
        return tolerance;
    }

    public void setTolerance(double tolerance) {
        this.tolerance = tolerance;
    }

    public int getMaxiter() {
        return maxiter;
    }

    public void setMaxiter(int maxiter) {
        this.maxiter = maxiter;
    }
}
