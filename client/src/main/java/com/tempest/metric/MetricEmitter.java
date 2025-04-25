package com.tempest.metric;

import com.tempest.metric.pojo.MetricEvent;

public interface MetricEmitter {
    void emit(MetricEvent event);
}
