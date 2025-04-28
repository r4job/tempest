package com.tempest.aggregation.pojo;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class AggregationKey {
    private final String objectType;
    private final String itemId;
    private final String timeBucket;

    public AggregationKey(String objectType, String itemId, long timestampMillis, AggregationBucket bucket) {
        this.objectType = objectType;
        this.itemId = itemId;
        this.timeBucket = computeTimeBucket(timestampMillis, bucket);
    }

    private String computeTimeBucket(long timestampMillis, AggregationBucket bucket) {
        Instant instant = Instant.ofEpochMilli(timestampMillis);
        ZonedDateTime zdt = instant.atZone(ZoneOffset.UTC);

        switch (bucket.getUnit()) {
            case MINUTE:
                int minute = (zdt.getMinute() / bucket.getCount()) * bucket.getCount();
                ZonedDateTime minuteAligned = zdt.withMinute(minute).withSecond(0).withNano(0);
                return minuteAligned.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
            case HOUR:
                int hour = (zdt.getHour() / bucket.getCount()) * bucket.getCount();
                ZonedDateTime hourAligned = zdt.withHour(hour).withMinute(0).withSecond(0).withNano(0);
                return hourAligned.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH"));
            case DAY:
                int day = (zdt.getDayOfMonth() / bucket.getCount()) * bucket.getCount();
                ZonedDateTime dayAligned = zdt.withDayOfMonth(day == 0 ? 1 : day).withHour(0).withMinute(0).withSecond(0).withNano(0);
                return dayAligned.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            default:
                throw new IllegalArgumentException("Unsupported time unit: " + bucket.getUnit());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AggregationKey that = (AggregationKey) o;
        return Objects.equals(objectType, that.objectType) && Objects.equals(itemId, that.itemId) && Objects.equals(timeBucket, that.timeBucket);
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
