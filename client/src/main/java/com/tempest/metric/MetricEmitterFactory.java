package com.tempest.metric;

import com.tempest.config.WarmingConfig;
import com.tempest.metric.impl.FileMetricEmitter;
import com.tempest.metric.impl.InMemoryMetricEmitter;

public class MetricEmitterFactory {

    private static final String MEMORY = "MEMORY";
    private static final String FILE = "FILE";

    public static MetricEmitter create(WarmingConfig.MetricConfig cfg) {
        switch (cfg.getDestination().toUpperCase()) {
            case MEMORY:
                return new InMemoryMetricEmitter();
            case FILE:
                return new FileMetricEmitter(cfg.getFilePath());
            default:
                throw new IllegalArgumentException("Unsupported destination: " + cfg.getDestination());
        }
    }

    /*WarmingConfig config = ConfigLoader.load("warming-config.yaml");
    MetricEmitter emitter = MetricEmitterFactory.create(config.getMetric());

    emitter.emit(new Metric("item42", System.currentTimeMillis(), 1));*/
}
