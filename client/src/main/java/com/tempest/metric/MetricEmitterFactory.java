package com.tempest.metric;

import com.tempest.config.WarmingConfig;
import com.tempest.metric.impl.BufferedMetricEmitter;
import com.tempest.metric.impl.FileMetricEmitter;
import com.tempest.metric.impl.InMemoryMetricEmitter;

public class MetricEmitterFactory {

    private static final String MEMORY = "MEMORY";
    private static final String FILE = "FILE";

    public static MetricEmitter create(WarmingConfig.MetricConfig cfg) {
        final MetricEmitter backend;

        switch (cfg.getDestination().toUpperCase()) {
            case MEMORY:
                return new InMemoryMetricEmitter();
            case FILE:
                backend = new FileMetricEmitter(cfg.getFilePath());
                break;
            default:
                throw new IllegalArgumentException("Unsupported destination: " + cfg.getDestination());
        }

        return new BufferedMetricEmitter(backend, cfg.getInterval());
    }

    /*WarmingConfig config = ConfigLoader.load("warming-config.yaml");
    MetricEmitter emitter = MetricEmitterFactory.create(config.getMetric());

    emitter.emit(new Metric("item42", System.currentTimeMillis(), 1));*/
}
