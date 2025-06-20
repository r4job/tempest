package com.tempest.aggregation.impl;

import com.tempest.aggregation.TestMetricEvent;
import com.tempest.aggregation.pojo.AggregationBucket;
import com.tempest.aggregation.pojo.AggregationKey;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FilteredAggregatorTest {
    @Test
    public void filtersOutNonMatchingEvents() {
        AggregationBucket bucket = new AggregationBucket(AggregationBucket.TimeUnit.MINUTE, 1);
        InMemoryAggregator base = new InMemoryAggregator(bucket);
        FilteredAggregator filtered = new FilteredAggregator(base, e -> e.getItemId().startsWith("a"));

        long now = System.currentTimeMillis();
        filtered.addEvent(new TestMetricEvent("video", "abc", now, 1));
        filtered.addEvent(new TestMetricEvent("video", "zzz", now, 1));

        Map<AggregationKey, Double> result = filtered.collectAndReset();
        assertEquals(1, result.size());
        assertEquals("abc", result.keySet().iterator().next().getItemId());
    }
}
