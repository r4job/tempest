package com.tempest.metric;

import com.tempest.metric.impl.AsyncMetricEmitter;
import com.tempest.metric.impl.DurableMetricEmitter;
import com.tempest.metric.impl.RetryMetricEmitter;
import com.tempest.metric.impl.ScheduledBatchMetricEmitter;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class MetricEmitterBuilderTest {

    static class DummyEmitter implements MetricEmitter {
        @Override
        public java.util.concurrent.CompletableFuture<EmitResult> emit(MetricEvent event) {
            return java.util.concurrent.CompletableFuture.completedFuture(EmitResult.ok());
        }
    }

    @Test
    void testBuildWithAsyncOnly() {
        MetricEmitter result = MetricEmitterBuilder
                .emitter(new DummyEmitter())
                .withAsync(true)
                .build();

        assertInstanceOf(AsyncMetricEmitter.class, result);
    }

    @Test
    void testBuildWithRetryOnly() {
        MetricEmitter result = MetricEmitterBuilder
                .emitter(new DummyEmitter())
                .withRetry(true, 3, 100)
                .build();

        assertInstanceOf(RetryMetricEmitter.class, result);
    }

    @Test
    void testBuildWithDurabilityOnly() {
        File tempFile = new File("test_durability.log");
        MetricEmitter result = MetricEmitterBuilder
                .emitter(new DummyEmitter())
                .withDurability(true, tempFile)
                .build();

        assertInstanceOf(DurableMetricEmitter.class, result);
        assertTrue(tempFile.delete()); // cleanup
    }

    @Test
    void testBuildWithBatchOnly() {
        MetricEmitter result = MetricEmitterBuilder
                .emitter(new DummyEmitter())
                .withBatch(true, 1)
                .build();

        assertInstanceOf(ScheduledBatchMetricEmitter.class, result);
    }

    @Test
    void testBuildWithAllWrappers() {
        File tempFile = new File("test_all_wrappers.log");
        MetricEmitter result = MetricEmitterBuilder
                .emitter(new DummyEmitter())
                .withDurability(true, tempFile)
                .withRetry(true, 3, 100)
                .withAsync(true)
                .withBatch(true, 1)
                .build();

        assertInstanceOf(ScheduledBatchMetricEmitter.class, result);
        assertTrue(tempFile.delete()); // cleanup
    }
}

