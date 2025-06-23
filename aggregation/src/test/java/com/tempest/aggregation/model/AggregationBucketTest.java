package com.tempest.aggregation.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AggregationBucketTest {

    @Test
    public void createsValidBucket() {
        AggregationBucket bucket = new AggregationBucket(AggregationBucket.TimeUnit.MINUTE, 5);
        assertEquals(AggregationBucket.TimeUnit.MINUTE, bucket.getUnit());
        assertEquals(5, bucket.getCount());
    }

    @Test
    public void throwsOnZeroCount() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new AggregationBucket(AggregationBucket.TimeUnit.HOUR, 0)
        );
        assertEquals("Bucket size must be positive", ex.getMessage());
    }

    @Test
    public void throwsOnNegativeCount() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new AggregationBucket(AggregationBucket.TimeUnit.DAY, -10)
        );
        assertEquals("Bucket size must be positive", ex.getMessage());
    }

    @Test
    public void throwsOnCountExceedingLimit() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new AggregationBucket(AggregationBucket.TimeUnit.MINUTE, 60)
        );
        assertEquals("Bucket size cannot exceed 60", ex.getMessage());
    }

    @Test
    public void timeUnitToStringMatchesExpected() {
        assertEquals("MINUTE", AggregationBucket.TimeUnit.MINUTE.toString());
        assertEquals("HOUR", AggregationBucket.TimeUnit.HOUR.toString());
        assertEquals("DAY", AggregationBucket.TimeUnit.DAY.toString());
    }
}

