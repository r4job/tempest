package com.tempest.metric.impl;

import com.tempest.metric.EmitResult;
import com.tempest.metric.MetricEmitter;
import com.tempest.metric.MetricEvent;
import com.tempest.metric.TestMetricEvent;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FixedSizeBatchMetricEmitterTest {

    MetricEvent event1 = new TestMetricEvent("click", "id1", System.currentTimeMillis(), 1);
    MetricEvent event2 = new TestMetricEvent("click", "id2", System.currentTimeMillis(), 1);

    @Test
    void testFlushTriggeredWhenThresholdMet() {
        MetricEmitter delegate = mock(MetricEmitter.class);
        when(delegate.emit(any())).thenReturn(CompletableFuture.completedFuture(EmitResult.ok()));

        FixedSizeBatchMetricEmitter emitter = new FixedSizeBatchMetricEmitter(delegate, 10, 2);

        // first event should NOT trigger flush
        emitter.emit(event1).join();
        verify(delegate, never()).emit(any());

        // second event should trigger flush
        emitter.emit(event2).join();
        verify(delegate, times(2)).emit(any());
    }

    @Test
    void testNoFlushIfThresholdNotMet() {
        MetricEmitter delegate = mock(MetricEmitter.class);
        when(delegate.emit(any())).thenReturn(CompletableFuture.completedFuture(EmitResult.ok()));

        FixedSizeBatchMetricEmitter emitter = new FixedSizeBatchMetricEmitter(delegate, 10, 5);

        emitter.emit(event1).join();
        emitter.emit(event2).join();

        verify(delegate, never()).emit(any());
    }

    @Test
    void testEmitFailsWhenQueueFull() {
        MetricEmitter delegate = mock(MetricEmitter.class);
        when(delegate.emit(any())).thenReturn(CompletableFuture.completedFuture(EmitResult.ok()));

        FixedSizeBatchMetricEmitter emitter = new FixedSizeBatchMetricEmitter(delegate, 1, 2);

        emitter.emit(event1).join();
        CompletableFuture<EmitResult> result = emitter.emit(event2);

        assertFalse(result.join().isSuccess());
        verify(delegate, never()).emit(any());
    }
}

