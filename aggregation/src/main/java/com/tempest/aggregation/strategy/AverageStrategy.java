package com.tempest.aggregation.strategy;

public class AverageStrategy implements AggregationStrategy {
    private double sum = 0;
    private int count = 0;

    @Override
    public void add(double value) {
        sum += value;
        ++count;
    }

    @Override
    public double aggregate() {
        return count == 0 ? 0.0 : sum / count;
    }
}
