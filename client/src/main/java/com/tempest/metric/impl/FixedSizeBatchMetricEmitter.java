package com.tempest.metric.impl;

import com.tempest.metric.EmitResult;
import com.tempest.metric.MetricEmitter;
import com.tempest.metric.MetricEvent;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Flushes metrics once the buffer exceeds a configured size.
 */
public class FixedSizeBatchMetricEmitter extends BatchMetricEmitter {
    private final int threshold;

    public FixedSizeBatchMetricEmitter(MetricEmitter delegate, int capacity, int threshold) {
        super(delegate, capacity);
        this.threshold = threshold;
    }

    @Override
    protected synchronized CompletableFuture<EmitResult> checkFlushCondition() {
        if (queue.size() >= threshold) {
            List<MetricEvent> batch = drainAll();
            return flushBuffer(batch);
        }
        return CompletableFuture.completedFuture(EmitResult.ok());
    }
}
