package com.tempest.aggregation.impl;

import com.tempest.aggregation.TestMetricEvent;
import com.tempest.aggregation.pojo.AggregationBucket;
import com.tempest.aggregation.pojo.AggregationKey;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BufferedAggregatorTest {
    @Test
    public void testBufferedAggregatorFlushesOnThreshold() {
        AggregationBucket bucket = new AggregationBucket(AggregationBucket.TimeUnit.MINUTE, 1);
        InMemoryAggregator base = new InMemoryAggregator(bucket);
        BufferedAggregator buffered = new BufferedAggregator(base, 3);

        long now = System.currentTimeMillis();
        buffered.addEvent(new TestMetricEvent("video", "item1", now, 1));
        buffered.addEvent(new TestMetricEvent("video", "item1", now, 1));
        assertTrue(base.collectAndReset().isEmpty());

        buffered.addEvent(new TestMetricEvent("video", "item1", now, 1)); // triggers flush
        Map<AggregationKey, Integer> result = base.collectAndReset();
        assertEquals(1, result.size());
        assertEquals(3, result.values().stream().mapToInt(i -> i).sum());
    }
}
