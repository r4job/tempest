package com.tempest.aggregation.impl;

import com.tempest.aggregation.TestMetricEvent;
import com.tempest.aggregation.pojo.AggregationBucket;
import com.tempest.aggregation.pojo.AggregationKey;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ForwardingAggregatorTest {
    @Test
    public void testForwardingAggregatorDelegatesCorrectly() {
        AggregationBucket bucket = new AggregationBucket(AggregationBucket.TimeUnit.MINUTE, 1);
        InMemoryAggregator base = new InMemoryAggregator(bucket);
        ForwardingAggregator forwarder = new ForwardingAggregator(base::addEvent);

        long now = System.currentTimeMillis();
        forwarder.addEvent(new TestMetricEvent("video", "item1", now, 1));
        forwarder.addEvent(new TestMetricEvent("video", "item1", now, 1));

        Map<AggregationKey, Integer> result = base.collectAndReset();
        assertEquals(1, result.size());
        assertEquals(2, result.values().stream().mapToInt(i -> i).sum());
    }
}
