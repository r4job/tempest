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
        private String destination;
        private String filePath;

        public String getDestination() {
            return destination;
        }

        public void setDestination(String destination) {
            this.destination = destination;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }
    }
}
