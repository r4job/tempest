package com.tempest.aggregation.pojo;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class AggregationKey {
    private final String objectType;
    private final String itemId;
    private final String minuteBucket;

    public AggregationKey(String objectType, String itemId, long timestampMillis) {
        this.objectType = objectType;
        this.itemId = itemId;
        this.minuteBucket = toMinuteBucket(timestampMillis);
    }

    private String toMinuteBucket(long timestampMillis) {
        Instant instant = Instant.ofEpochMilli(timestampMillis);
        return DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
                .withZone(ZoneOffset.UTC)
                .format(instant);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AggregationKey that = (AggregationKey) o;
        return Objects.equals(objectType, that.objectType) && Objects.equals(itemId, that.itemId) && Objects.equals(minuteBucket, that.minuteBucket);
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectType, itemId, minuteBucket);
    }

    @Override
    public String toString() {
        return "AggregationKey{" +
                "objectType='" + objectType + '\'' +
                ", itemId='" + itemId + '\'' +
                ", minuteBucket='" + minuteBucket + '\'' +
                '}';
    }
}
