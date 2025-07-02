package com.tempest.aggregation.impl;

import com.tempest.aggregation.TestMetricEvent;
import com.tempest.aggregation.model.AggregationBucket;
import com.tempest.aggregation.model.AggregationKey;
import com.tempest.aggregation.strategy.AggregationStrategyFactory;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ValueAggregatorTest {

    private static class SumStrategy implements com.tempest.aggregation.strategy.AggregationStrategy {
        private double sum = 0;

        @Override
        public void add(double value) {
            sum += value;
        }

        @Override
        public double aggregate() {
            return sum;
        }
    }

    @Test
    public void testSingleKeyAggregation() {
        AggregationBucket bucket = new AggregationBucket(AggregationBucket.TimeUnit.MINUTE, 1);
        AggregationStrategyFactory factory = SumStrategy::new;
        ValueAggregator aggregator = new ValueAggregator(bucket, factory);

        long now = System.currentTimeMillis();
        aggregator.addEvent(new TestMetricEvent("type", "id", now, 3));
        aggregator.addEvent(new TestMetricEvent("type", "id", now, 7));

        Map<AggregationKey, Double> result = aggregator.collectAndReset();
        assertEquals(1, result.size());
        assertTrue(result.values().contains(10.0));
    }

    @Test
    public void testMultipleKeysAggregation() {
        AggregationBucket bucket = new AggregationBucket(AggregationBucket.TimeUnit.MINUTE, 1);
        AggregationStrategyFactory factory = SumStrategy::new;
        ValueAggregator aggregator = new ValueAggregator(bucket, factory);

        long now = System.currentTimeMillis();
        aggregator.addEvent(new TestMetricEvent("type", "id1", now, 1));
        aggregator.addEvent(new TestMetricEvent("type", "id2", now, 2));
        aggregator.addEvent(new TestMetricEvent("type", "id1", now, 4));

        Map<AggregationKey, Double> result = aggregator.collectAndReset();
        assertEquals(2, result.size());
        assertTrue(result.values().contains(5.0));
        assertTrue(result.values().contains(2.0));
    }

    @Test
    public void testClearAfterCollect() {
        AggregationBucket bucket = new AggregationBucket(AggregationBucket.TimeUnit.MINUTE, 1);
        AggregationStrategyFactory factory = SumStrategy::new;
        ValueAggregator aggregator = new ValueAggregator(bucket, factory);

        long now = System.currentTimeMillis();
        aggregator.addEvent(new TestMetricEvent("type", "id", now, 3));
        aggregator.collectAndReset(); // should clear internal state
        Map<AggregationKey, Double> result = aggregator.collectAndReset();

        assertEquals(0, result.size(), "Aggregator should be empty after reset");
    }
}
