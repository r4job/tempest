package com.tempest.aggregation;

import com.tempest.aggregation.model.AggregationKey;
import com.tempest.metric.MetricEvent;

import java.util.Map;

public interface CollectingAggregator {
    void addEvent(MetricEvent event);
    Map<AggregationKey, Double> collectAndReset();
}
