package com.tempest.metric;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InMemoryMetricEmitter implements MetricEmitter {
    private final List<Metric> buffer = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void emit(Metric metric) {
        buffer.add(metric);
    }

    public List<Metric> drain() {
        List<Metric> copy;
        synchronized (buffer) {
            copy = new ArrayList<>(buffer);
            buffer.clear();
        }
        return copy;
    }
}
