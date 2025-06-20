package com.tempest.aggregation.impl;

import com.tempest.aggregation.TestMetricEvent;
import com.tempest.aggregation.pojo.AggregationBucket;
import com.tempest.aggregation.pojo.AggregationKey;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EvictingAggregatorTest {
    @Test
    public void evictsOldEvents() {
        AggregationBucket bucket = new AggregationBucket(AggregationBucket.TimeUnit.MINUTE, 1);
        InMemoryAggregator inner = new InMemoryAggregator(bucket);
        long now = System.currentTimeMillis();

        AggregationKey oldKey = new AggregationKey("video", "old", now - 10 * 60 * 1000, bucket);
        inner.addEvent(new TestMetricEvent("video", "old", now - 10 * 60 * 1000, 1));
        inner.addEvent(new TestMetricEvent("video", "recent", now, 1));

        EvictingAggregator evicting = new EvictingAggregator(inner, 5 * 60 * 1000);
        Map<AggregationKey, Double> result = evicting.collectAndReset();

        assertEquals(1, result.size());
        assertTrue(result.keySet().stream().allMatch(k -> k.getItemId().equals("recent")));
    }
}
