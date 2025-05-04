package com.tempest.metric.impl;

import com.tempest.metric.MetricEmitter;
import com.tempest.metric.MetricEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AsyncMetricEmitter implements MetricEmitter {
    private static final Logger logger = LoggerFactory.getLogger(AsyncMetricEmitter.class);

    private final BlockingQueue<MetricEvent> queue;
    private final Thread worker;

    public AsyncMetricEmitter(MetricEmitter delegate, int capacity) {
        this.queue = new LinkedBlockingQueue<>(capacity);

        final String WORKER_NAME = "async-metric-emitter-worker";
        this.worker = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    MetricEvent event = queue.take(); // blocking
                    delegate.emit(event);
                }
            } catch (InterruptedException e) {
                logger.error("[AsyncMetricEmitter] InterruptedException occurred: {}", e.getMessage());
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
