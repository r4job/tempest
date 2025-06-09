package com.tempest.aggregation;

import com.tempest.aggregation.pojo.AggregationKey;
import com.tempest.metric.MetricEvent;

import java.util.Map;

public interface CollectingAggregator extends MetricEventConsumer {
    Map<AggregationKey, Integer> collectAndReset(); // Returns current aggregated counts and clears state
}
