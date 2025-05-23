package com.tempest.metric.impl;

import com.tempest.metric.EmitResult;
import com.tempest.metric.MetricEmitter;
import com.tempest.metric.MetricEvent;
import com.tempest.metric.TestMetricEvent;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

class ScheduledBatchMetricEmitterTest {

    @Test
    void testFlushTriggeredByScheduler() throws Exception {
        MetricEvent event1 = new TestMetricEvent("view", "id1", System.currentTimeMillis(), 1);
        MetricEvent event2 = new TestMetricEvent("view", "id2", System.currentTimeMillis(), 1);

        MetricEmitter delegate = mock(MetricEmitter.class);
        when(delegate.emit(any())).thenReturn(CompletableFuture.completedFuture(EmitResult.ok()));

        ScheduledBatchMetricEmitter emitter = new ScheduledBatchMetricEmitter(delegate, 10, Duration.ofMillis(500));

        emitter.emit(event1).join();
        emitter.emit(event2).join();

        // Wait for the scheduled task to trigger flush
        Thread.sleep(1200);

        verify(delegate, atLeast(2)).emit(any());
    }

    @Test
    void testFlushSkipsEmptyBuffer() throws Exception {
        MetricEmitter delegate = mock(MetricEmitter.class);
        when(delegate.emit(any())).thenReturn(CompletableFuture.completedFuture(EmitResult.ok()));

        ScheduledBatchMetricEmitter emitter = new ScheduledBatchMetricEmitter(delegate, 10, Duration.ofMillis(300));

        // Sleep through two intervals, but no emits were queued
        Thread.sleep(800);

        verify(delegate, never()).emit(any());
    }
}
