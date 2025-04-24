package com.tempest.metric.impl;

import com.tempest.metric.MetricEmitter;
import com.tempest.metric.pojo.Metric;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AsyncMetricEmitter implements MetricEmitter {
    private final BlockingQueue<Metric> queue = new LinkedBlockingQueue<>();
    private final Thread worker;
    private final String WORKER_NAME = "async-metric-emitter-worker";

    public AsyncMetricEmitter(MetricEmitter delegate) {
        this.worker = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Metric metric = queue.take(); // blocking
                    delegate.emit(metric);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, WORKER_NAME);
        worker.start();
    }

    @Override
    public void emit(Metric metric) {
        queue.offer(metric); // non-blocking
    }

    public void shutdown() {
        worker.interrupt();
    }
}
