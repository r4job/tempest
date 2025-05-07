package com.tempest.metric.impl;

import com.tempest.metric.EmitResult;
import com.tempest.metric.MetricEmitter;
import com.tempest.metric.MetricEvent;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Flushes metrics every fixed interval regardless of buffer size.
 */
public class ScheduledBatchMetricEmitter extends BatchMetricEmitter {

    public ScheduledBatchMetricEmitter(MetricEmitter delegate, int capacity, Duration interval) {
        super(delegate, capacity);
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            List<MetricEvent> batch = drainAll();
            flushBuffer(batch);
        }, interval.toMillis(), interval.toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    protected CompletableFuture<EmitResult> checkFlushCondition() {
        return CompletableFuture.completedFuture(EmitResult.ok());
    }
}