package com.tempest.metric;

public interface MetricEmitter {
    void emit(MetricEvent event);
}
