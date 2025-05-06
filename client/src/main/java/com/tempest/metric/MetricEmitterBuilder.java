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

    public void withAsync(int asyncQueueCapacity) {
        this.enableAsync = true;
        this.asyncQueueCapacity = asyncQueueCapacity;
    }

    public void withBatch(int flushIntervalSec) {
        this.enableBatch = true;
        this.flushIntervalSec = flushIntervalSec;
    }

    public void withRetry(int maxRetries, long baseDelayMs) {
        this.enableRetry = true;
        this.maxRetries = maxRetries;
        this.retryBaseDelayMs = baseDelayMs;
    }

    public void withDurability(File file) {
        this.enableDurability = true;
        this.durabilityFile = file;
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
