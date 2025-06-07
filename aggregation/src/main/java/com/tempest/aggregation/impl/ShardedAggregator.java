package com.tempest.aggregation.impl;

import com.tempest.aggregation.MetricAggregator;
import com.tempest.aggregation.pojo.AggregationKey;
import com.tempest.metric.MetricEvent;

import java.util.*;

public class ShardedAggregator implements MetricAggregator {

    private final int numShards;
    private final List<MetricAggregator> shardAggregators;

    public ShardedAggregator(int numShards, AggregatorFactory aggregatorFactory) {
        if (numShards <= 0) {
            throw new IllegalArgumentException("Number of shards must be positive");
        }
        this.numShards = numShards;
        this.shardAggregators = new ArrayList<>(numShards);
        for (int i = 0; i < numShards; i++) {
            shardAggregators.add(aggregatorFactory.create());
        }
    }

    private int shardIndex(MetricEvent event) {
        return Math.abs(Objects.hash(event.getObjectType(), event.getItemId()) % numShards);
    }

    @Override
    public void addEvent(MetricEvent event) {
        int index = shardIndex(event);
        shardAggregators.get(index).addEvent(event);
    }

    @Override
    public Map<AggregationKey, Integer> collectAndReset() {
        Map<AggregationKey, Integer> merged = new HashMap<>();
        for (MetricAggregator aggregator : shardAggregators) {
            Map<AggregationKey, Integer> partial = aggregator.collectAndReset();
            for (Map.Entry<AggregationKey, Integer> entry : partial.entrySet()) {
                merged.merge(entry.getKey(), entry.getValue(), Integer::sum);
            }
        }
        return merged;
    }

    @FunctionalInterface
    public interface AggregatorFactory {
        MetricAggregator create();
    }
}

