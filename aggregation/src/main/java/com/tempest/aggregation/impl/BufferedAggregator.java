package com.tempest.aggregation.impl;

import com.tempest.aggregation.CollectingAggregator;
import com.tempest.aggregation.pojo.AggregationKey;
import com.tempest.metric.MetricEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BufferedAggregator extends AggregatorPlugin {
    private final int bufferSize;
    private final List<MetricEvent> buffer = new ArrayList<>();

    public BufferedAggregator(CollectingAggregator delegate, int bufferSize) {
        super(delegate);
        this.bufferSize = bufferSize;
    }

    @Override
    public synchronized void addEvent(MetricEvent event) {
        buffer.add(event);
        if (buffer.size() >= bufferSize) {
            flush();
        }
    }

    private void flush() {
        for (MetricEvent e : buffer) {
            delegate.addEvent(e);
        }
        buffer.clear();
    }

    @Override
    public synchronized Map<AggregationKey, Double> collectAndReset() {
        flush();
        return super.collectAndReset();
    }
}

