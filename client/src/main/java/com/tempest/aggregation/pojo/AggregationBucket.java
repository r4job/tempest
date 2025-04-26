package com.tempest.aggregation.pojo;

public class AggregationBucket {
    public enum TimeUnit {
        MINUTE("MINUTE"),
        HOUR("HOUR"),
        DAY("DAY");

        private final String unit;

        TimeUnit(final String unit) {
            this.unit = unit;
        }

        @Override
        public String toString() {
            return unit;
        }
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
