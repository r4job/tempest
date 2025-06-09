package com.tempest.aggregation.impl;

import com.tempest.aggregation.MetricEventConsumer;
import com.tempest.metric.MetricEvent;

public class ForwardingAggregator implements MetricEventConsumer {
    private final EventForwarder forwarder;

    public ForwardingAggregator(EventForwarder forwarder) {
        this.forwarder = forwarder;
    }

    @Override
    public void addEvent(MetricEvent event) {
        forwarder.send(event);
    }

    public interface EventForwarder {
        void send(MetricEvent event);
    }
}
