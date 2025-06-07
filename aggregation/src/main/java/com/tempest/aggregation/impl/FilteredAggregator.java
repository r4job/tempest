package com.tempest.aggregation.impl;

import com.tempest.aggregation.MetricAggregator;
import com.tempest.metric.MetricEvent;

import java.util.function.Predicate;

public class FilteredAggregator extends AggregatorPlugin {
    private final Predicate<MetricEvent> predicate;

    public FilteredAggregator(MetricAggregator delegate, Predicate<MetricEvent> predicate) {
        super(delegate);
        this.predicate = predicate;
    }

    @Override
    public void addEvent(MetricEvent event) {
        if (predicate.test(event)) {
            super.addEvent(event);
        }
    }
}
