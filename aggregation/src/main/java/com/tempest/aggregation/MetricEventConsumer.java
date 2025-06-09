package com.tempest.aggregation;

import com.tempest.metric.MetricEvent;

public interface MetricEventConsumer {
    void addEvent(MetricEvent event);
}
