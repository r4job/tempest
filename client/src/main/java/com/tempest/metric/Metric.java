package com.tempest.metric;

public class Metric {
    private final String itemId;
    private final long timestamp;
    private final int count;

    public Metric(String itemId, long timestamp, int count) {
        this.itemId = itemId;
        this.timestamp = timestamp;
        this.count = count;
    }

    public Metric(String itemId, long timestamp) {
        this.itemId = itemId;
        this.timestamp = timestamp;
        this.count = 1;
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
}
