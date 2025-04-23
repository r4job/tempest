package com.tempest.metric;

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
