package com.tempest.aggregation.strategy;

import com.tempest.metric.MetricEvent;

public interface RoutingStrategy {
    String route(MetricEvent event); // returns node ID or endpoint URI
}
