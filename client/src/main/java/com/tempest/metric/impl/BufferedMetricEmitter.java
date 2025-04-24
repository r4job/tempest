package com.tempest.metric.impl;

import com.tempest.metric.MetricEmitter;
import com.tempest.metric.pojo.Metric;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BufferedMetricEmitter implements MetricEmitter {

    private final InMemoryMetricEmitter buffer = new InMemoryMetricEmitter();
    private final ScheduledExecutorService scheduler;

    public BufferedMetricEmitter(MetricEmitter backend, long intervalSec) {
        this.scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(() -> {
            List<Metric> metrics = buffer.drain();
            if (!metrics.isEmpty()) {
                for (Metric m : metrics) {
                    backend.emit(m);
                }
            }
        }, 0, intervalSec, TimeUnit.SECONDS);
    }

    @Override
    public void emit(Metric metric) {
        buffer.emit(metric);
    }

    public void shutdown() {
        scheduler.shutdownNow();
    }
}
