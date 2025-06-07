package com.tempest.aggregation.impl;

import com.tempest.aggregation.MetricAggregator;
import com.tempest.aggregation.pojo.AggregationKey;
import com.tempest.metric.MetricEvent;

import java.util.Map;

public abstract class AggregatorPlugin implements MetricAggregator {

    protected final MetricAggregator delegate;

    public AggregatorPlugin(MetricAggregator delegate) {
        this.delegate = delegate;
    }

    @Override
    public void addEvent(MetricEvent event) {
        delegate.addEvent(event);
    }

    @Override
    public Map<AggregationKey, Integer> collectAndReset() {
        return delegate.collectAndReset();
    }
}

