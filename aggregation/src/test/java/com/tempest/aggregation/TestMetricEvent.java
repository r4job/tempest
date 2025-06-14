package com.tempest.aggregation;

import com.tempest.metric.MetricEvent;

import java.io.Serializable;

public class TestMetricEvent extends MetricEvent implements Serializable {
    public TestMetricEvent(String type, String id, long timestamp, int count) {
        super(type, id, timestamp, count);
    }
}
