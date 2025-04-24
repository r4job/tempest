package com.tempest.metric;

import com.tempest.config.WarmingConfig;
import com.tempest.metric.impl.*;

public class MetricEmitterFactory {

    private static final String CSV = "CSV";
    private static final String KAFKA = "KAFKA";
    private static final String HTTP = "HTTP";
    private static final String MEMORY = "MEMORY";
    private static final String ERROR_MESSAGE_PREFIX = "Unsupported metric backend: ";

    public static MetricEmitter create(WarmingConfig.MetricConfig cfg) {
        final MetricEmitter backend;

        switch (cfg.getBackend().toUpperCase()) {
            case CSV:
                backend = new CsvMetricEmitter(cfg.getFilePath());
                break;
            case KAFKA:
                backend = new KafkaMetricEmitter(
                        cfg.getKafka().getBootstrapServers(),
                        cfg.getKafka().getTopic()
                );
                break;
            case HTTP:
                backend = new HttpMetricEmitter(cfg.getHttp().getEndpoint());
                break;
            case MEMORY:
                return new InMemoryMetricEmitter();
            default:
                throw new IllegalArgumentException(ERROR_MESSAGE_PREFIX + cfg.getBackend());
        }

        return new BufferedMetricEmitter(backend, cfg.getFlushIntervalSec());
    }

    /*WarmingConfig config = ConfigLoader.load("warming-config.yaml");
    MetricEmitter emitter = MetricEmitterFactory.create(config.getMetric());

    emitter.emit(new Metric("item42", System.currentTimeMillis(), 1));*/
}
