package com.tempest.config;

public class WarmingConfig {
    private MetricConfig metric;

    public MetricConfig getMetric() {
        return metric;
    }

    public void setMetric(MetricConfig metric) {
        this.metric = metric;
    }

    public static class MetricConfig {
        private String backend;
        private String filePath;
        private int flushIntervalSec = 10; // 10 seconds by default

        private KafkaConfig kafka;
        private HttpConfig http;

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

        public KafkaConfig getKafka() {
            return kafka;
        }

        public void setKafka(KafkaConfig kafka) {
            this.kafka = kafka;
        }

        public HttpConfig getHttp() {
            return http;
        }

        public void setHttp(HttpConfig http) {
            this.http = http;
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

        public static class HttpConfig {
            private String endpoint;

            public String getEndpoint() {
                return endpoint;
            }

            public void setEndpoint(String endpoint) {
                this.endpoint = endpoint;
            }
        }
    }
}
