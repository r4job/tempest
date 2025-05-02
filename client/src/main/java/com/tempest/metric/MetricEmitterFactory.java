package com.tempest.metric;

import com.tempest.config.WarmingConfig;
import com.tempest.metric.impl.*;

public class MetricEmitterFactory {

    private static final String ASYNC = "ASYNC";
    private static final String AWS = "AWS";
    private static final String BUFFERED = "BUFFERED";
    private static final String CSV = "CSV";
    private static final String GRPC = "GRPC";
    private static final String HTTP = "HTTP";
    private static final String MEMORY = "MEMORY";
    private static final String KAFKA = "KAFKA";
    private static final String RABBITMQ = "RABBITMQ";

    private static final String ERROR_MESSAGE_PREFIX = "Unsupported metric backend: ";

    public static MetricEmitter create(WarmingConfig.MetricConfig cfg) throws Exception {
        final MetricEmitter backend;

        switch (cfg.getBackend().toUpperCase()) {
            case ASYNC:
                backend = new AsyncMetricEmitter(new InMemoryMetricEmitter());
                break;
            case AWS:
                final WarmingConfig.MetricConfig.AwsConfig aws = cfg.getAws();
                backend = new AwsSnsMetricEmitter(aws.getTopicArn(), aws.getRegion());
                break;
            case BUFFERED:
                backend = new BufferedMetricEmitter(new InMemoryMetricEmitter(), cfg.getFlushIntervalSec());
                break;
            case CSV:
                backend = new CsvMetricEmitter(cfg.getFilePath());
                break;
            case KAFKA:
                final WarmingConfig.MetricConfig.KafkaConfig kafka = cfg.getKafka();
                backend = new KafkaMetricEmitter(
                        kafka.getBootstrapServers(),
                        kafka.getTopic()
                );
                break;
            case GRPC:
                final WarmingConfig.MetricConfig.GrpcConfig grpc = cfg.getGrpc();
                backend = new GrpcMetricEmitter(grpc.getHost(), grpc.getPort(), grpc.getSecretSeed(), grpc.getRotationIntervalMillis(), grpc.getTokenTTLMillis());
                break;
            case HTTP:
                backend = new HttpMetricEmitter(cfg.getHttp().getEndpoint());
                break;
            case MEMORY:
                backend = new InMemoryMetricEmitter();
                break;
            case RABBITMQ:
                final WarmingConfig.MetricConfig.RabbitMQConfig rabbitMQ = cfg.getRabbitMQ();
                backend = new RabbitMQMetricEmitter(rabbitMQ.getHost(), rabbitMQ.getPort(), rabbitMQ.getExchangeName(), rabbitMQ.getRoutingKey());
                break;
            default:
                throw new IllegalArgumentException(ERROR_MESSAGE_PREFIX + cfg.getBackend());
        }

        return backend;
    }

    /*WarmingConfig config = ConfigLoader.load("metric-config.yaml");
    MetricEmitter emitter = MetricEmitterFactory.create(config.getMetric());

    emitter.emit(new Metric("item42", System.currentTimeMillis(), 1));*/
}
