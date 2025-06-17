package com.tempest.aggregation.pojo;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class AggregationKeyTest {

    @Test
    public void alignsToMinuteBucketCorrectly() {
        long timestamp = ZonedDateTime.of(2025, 6, 15, 12, 47, 33, 0, ZoneOffset.UTC).toInstant().toEpochMilli();
        AggregationBucket bucket = new AggregationBucket(AggregationBucket.TimeUnit.MINUTE, 5);

        AggregationKey key = new AggregationKey("video", "item123", timestamp, bucket);

        assertEquals("video", key.getObjectType());
        assertEquals("item123", key.getItemId());
        assertEquals("2025-06-15T12:45", key.getTimeBucket()); // aligned down to 12:45
    }

    @Test
    public void alignsToHourBucketCorrectly() {
        long timestamp = ZonedDateTime.of(2025, 6, 15, 14, 26, 0, 0, ZoneOffset.UTC).toInstant().toEpochMilli();
        AggregationBucket bucket = new AggregationBucket(AggregationBucket.TimeUnit.HOUR, 3);

        AggregationKey key = new AggregationKey("video", "item123", timestamp, bucket);

        assertEquals("2025-06-15T12", key.getTimeBucket()); // 14:26 -> 12:00 (3-hour window)
    }

    @Test
    public void alignsToDayBucketCorrectly() {
        long timestamp = ZonedDateTime.of(2025, 6, 10, 1, 0, 0, 0, ZoneOffset.UTC).toInstant().toEpochMilli();
        AggregationBucket bucket = new AggregationBucket(AggregationBucket.TimeUnit.DAY, 5);

        AggregationKey key = new AggregationKey("video", "itemX", timestamp, bucket);

        assertEquals("2025-06-06", key.getTimeBucket()); // aligned to 6th (5-day bucket: 10 -> 6)
    }

    @Test
    public void testEqualityAndHashCode() {
        long now = System.currentTimeMillis();
        AggregationBucket bucket = new AggregationBucket(AggregationBucket.TimeUnit.MINUTE, 1);

        AggregationKey key1 = new AggregationKey("typeA", "id1", now, bucket);
        AggregationKey key2 = new AggregationKey("typeA", "id1", now, bucket);

        assertEquals(key1, key2);
        assertEquals(key1.hashCode(), key2.hashCode());
    }

    @Test
    public void exposesStructuredTimeCorrectly() {
        long timestamp = ZonedDateTime.of(2025, 6, 15, 12, 47, 0, 0, ZoneOffset.UTC).toInstant().toEpochMilli();
        AggregationBucket bucket = new AggregationBucket(AggregationBucket.TimeUnit.MINUTE, 10);

        AggregationKey key = new AggregationKey("object", "id", timestamp, bucket);

        ZonedDateTime zdt = key.getTimeBucketZdt();
        assertEquals(40, zdt.getMinute()); // 47 aligned down to 40 for 10-min bucket
        assertEquals(2025, zdt.getYear());
        assertEquals(6, zdt.getMonthValue());
        assertEquals(15, zdt.getDayOfMonth());

        Instant instant = key.getTimeBucketInstant();
        assertEquals(zdt.toInstant(), instant);
    }
}
