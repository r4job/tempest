package com.tempest.metric.impl;

import com.tempest.metric.MetricEmitter;
import com.tempest.metric.MetricEvent;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScheduledBatchMetricEmitter implements MetricEmitter {

    private final InMemoryMetricEmitter buffer = new InMemoryMetricEmitter();
    private final ScheduledExecutorService scheduler;

    public ScheduledBatchMetricEmitter(MetricEmitter backend, long intervalSec) {
        this.scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(() -> {
            List<MetricEvent> events = buffer.drain();
            if (!events.isEmpty()) {
                for (MetricEvent m : events) {
                    backend.emit(m);
                }
            }
        }, 0, intervalSec, TimeUnit.SECONDS);
    }

    @Override
    public void emit(MetricEvent event) {
        buffer.emit(event);
    }

    public void shutdown() {
        scheduler.shutdownNow();
    }
}
