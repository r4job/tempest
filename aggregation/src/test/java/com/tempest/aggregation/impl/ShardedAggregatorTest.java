package com.tempest.aggregation.impl;

import com.tempest.aggregation.TestMetricEvent;
import com.tempest.aggregation.model.AggregationBucket;
import com.tempest.aggregation.model.AggregationKey;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ShardedAggregatorTest {
    @Test
    public void testShardedAggregatorDistributesAndMerges() {
        AggregationBucket bucket = new AggregationBucket(AggregationBucket.TimeUnit.MINUTE, 1);
        ShardedAggregator sharded = new ShardedAggregator(4, () -> new InMemoryAggregator(bucket));

        long now = System.currentTimeMillis();
        sharded.addEvent(new TestMetricEvent("video", "item1", now, 1));
        sharded.addEvent(new TestMetricEvent("video", "item1", now, 1));
        sharded.addEvent(new TestMetricEvent("video", "item2", now, 1));

        Map<AggregationKey, Double> result = sharded.collectAndReset();
        assertEquals(2, result.size());
        assertEquals(3.0, result.values().stream().mapToDouble(i -> i).sum());
    }
}
