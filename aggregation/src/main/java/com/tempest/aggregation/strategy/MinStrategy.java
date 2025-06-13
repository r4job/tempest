package com.tempest.aggregation.strategy;

public class MinStrategy implements AggregationStrategy {
    private double min = Double.POSITIVE_INFINITY;

    @Override
    public void add(double value) {
        if (value < min) min = value;
    }

    @Override
    public double aggregate() {
        return min == Double.POSITIVE_INFINITY ? 0.0 : min;
    }
}
