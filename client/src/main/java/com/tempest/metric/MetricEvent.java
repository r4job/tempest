package com.tempest.metric;

import java.io.Serializable;

public abstract class MetricEvent implements Serializable {
    private static final int COUNT_ONE = 1;
    private final String objectType;
    private final String itemId;
    private final long timestamp;
    private final int count;

    public MetricEvent(String objectType, String itemId, long timestamp, int count) {
        this.objectType = objectType;
        this.itemId = itemId;
        this.timestamp = timestamp;
        this.count = count;
    }

    public MetricEvent(String objectType, String itemId, long timestamp) {
        this.objectType = objectType;
        this.itemId = itemId;
        this.timestamp = timestamp;
        this.count = COUNT_ONE;
    }

    public int getCount() {
        return count;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getItemId() {
        return itemId;
    }

    public String getObjectType() {
        return objectType;
    }
}
