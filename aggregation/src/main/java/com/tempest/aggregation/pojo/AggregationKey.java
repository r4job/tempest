package com.tempest.aggregation.pojo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class AggregationKey {
    private static final Logger logger = LoggerFactory.getLogger(AggregationKey.class);

    private final String objectType;
    private final String itemId;
    private final String timeBucket;             // for hashing/grouping
    private final ZonedDateTime timeBucketZdt;   // for eviction/comparison

    public AggregationKey(String objectType, String itemId, long timestampMillis, AggregationBucket bucket) {
        this.objectType = objectType;
        this.itemId = itemId;

        Instant instant = Instant.ofEpochMilli(timestampMillis);
        ZonedDateTime zdt = instant.atZone(ZoneOffset.UTC);
        this.timeBucketZdt = alignToBucket(zdt, bucket);
        this.timeBucket = formatBucketString(timeBucketZdt, bucket);
    }

    private ZonedDateTime alignToBucket(ZonedDateTime zdt, AggregationBucket bucket) {
        switch (bucket.getUnit()) {
            case MINUTE:
                int minute = (zdt.getMinute() / bucket.getCount()) * bucket.getCount();
                return zdt.withMinute(minute).withSecond(0).withNano(0);
            case HOUR:
                int hour = (zdt.getHour() / bucket.getCount()) * bucket.getCount();
                return zdt.withHour(hour).withMinute(0).withSecond(0).withNano(0);
            case DAY:
                int day = ((zdt.getDayOfMonth() - 1) / bucket.getCount()) * bucket.getCount() + 1;
                return zdt.withDayOfMonth(day).withHour(0).withMinute(0).withSecond(0).withNano(0);
            default:
                logger.error("[AggregationKey] Unsupported time unit: {}", bucket.getUnit());
                throw new IllegalArgumentException("Unsupported time unit: " + bucket.getUnit());
        }
    }

    private String formatBucketString(ZonedDateTime aligned, AggregationBucket bucket) {
        switch (bucket.getUnit()) {
            case MINUTE:
                return aligned.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
            case HOUR:
                return aligned.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH"));
            case DAY:
                return aligned.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            default:
                logger.error("[AggregationKey] Unsupported time unit: {}", bucket.getUnit());
                throw new IllegalArgumentException("Unsupported time unit: " + bucket.getUnit());
        }
    }

    public String getObjectType() {
        return objectType;
    }

    public String getItemId() {
        return itemId;
    }

    public String getTimeBucket() {
        return timeBucket;
    }

    public ZonedDateTime getTimeBucketZdt() {
        return timeBucketZdt;
    }

    public Instant getTimeBucketInstant() {
        return timeBucketZdt.toInstant();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AggregationKey)) return false;
        AggregationKey that = (AggregationKey) o;
        return Objects.equals(objectType, that.objectType) &&
                Objects.equals(itemId, that.itemId) &&
                Objects.equals(timeBucket, that.timeBucket);
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectType, itemId, timeBucket);
    }

    @Override
    public String toString() {
        return "AggregationKey{" +
                "objectType='" + objectType + '\'' +
                ", itemId='" + itemId + '\'' +
                ", timeBucket='" + timeBucket + '\'' +
                '}';
    }
}
