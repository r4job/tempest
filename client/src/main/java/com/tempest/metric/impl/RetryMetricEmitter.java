package com.tempest.metric.impl;

import com.tempest.metric.EmitResult;
import com.tempest.metric.MetricEmitter;
import com.tempest.metric.MetricEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class RetryMetricEmitter implements MetricEmitter {
    private static final Logger logger = LoggerFactory.getLogger(RetryMetricEmitter.class);

    private final MetricEmitter delegate;
    private final int maxRetries;
    private final long baseDelayMs;

    public RetryMetricEmitter(MetricEmitter delegate, int maxRetries, long baseDelayMs) {
        this.delegate = delegate;
        this.maxRetries = maxRetries;
        this.baseDelayMs = baseDelayMs;
    }

    @Override
    public CompletableFuture<EmitResult> emit(MetricEvent event) {
        for (int i = 0; i <= maxRetries; i++) {
            try {
                delegate.emit(event);
            } catch (Exception e) {
                if (i == maxRetries) {
                    final String format = "[RetryMetricEmitter] Failed after retries, due to exception: {}";
                    logger.error(format, e.getMessage());
                    return CompletableFuture.completedFuture(EmitResult.fail(format, e.getMessage()));
                } else {
                    try {
                        // TODO: more retry strategies
                        Thread.sleep((long) (baseDelayMs * Math.pow(2, i)));
                    } catch (InterruptedException ex) {
                        final String format = "[RetryMetricEmitter] retries interrupted: {}";
                        logger.error(format, ex.getMessage());
                        return CompletableFuture.completedFuture(EmitResult.fail(format, ex.getMessage()));
                    }
                }
            }
        }

        return CompletableFuture.completedFuture(EmitResult.ok());
    }
}
