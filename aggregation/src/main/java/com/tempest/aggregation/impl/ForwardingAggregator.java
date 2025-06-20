package com.tempest.aggregation.impl;

import com.tempest.aggregation.CollectingAggregator;
import com.tempest.aggregation.ForwardingTarget;
import com.tempest.aggregation.pojo.AggregationKey;
import com.tempest.aggregation.strategy.RoutingStrategy;
import com.tempest.metric.MetricEvent;

import java.util.Collections;
import java.util.Map;

public class ForwardingAggregator implements CollectingAggregator {

    private final RoutingStrategy routingStrategy;
    private final ForwardingTarget forwardingTarget;
    private final CollectingAggregator localFallback;

    public ForwardingAggregator(RoutingStrategy routingStrategy,
                                ForwardingTarget forwardingTarget,
                                CollectingAggregator localFallback) {
        this.routingStrategy = routingStrategy;
        this.forwardingTarget = forwardingTarget;
        this.localFallback = localFallback;
    }

    @Override
    public void addEvent(MetricEvent event) {
        String target = routingStrategy.route(event);
        if (target == null && localFallback != null) {
            localFallback.addEvent(event);
        } else {
            forwardingTarget.forward(event, target);
        }
    }

    @Override
    public Map<AggregationKey, Double> collectAndReset() {
        return localFallback != null
                ? localFallback.collectAndReset()
                : Collections.emptyMap();
    }
}
