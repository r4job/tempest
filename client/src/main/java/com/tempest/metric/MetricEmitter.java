package com.tempest.metric;

import com.tempest.metric.pojo.Metric;

public interface MetricEmitter {
    void emit(Metric metric);
}
