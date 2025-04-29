package com.tempest.aggregation;

import com.tempest.aggregation.pojo.AggregationKey;
import com.tempest.metric.MetricEvent;

import java.util.Map;

public interface MetricAggregator {
    void addEvent(MetricEvent event);
    Map<AggregationKey, Integer> collectAndReset(); // Returns current aggregated counts and clears state
}
