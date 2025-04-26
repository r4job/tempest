package com.tempest.aggregation.pojo;

public class AggregationBucket {
    public enum TimeUnit {
        MINUTE,
        HOUR,
        DAY
    }

    private final TimeUnit unit;
    private final int count;

    public AggregationBucket(TimeUnit unit, int count) {
        this.unit = unit;
        this.count = count;

        if (count <= 0) {
            throw new IllegalArgumentException("Bucket size must be positive");
        }

        if (count >= 60) {
            throw new IllegalArgumentException("Bucket size cannot exceed 60");
        }
    }

    public TimeUnit getUnit() {
        return unit;
    }

    public int getSize() {
        return count;
    }
}
