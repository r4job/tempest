package com.tempest.metric.impl;

import com.tempest.metric.MetricEmitter;
import com.tempest.metric.pojo.MetricEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InMemoryMetricEmitter implements MetricEmitter {
    private final List<MetricEvent> buffer = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void emit(MetricEvent event) {
        buffer.add(event);
    }

    public List<MetricEvent> drain() {
        List<MetricEvent> copy;
        synchronized (buffer) {
            copy = new ArrayList<>(buffer);
            buffer.clear();
        }
        return copy;
    }
}
