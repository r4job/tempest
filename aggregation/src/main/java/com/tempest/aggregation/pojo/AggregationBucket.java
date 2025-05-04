package com.tempest.aggregation.pojo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AggregationBucket {
    private static final Logger logger = LoggerFactory.getLogger(AggregationBucket.class);

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
            logger.error("[AggregationBucket] Illegal argument: Bucket size must be positive");
            throw new IllegalArgumentException("Bucket size must be positive");
        }

        if (count >= 60) {
            logger.error("[AggregationBucket] Illegal argument: Bucket size cannot exceed 60");
            throw new IllegalArgumentException("Bucket size cannot exceed 60");
        }
    }

    public TimeUnit getUnit() {
        return unit;
    }

    public int getCount() {
        return count;
    }
}
