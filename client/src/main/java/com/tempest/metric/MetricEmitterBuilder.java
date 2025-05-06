package com.tempest.metric;

import com.tempest.metric.impl.AsyncMetricEmitter;
import com.tempest.metric.impl.DurableMetricEmitter;
import com.tempest.metric.impl.RetryMetricEmitter;
import com.tempest.metric.impl.ScheduledBatchMetricEmitter;

import java.io.File;

public class MetricEmitterBuilder {

    private MetricEmitter base;

    private boolean enableAsync;
    private int asyncQueueCapacity;

    private boolean enableBatch;
    private int flushIntervalSec;

    private boolean enableRetry;
    private int maxRetries;
    private long retryBaseDelayMs;

    private boolean enableDurability;
    private File durabilityFile;

    public static MetricEmitterBuilder emitter(MetricEmitter base) {
        MetricEmitterBuilder builder = new MetricEmitterBuilder();
        builder.base = base;
        return builder;
    }

    public MetricEmitterBuilder withAsync(boolean enablement, int asyncQueueCapacity) {
        if (enablement) {
            this.enableAsync = true;
            this.asyncQueueCapacity = asyncQueueCapacity;
        }
        return this;
    }

    public MetricEmitterBuilder withBatch(boolean enablement, int flushIntervalSec) {
        if (enablement) {
            this.enableBatch = true;
            this.flushIntervalSec = flushIntervalSec;
        }
        return this;
    }

    public MetricEmitterBuilder withRetry(boolean enablement, int maxRetries, long baseDelayMs) {
        if (enablement) {
            this.enableRetry = true;
            this.maxRetries = maxRetries;
            this.retryBaseDelayMs = baseDelayMs;
        }
        return this;
    }

    public MetricEmitterBuilder withDurability(boolean enablement, File file) {
        if (enablement) {
            this.enableDurability = true;
            this.durabilityFile = file;
        }
        return this;
    }

    public MetricEmitter build() {
        MetricEmitter emitter = base;
        if (enableDurability) {
            emitter = new DurableMetricEmitter(emitter, durabilityFile);
        }
        if (enableRetry) {
            emitter = new RetryMetricEmitter(emitter, maxRetries, retryBaseDelayMs);
        }
        if (enableAsync) {
            emitter = new AsyncMetricEmitter(emitter, asyncQueueCapacity);
        }
        if (enableBatch) {
            emitter = new ScheduledBatchMetricEmitter(emitter, flushIntervalSec);
        }
        return emitter;
    }
}
