package com.tempest.config;

import java.io.File;

public class WarmingConfig {
    private MetricConfig metric;
    private AggregationConfig aggregation;

    public MetricConfig getMetric() {
        return metric;
    }

    public void setMetric(MetricConfig metric) {
        this.metric = metric;
    }

    public AggregationConfig getAggregation() {
        return aggregation;
    }

    public void setAggregation(AggregationConfig aggregation) {
        this.aggregation = aggregation;
    }

    public static class MetricConfig {
        private String backend;
        private String filePath;
        private int flushIntervalSec = 10; // 10 seconds by default

        private AwsConfig aws;
        private KafkaConfig kafka;
        private GrpcConfig grpc;
        private HttpConfig http;
        private RabbitMQConfig rabbitMQ;

        private boolean enableRetry = false;
        private boolean enableDurability = false;
        private int maxRetries = 3;
        private long retryBaseDelayMs = 200;
        private File durabilityFile = new File("retry-metrics.log");

        public String getBackend() {
            return backend;
        }

        public void setBackend(String backend) {
            this.backend = backend;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public int getFlushIntervalSec() {
            return flushIntervalSec;
        }

        public void setFlushIntervalSec(int flushIntervalSec) {
            this.flushIntervalSec = flushIntervalSec;
        }

        public AwsConfig getAws() {
            return aws;
        }

        public void setAws(AwsConfig aws) {
            this.aws = aws;
        }

        public KafkaConfig getKafka() {
            return kafka;
        }

        public void setKafka(KafkaConfig kafka) {
            this.kafka = kafka;
        }

        public GrpcConfig getGrpc() {
            return grpc;
        }

        public void setGrpc(GrpcConfig grpc) {
            this.grpc = grpc;
        }

        public HttpConfig getHttp() {
            return http;
        }

        public void setHttp(HttpConfig http) {
            this.http = http;
        }

        public RabbitMQConfig getRabbitMQ() {
            return rabbitMQ;
        }

        public void setRabbitMQ(RabbitMQConfig rabbitMQ) {
            this.rabbitMQ = rabbitMQ;
        }

        public boolean isEnableRetry() {
            return enableRetry;
        }

        public void setEnableRetry(boolean enableRetry) {
            this.enableRetry = enableRetry;
        }

        public boolean isEnableDurability() {
            return enableDurability;
        }

        public void setEnableDurability(boolean enableDurability) {
            this.enableDurability = enableDurability;
        }

        public int getMaxRetries() {
            return maxRetries;
        }

        public void setMaxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
        }

        public long getRetryBaseDelayMs() {
            return retryBaseDelayMs;
        }

        public void setRetryBaseDelayMs(long retryBaseDelayMs) {
            this.retryBaseDelayMs = retryBaseDelayMs;
        }

        public File getDurabilityFile() {
            return durabilityFile;
        }

        public void setDurabilityFile(File durabilityFile) {
            this.durabilityFile = durabilityFile;
        }

        public static class AwsConfig {
            private String topicArn;
            private String region;

            public String getTopicArn() {
                return topicArn;
            }

            public void setTopicArn(String topicArn) {
                this.topicArn = topicArn;
            }

            public String getRegion() {
                return region;
            }

            public void setRegion(String region) {
                this.region = region;
            }
        }

        public static class KafkaConfig {
            private String bootstrapServers;
            private String topic;

            public String getBootstrapServers() {
                return bootstrapServers;
            }

            public void setBootstrapServers(String bootstrapServers) {
                this.bootstrapServers = bootstrapServers;
            }

            public String getTopic() {
                return topic;
            }

            public void setTopic(String topic) {
                this.topic = topic;
            }
        }

        public static class GrpcConfig {
            private String host;
            private int port;
            private String secretSeed;
            private long rotationIntervalMillis = 30 * 60 * 1000; // 30 min default
            private long tokenTTLMillis = 10 * 60 * 1000;         // 10 min default

            public String getHost() {
                return host;
            }

            public void setHost(String host) {
                this.host = host;
            }

            public int getPort() {
                return port;
            }

            public void setPort(int port) {
                this.port = port;
            }

            public String getSecretSeed() {
                return secretSeed;
            }

            public void setSecretSeed(String secretSeed) {
                this.secretSeed = secretSeed;
            }

            public long getRotationIntervalMillis() {
                return rotationIntervalMillis;
            }

            public void setRotationIntervalMillis(long rotationIntervalMillis) {
                this.rotationIntervalMillis = rotationIntervalMillis;
            }

            public long getTokenTTLMillis() {
                return tokenTTLMillis;
            }

            public void setTokenTTLMillis(long tokenTTLMillis) {
                this.tokenTTLMillis = tokenTTLMillis;
            }
        }

        public static class HttpConfig {
            private String endpoint;

            public String getEndpoint() {
                return endpoint;
            }

            public void setEndpoint(String endpoint) {
                this.endpoint = endpoint;
            }
        }

        public static class RabbitMQConfig {
            private String host;
            private int port;
            private String exchangeName;
            private String routingKey;

            public String getHost() {
                return host;
            }

            public void setHost(String host) {
                this.host = host;
            }

            public int getPort() {
                return port;
            }

            public void setPort(int port) {
                this.port = port;
            }

            public String getExchangeName() {
                return exchangeName;
            }

            public void setExchangeName(String exchangeName) {
                this.exchangeName = exchangeName;
            }

            public String getRoutingKey() {
                return routingKey;
            }

            public void setRoutingKey(String routingKey) {
                this.routingKey = routingKey;
            }
        }
    }

    public static class AggregationConfig {
        private BucketConfig bucket;

        public BucketConfig getBucket() {
            return bucket;
        }

        public void setBucket(BucketConfig bucket) {
            this.bucket = bucket;
        }

        public static class BucketConfig {
            private String unit;
            private int count;

            public String getUnit() {
                return unit;
            }

            public void setUnit(String unit) {
                this.unit = unit;
            }

            public int getCount() {
                return count;
            }

            public void setCount(int count) {
                this.count = count;
            }
        }
    }
}
