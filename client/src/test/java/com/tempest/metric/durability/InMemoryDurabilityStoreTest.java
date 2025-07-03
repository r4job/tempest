package com.tempest.metric.durability;

import com.tempest.metric.MetricEvent;
import com.tempest.metric.TestMetricEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryDurabilityStoreTest {

    private InMemoryDurabilityStore store;
    private LinkedBlockingQueue<MetricEvent> queue;

    @BeforeEach
    void setup() {
        queue = new LinkedBlockingQueue<>();
        store = new InMemoryDurabilityStore(queue);
    }

    @Test
    void testAppendAndReadNextBatch() throws IOException {
        MetricEvent e1 = new TestMetricEvent("video", "itemA", System.currentTimeMillis(), 2);
        MetricEvent e2 = new TestMetricEvent("video", "itemB", System.currentTimeMillis(), 3);

        store.append(e1);
        store.append(e2);

        List<MetricEvent> batch = store.readNextBatch(10);
        assertEquals(2, batch.size());
        assertTrue(batch.contains(e1));
        assertTrue(batch.contains(e2));
    }

    @Test
    void testReadNextBatchWithLimit() throws IOException {
        for (int i = 0; i < 5; i++) {
            store.append(new TestMetricEvent("video", "item" + i, System.currentTimeMillis(), 1));
        }

        List<MetricEvent> batch = store.readNextBatch(3);
        assertEquals(3, batch.size());

        List<MetricEvent> remaining = store.readNextBatch(10);
        assertEquals(2, remaining.size());
    }

    @Test
    void testCloseClearsQueue() throws IOException {
        store.append(new TestMetricEvent("video", "itemX", System.currentTimeMillis(), 1));
        store.close();
        assertEquals(0, queue.size());
    }

    @Test
    void testMarkBatchProcessedIsNoOp() throws IOException {
        store.append(new TestMetricEvent("video", "itemY", System.currentTimeMillis(), 1));
        List<MetricEvent> batch = store.readNextBatch(1);
        store.markBatchProcessed(batch); // should do nothing and not throw
    }
}

