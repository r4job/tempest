package com.tempest.aggregation;

import com.tempest.aggregation.pojo.AggregationKey;

import java.util.Map;

public interface CollectingAggregator extends MetricEventConsumer {
    Map<AggregationKey, Double> collectAndReset(); // Returns current aggregated counts and clears state
}
