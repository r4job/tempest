package com.tempest.aggregation.impl;

import com.tempest.aggregation.CollectingAggregator;
import com.tempest.aggregation.model.AggregationBucket;
import com.tempest.aggregation.model.AggregationKey;
import com.tempest.aggregation.strategy.AggregationStrategy;
import com.tempest.aggregation.strategy.AggregationStrategyFactory;
import com.tempest.metric.MetricEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ValueAggregator implements CollectingAggregator {
    private final AggregationBucket bucket;
    private final AggregationStrategyFactory strategyFactory;
    private final Map<AggregationKey, AggregationStrategy> strategies = new ConcurrentHashMap<>();

    public ValueAggregator(AggregationBucket bucket, AggregationStrategyFactory strategyFactory) {
        this.bucket = bucket;
        this.strategyFactory = strategyFactory;
    }

    @Override
    public void addEvent(MetricEvent event) {
        double value = event.getCount();
        AggregationKey key = new AggregationKey(event.getObjectType(), event.getItemId(), event.getTimestamp(), bucket);
        strategies.computeIfAbsent(key, k -> strategyFactory.create()).add(value);
    }

    @Override
    public Map<AggregationKey, Double> collectAndReset() {
        Map<AggregationKey, Double> result = new HashMap<>();
        for (Map.Entry<AggregationKey, AggregationStrategy> entry : strategies.entrySet()) {
            result.put(entry.getKey(), entry.getValue().aggregate());
        }
        strategies.clear();
        return result;
    }
}
