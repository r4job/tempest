package com.tempest.metric.impl;

import com.tempest.metric.MetricEmitter;
import com.tempest.metric.MetricEvent;

public class RetryMetricEmitter implements MetricEmitter {

    private final MetricEmitter delegate;
    private final int maxRetries;
    private final long baseDelayMs;

    public RetryMetricEmitter(MetricEmitter delegate, int maxRetries, long baseDelayMs) {
        this.delegate = delegate;
        this.maxRetries = maxRetries;
        this.baseDelayMs = baseDelayMs;
    }

    @Override
    public void emit(MetricEvent event) {
        for (int i = 0; i <= maxRetries; i++) {
            try {
                delegate.emit(event);
                return;
            } catch (Exception e) {
                if (i == maxRetries) {
                    System.err.println("[RetryEmitter] Failed after retries: " + e.getMessage());
                } else {
                    try {
                        Thread.sleep((long) (baseDelayMs * Math.pow(2, i)));
                    } catch (InterruptedException ignored) {}
                }
            }
        }
    }
}
