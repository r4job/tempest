package com.tempest.aggregation.strategy;

public class MaxStrategy implements AggregationStrategy {
    private double max = Double.NEGATIVE_INFINITY;

    @Override
    public void add(double value) {
        if (value > max) max = value;
    }

    @Override
    public double aggregate() {
        return max == Double.NEGATIVE_INFINITY ? 0.0 : max;
    }
}

