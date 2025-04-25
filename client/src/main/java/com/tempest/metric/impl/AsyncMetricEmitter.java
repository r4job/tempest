package com.tempest.metric.impl;

import com.tempest.metric.MetricEmitter;
import com.tempest.metric.pojo.MetricEvent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AsyncMetricEmitter implements MetricEmitter {
    private final BlockingQueue<MetricEvent> queue = new LinkedBlockingQueue<>();
    private final Thread worker;
    private final String WORKER_NAME = "async-metric-emitter-worker";

    public AsyncMetricEmitter(MetricEmitter delegate) {
        this.worker = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    MetricEvent event = queue.take(); // blocking
                    delegate.emit(event);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, WORKER_NAME);
        worker.start();
    }

    @Override
    public void emit(MetricEvent event) {
        queue.offer(event); // non-blocking
    }

    public void shutdown() {
        worker.interrupt();
    }
}
