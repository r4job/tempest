package com.tempest.metric.durability;

import com.tempest.metric.MetricEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * While memory storage is not durable, this class is used for unit testing.
 */
public class InMemoryDurabilityStore implements MetricDurabilityStore {
    private final BlockingQueue<MetricEvent> queue;

    public InMemoryDurabilityStore(BlockingQueue<MetricEvent> queue) {
        this.queue = queue;
    }

    @Override
    public void append(MetricEvent event) throws IOException {
        queue.offer(event);
    }

    @Override
    public List<MetricEvent> readNextBatch(int maxCount) {
        List<MetricEvent> batch = new ArrayList<>(maxCount);
        queue.drainTo(batch, maxCount);
        return batch;
    }

    @Override
    public void markBatchProcessed(List<MetricEvent> batch) {
        // No-op since events are removed during read
    }

    @Override
    public void close() throws IOException {
        queue.clear();
    }
}
