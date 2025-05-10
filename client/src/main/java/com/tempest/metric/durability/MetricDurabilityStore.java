package com.tempest.metric.durability;

import com.tempest.metric.MetricEvent;

import java.io.IOException;
import java.util.List;

public interface MetricDurabilityStore {
    void append(MetricEvent event) throws IOException;
    List<MetricEvent> readNextBatch(int maxCount);
    void markBatchProcessed(List<MetricEvent> batch);
    void close() throws IOException;
}
