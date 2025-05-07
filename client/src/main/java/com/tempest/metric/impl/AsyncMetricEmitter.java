package com.tempest.metric.impl;

import com.tempest.metric.EmitResult;
import com.tempest.metric.MetricEmitter;
import com.tempest.metric.MetricEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class AsyncMetricEmitter implements MetricEmitter {
    private static final Logger logger = LoggerFactory.getLogger(AsyncMetricEmitter.class);

    private final MetricEmitter delegate;

    public AsyncMetricEmitter(MetricEmitter delegate) {
        this.delegate = delegate;
    }

    @Override
    public CompletableFuture<EmitResult> emit(MetricEvent event) {
        final String ERROR_PREFIX = "[AsyncMetricEmitter] Emission failed: {}";
        return delegate.emit(event).whenComplete((res, ex) -> {
            if (ex != null) {
                logger.error(ERROR_PREFIX, ex.getMessage());
            }

            if (!res.isSuccess()) {
                logger.error(ERROR_PREFIX, res.getMessage());
            }
        });
    }
}
