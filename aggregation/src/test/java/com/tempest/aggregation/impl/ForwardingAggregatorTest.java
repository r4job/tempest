package com.tempest.aggregation.impl;

import com.tempest.aggregation.ForwardingTarget;
import com.tempest.aggregation.TestMetricEvent;
import com.tempest.aggregation.pojo.AggregationBucket;
import com.tempest.aggregation.pojo.AggregationKey;
import com.tempest.aggregation.strategy.ModRoutingStrategy;
import com.tempest.metric.MetricEvent;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ForwardingAggregatorTest {
    @Test
    public void testForwardingAggregatorWithModuloStrategy() {
        AggregationBucket bucket = new AggregationBucket(AggregationBucket.TimeUnit.MINUTE, 1);
        InMemoryAggregator shard0 = new InMemoryAggregator(bucket);
        InMemoryAggregator shard1 = new InMemoryAggregator(bucket);

        List<InMemoryAggregator> shards = Arrays.asList(shard0, shard1);

        // ForwardingTarget that delegates to a list of shards by index
        ForwardingTarget target = (event, nodeId) -> {
            int index = Integer.parseInt(nodeId.replace("node-", ""));
            shards.get(index).addEvent(event);
        };

        ModRoutingStrategy strategy = new ModRoutingStrategy(shards.size());

        ForwardingAggregator forwarder = new ForwardingAggregator(strategy, target, null);

        long now = System.currentTimeMillis();
        MetricEvent event1 = new TestMetricEvent("video", "item1", now, 1);
        MetricEvent event2 = new TestMetricEvent("video", "item1", now, 1);

        forwarder.addEvent(event1);
        forwarder.addEvent(event2);

        double total = 0;
        for (InMemoryAggregator shard : shards) {
            Map<AggregationKey, Double> result = shard.collectAndReset();
            total += result.values().stream().mapToDouble(i -> i).sum();
        }

        assertEquals(2.0, total);
    }
}
