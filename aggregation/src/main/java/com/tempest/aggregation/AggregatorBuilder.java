package com.tempest.aggregation;

import com.tempest.aggregation.impl.*;
import com.tempest.aggregation.model.AggregationBucket;
import com.tempest.aggregation.strategy.AggregationStrategyFactory;

public class AggregatorBuilder {

    private final AggregationBucket bucket;
    private int shardCount = 1;
    private int bufferSize = -1;
    private Long evictionTtl = null;
    private AggregationStrategyFactory strategyFactory = null;

    public static AggregatorBuilder create(AggregationBucket bucket) {
        return new AggregatorBuilder(bucket);
    }

    private AggregatorBuilder(AggregationBucket bucket) {
        this.bucket = bucket;
    }

    public AggregatorBuilder withSharding(int shards) {
        this.shardCount = shards;
        return this;
    }

    public AggregatorBuilder withBuffering(int bufferSize) {
        this.bufferSize = bufferSize;
        return this;
    }

    public AggregatorBuilder withEviction(long ttl) {
        this.evictionTtl = ttl;
        return this;
    }

    public AggregatorBuilder withStrategyFactory(AggregationStrategyFactory factory) {
        this.strategyFactory = factory;
        return this;
    }

    public CollectingAggregator build() {
        CollectingAggregator base = strategyFactory == null
                ? new InMemoryAggregator(bucket)
                : new ValueAggregator(bucket, strategyFactory);

        if (evictionTtl != null) {
            base = new EvictingAggregator(base, evictionTtl);
        }

        if (bufferSize > 0) {
            base = new BufferedAggregator(base, bufferSize);
        }

        if (shardCount > 1) {
            CollectingAggregator finalBase = base;
            base = new ShardedAggregator(shardCount, () -> finalBase);
        }

        return base;
    }
}
