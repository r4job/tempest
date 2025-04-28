package com.tempest.aggregation.impl;

import com.tempest.aggregation.MetricAggregator;
import com.tempest.aggregation.pojo.AggregationBucket;
import com.tempest.aggregation.pojo.AggregationKey;
import com.tempest.metric.pojo.MetricEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryAggregator implements MetricAggregator {

    private final ConcurrentHashMap<AggregationKey, AtomicInteger> counters = new ConcurrentHashMap<>();
    private final AggregationBucket bucket;

    public InMemoryAggregator(AggregationBucket bucket) {
        this.bucket = bucket;
    }

    @Override
    public void addEvent(MetricEvent event) {
        AggregationKey key = new AggregationKey(event.getObjectType(), event.getItemId(), event.getTimestamp(), bucket);
        counters.computeIfAbsent(key, k -> new AtomicInteger(0)).incrementAndGet();
    }

    @Override
    public Map<AggregationKey, Integer> collectAndReset() {
        Map<AggregationKey, Integer> snapshot = new HashMap<>();
        for (Map.Entry<AggregationKey, AtomicInteger> entry : counters.entrySet()) {
            snapshot.put(entry.getKey(), entry.getValue().get());
        }
        counters.clear();
        return snapshot;
    }
}
