package com.tempest.aggregation.strategy;

public class SumStrategy implements AggregationStrategy {
    private double sum = 0;

    @Override
    public void add(double value) {
        sum += value;
    }

    @Override
    public double aggregate() {
        return sum;
    }
}
