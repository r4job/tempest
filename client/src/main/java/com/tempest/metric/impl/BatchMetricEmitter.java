package com.tempest.metric.impl;

import com.tempest.metric.EmitResult;
import com.tempest.metric.MetricEmitter;
import com.tempest.metric.MetricEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class BatchMetricEmitter implements MetricEmitter {

    protected final MetricEmitter delegate;
    protected final LinkedBlockingQueue<MetricEvent> queue;

    public BatchMetricEmitter(MetricEmitter delegate, int capacity) {
        this.delegate = delegate;
        this.queue = new LinkedBlockingQueue<>(capacity);
    }

    @Override
    public CompletableFuture<EmitResult> emit(MetricEvent event) {
        boolean accepted = queue.offer(event);
        return accepted
                ? checkFlushCondition()
                : CompletableFuture.completedFuture(EmitResult.fail("Batch queue full"));
    }

    protected abstract CompletableFuture<EmitResult> checkFlushCondition();

    protected List<MetricEvent> drainAll() {
        List<MetricEvent> batch = new ArrayList<>();
        queue.drainTo(batch);
        return batch;
    }

    protected CompletableFuture<EmitResult> flushBuffer(List<MetricEvent> batch) {
        if (batch.isEmpty()) {
            return CompletableFuture.completedFuture(EmitResult.ok());
        }

        List<CompletableFuture<EmitResult>> futures = new ArrayList<>();
        for (MetricEvent event : batch) {
            futures.add(delegate.emit(event));
        }
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> EmitResult.ok());
    }
}
