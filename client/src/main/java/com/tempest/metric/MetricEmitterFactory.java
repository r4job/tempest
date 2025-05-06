package com.tempest.metric;

import com.tempest.config.WarmingConfig;
import com.tempest.metric.impl.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetricEmitterFactory {
    private static final Logger logger = LoggerFactory.getLogger(MetricEmitterFactory.class);

    private static final String AWS = "AWS";
    private static final String CSV = "CSV";
    private static final String GRPC = "GRPC";
    private static final String HTTP = "HTTP";
    private static final String MEMORY = "MEMORY";
    private static final String KAFKA = "KAFKA";
    private static final String RABBITMQ = "RABBITMQ";

    private static final String ERROR_MESSAGE_PREFIX = "Unsupported metric backend: ";

    public static MetricEmitter create(WarmingConfig.MetricConfig cfg) {
        final MetricEmitter base;

        switch (cfg.getBackend().toUpperCase()) {
            case AWS:
                final WarmingConfig.MetricConfig.AwsConfig aws = cfg.getAws();
                base = new AwsSnsMetricEmitter(aws.getTopicArn(), aws.getRegion());
                break;
            case CSV:
                base = new CsvMetricEmitter(cfg.getFilePath());
                break;
            case KAFKA:
                final WarmingConfig.MetricConfig.KafkaConfig kafka = cfg.getKafka();
                base = new KafkaMetricEmitter(
                        kafka.getBootstrapServers(),
                        kafka.getTopic()
                );
                break;
            case GRPC:
                final WarmingConfig.MetricConfig.GrpcConfig grpc = cfg.getGrpc();
                base = new GrpcMetricEmitter(grpc.getHost(), grpc.getPort(), grpc.getSecretSeed(), grpc.getRotationIntervalMillis(), grpc.getTokenTTLMillis());
                break;
            case HTTP:
                base = new HttpMetricEmitter(cfg.getHttp().getEndpoint());
                break;
            case MEMORY:
                base = new InMemoryMetricEmitter();
                break;
            case RABBITMQ:
                final WarmingConfig.MetricConfig.RabbitMQConfig rabbitMQ = cfg.getRabbitMQ();
                try {
                    base = new RabbitMQMetricEmitter(rabbitMQ.getHost(), rabbitMQ.getPort(), rabbitMQ.getExchangeName(), rabbitMQ.getRoutingKey());
                } catch (Exception e) {
                    RuntimeException ex = new RuntimeException(e);
                    logger.error("[MetricEmitterFactory]: RuntimeException occurs: ", ex);
                    throw ex;
                }
                break;
            default:
                logger.error("[MetricEmitterFactory] " + ERROR_MESSAGE_PREFIX + "{}", cfg.getBackend());
                throw new IllegalArgumentException(ERROR_MESSAGE_PREFIX + cfg.getBackend());
        }

        return MetricEmitterBuilder.emitter(base)
                .withDurability(cfg.isEnableDurability(), cfg.getDurabilityFile())
                .withRetry(cfg.isEnableRetry(), cfg.getMaxRetries(), cfg.getRetryBaseDelayMs())
                .withAsync(cfg.isEnableAsync(), cfg.getAsyncQueueCapacity())
                .withBatch(cfg.isEnableBatch(), cfg.getFlushIntervalSec())
                .build();
    }

    /*WarmingConfig config = ConfigLoader.load("metric-config.yaml");
    MetricEmitter emitter = MetricEmitterFactory.create(config.getMetric());

    emitter.emit(new Metric("item42", System.currentTimeMillis(), 1));*/
}
