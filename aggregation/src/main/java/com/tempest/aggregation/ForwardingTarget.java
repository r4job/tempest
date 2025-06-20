package com.tempest.aggregation;

import com.tempest.metric.MetricEvent;

public interface ForwardingTarget {
    void forward(MetricEvent event, String target);
}
