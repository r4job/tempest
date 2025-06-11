package com.tempest.aggregation.strategy;

@FunctionalInterface
public interface AggregationStrategyFactory {
    AggregationStrategy create();
}
