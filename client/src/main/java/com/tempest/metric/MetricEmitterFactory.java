package com.tempest.metric;

import com.tempest.metric.impl.FileMetricEmitter;
import com.tempest.metric.impl.InMemoryMetricEmitter;

public class MetricEmitterFactory {
    public enum Destination {
        MEMORY, FILE
    }

    public static MetricEmitter create(Destination destination, String config) {
        switch (destination) {
            case MEMORY:
                return new InMemoryMetricEmitter();
            case FILE:
                return new FileMetricEmitter(config);
            default:
                throw new IllegalArgumentException("Unsupported destination: " + destination);
        }
    }
}
