package com.tempest.metric;

import com.tempest.metric.impl.DurableMetricEmitter;
import com.tempest.metric.impl.RetryMetricEmitter;

import java.io.File;

public class MetricEmitterBuilder {

    private MetricEmitter base;
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

    public MetricEmitterBuilder withRetry(int maxRetries, long baseDelayMs) {
        this.enableRetry = true;
        this.maxRetries = maxRetries;
        this.retryBaseDelayMs = baseDelayMs;
        return this;
    }

    public MetricEmitterBuilder withDurability() {
        this.enableDurability = true;
        return this;
    }

    public MetricEmitterBuilder withDurability(File file) {
        this.enableDurability = true;
        this.durabilityFile = file;
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
        return emitter;
    }
}
