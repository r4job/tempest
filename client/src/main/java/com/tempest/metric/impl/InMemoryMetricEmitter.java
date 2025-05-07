package com.tempest.metric.impl;

import com.tempest.metric.EmitResult;
import com.tempest.metric.MetricEmitter;
import com.tempest.metric.MetricEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class InMemoryMetricEmitter implements MetricEmitter {
    private final List<MetricEvent> buffer = Collections.synchronizedList(new ArrayList<>());

    @Override
    public CompletableFuture<EmitResult> emit(MetricEvent event) {
        buffer.add(event);
        return CompletableFuture.completedFuture(EmitResult.ok());
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
