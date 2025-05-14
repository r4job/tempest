package com.tempest.metric.impl;

import com.tempest.metric.EmitResult;
import com.tempest.metric.MetricEmitter;
import com.tempest.metric.MetricEvent;
import com.tempest.metric.TestMetricEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BatchMetricEmitterTest {
    static class TestBatchEmitter extends BatchMetricEmitter {
        public TestBatchEmitter(MetricEmitter delegate, int capacity) {
            super(delegate, capacity);
        }

        @Override
        protected CompletableFuture<EmitResult> checkFlushCondition() {
            List<MetricEvent> batch = drainAll();
            return flushBuffer(batch);
        }
    }

    @Test
    void testEmitAndFlush() throws Exception {
        MetricEmitter mockDelegate = mock(MetricEmitter.class);
        when(mockDelegate.emit(any())).thenReturn(CompletableFuture.completedFuture(EmitResult.ok()));

        TestBatchEmitter emitter = new TestBatchEmitter(mockDelegate, 10);

        MetricEvent e1 = new TestMetricEvent("type", "id1", System.currentTimeMillis(), 1);
        MetricEvent e2 = new TestMetricEvent("type", "id2", System.currentTimeMillis(), 1);

        emitter.emit(e1).get();
        emitter.emit(e2).get();

        verify(mockDelegate, times(2)).emit(any());
    }

    @Test
    void testEmitRejectsWhenFull() throws Exception {
        MetricEmitter mockDelegate = mock(MetricEmitter.class);
        when(mockDelegate.emit(any())).thenReturn(CompletableFuture.completedFuture(EmitResult.ok()));

        TestBatchEmitter emitter = new TestBatchEmitter(mockDelegate, 1);

        MetricEvent e1 = new TestMetricEvent("type", "id1", System.currentTimeMillis(), 1);
        MetricEvent e2 = new TestMetricEvent("type", "id2", System.currentTimeMillis(), 1);

        emitter.emit(e1).get();
        CompletableFuture<EmitResult> result = emitter.emit(e2);

        assertFalse(result.get().isSuccess());
        verify(mockDelegate, times(1)).emit(any());
    }
}
