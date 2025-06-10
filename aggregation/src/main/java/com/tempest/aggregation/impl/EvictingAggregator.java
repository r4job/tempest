package com.tempest.aggregation.impl;

import com.tempest.aggregation.pojo.AggregationKey;
import com.tempest.metric.MetricEvent;

import java.time.Instant;
import java.util.Iterator;
import java.util.Map;


public class EvictingAggregator extends AggregatorPlugin {

    private final long maxAgeMillis;

    public EvictingAggregator(AggregatorPlugin delegate, long maxAgeMillis) {
        super(delegate);
        this.maxAgeMillis = maxAgeMillis;
    }

    @Override
    public Map<AggregationKey, Integer> collectAndReset() {
        Map<AggregationKey, Integer> all = super.collectAndReset();
        long cutoff = Instant.now().toEpochMilli() - maxAgeMillis;

        Iterator<Map.Entry<AggregationKey, Integer>> it = all.entrySet().iterator();
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
