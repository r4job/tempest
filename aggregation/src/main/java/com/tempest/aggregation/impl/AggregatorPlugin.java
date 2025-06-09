package com.tempest.aggregation.impl;

import com.tempest.aggregation.CollectingAggregator;
import com.tempest.aggregation.pojo.AggregationKey;
import com.tempest.metric.MetricEvent;

import java.util.Map;

public abstract class AggregatorPlugin implements CollectingAggregator {

    protected final CollectingAggregator delegate;

    public AggregatorPlugin(CollectingAggregator delegate) {
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

