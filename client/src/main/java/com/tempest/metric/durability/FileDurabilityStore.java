package com.tempest.metric.durability;

import com.tempest.metric.MetricEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class FileDurabilityStore implements MetricDurabilityStore {
    private static final Logger logger = LoggerFactory.getLogger(FileDurabilityStore.class);

    private final MetricFileWriter writer;
    private final MetricFileReader reader;

    public FileDurabilityStore(File dir, int maxSegmentSizeBytes, int batchSize, long flushIntervalMs) throws IOException {
        this.writer = new MetricFileWriter(dir, maxSegmentSizeBytes, batchSize, flushIntervalMs);
        this.reader = new MetricFileReader(dir);
    }

    @Override
    public void append(MetricEvent event) throws IOException {
        writer.append(event);
    }

    @Override
    public List<MetricEvent> readNextBatch(int maxCount) {
        return reader.readNextBatch(maxCount);
    }

    @Override
    public void markBatchProcessed(List<MetricEvent> batch) {
        // MetricReader already deletes fully read segments
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}

