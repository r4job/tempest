package com.tempest.aggregation.impl;

import com.tempest.aggregation.CollectingAggregator;
import com.tempest.aggregation.pojo.AggregationBucket;
import com.tempest.aggregation.pojo.AggregationKey;
import com.tempest.metric.MetricEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryAggregator implements CollectingAggregator {

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
    public Map<AggregationKey, Double> collectAndReset() {
        Map<AggregationKey, Double> snapshot = new HashMap<>();
        for (Map.Entry<AggregationKey, AtomicInteger> entry : counters.entrySet()) {
            snapshot.put(entry.getKey(), entry.getValue().get() * 1.0);
        }
        counters.clear();
        return snapshot;
    }
}
