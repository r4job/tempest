package com.tempest.aggregation.impl;

import com.tempest.aggregation.TestMetricEvent;
import com.tempest.aggregation.pojo.AggregationBucket;
import com.tempest.aggregation.pojo.AggregationKey;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InMemoryAggregatorTest {
    @Test
    public void aggregatesByItemIdAndTimeBucket() {
        AggregationBucket bucket = new AggregationBucket(AggregationBucket.TimeUnit.MINUTE, 1);
        InMemoryAggregator aggregator = new InMemoryAggregator(bucket);
        long now = System.currentTimeMillis();

        aggregator.addEvent(new TestMetricEvent("video", "item1", now, 1));
        aggregator.addEvent(new TestMetricEvent("video", "item1", now, 1));
        aggregator.addEvent(new TestMetricEvent("video", "item2", now, 1));

        Map<AggregationKey, Double> result = aggregator.collectAndReset();
        assertEquals(2, result.size());

        double total = result.values().stream().mapToDouble(i -> i).sum();
        assertEquals(3.0, total);
    }

    @Test
    public void resetsStateAfterCollect() {
        AggregationBucket bucket = new AggregationBucket(AggregationBucket.TimeUnit.MINUTE, 1);
        InMemoryAggregator aggregator = new InMemoryAggregator(bucket);

        aggregator.addEvent(new TestMetricEvent("video", "item1", System.currentTimeMillis(), 1));
        aggregator.collectAndReset();

        assertTrue(aggregator.collectAndReset().isEmpty());
    }
}
