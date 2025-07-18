package com.tempest.aggregation.impl;

import com.tempest.aggregation.CollectingAggregator;
import com.tempest.aggregation.model.AggregationKey;
import com.tempest.metric.MetricEvent;

import java.time.Instant;
import java.util.Iterator;
import java.util.Map;


public class EvictingAggregator extends AggregatorPlugin {

    private final long maxAgeMillis;

    public EvictingAggregator(CollectingAggregator delegate, long maxAgeMillis) {
        super(delegate);
        this.maxAgeMillis = maxAgeMillis;
    }

    @Override
    public Map<AggregationKey, Double> collectAndReset() {
        Map<AggregationKey, Double> all = super.collectAndReset();
        long cutoff = Instant.now().toEpochMilli() - maxAgeMillis;

        Iterator<Map.Entry<AggregationKey, Double>> it = all.entrySet().iterator();
        while (it.hasNext()) {
            AggregationKey key = it.next().getKey();
            if (key.getTimeBucketInstant().toEpochMilli() < cutoff) {
                it.remove();
            }
        }

        return all;
    }

    @Override
    public void addEvent(MetricEvent event) {
        super.addEvent(event);
    }
}
