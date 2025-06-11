package com.tempest.aggregation.strategy;

public interface AggregationStrategy {

    void add(double value);

    double aggregate();
}

