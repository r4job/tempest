package com.tempest.metric.impl;

import com.tempest.metric.EmitResult;
import com.tempest.metric.MetricEmitter;
import com.tempest.metric.MetricEvent;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RetryMetricEmitterTest {

    MetricEvent event = mock(MetricEvent.class);

    @Test
    void testEmitSucceedsWithoutRetry() {
        MetricEmitter delegate = mock(MetricEmitter.class);
        when(delegate.emit(event)).thenReturn(CompletableFuture.completedFuture(EmitResult.ok()));

        RetryMetricEmitter emitter = new RetryMetricEmitter(delegate, 3, 10);
        EmitResult result = emitter.emit(event).join();

        assertTrue(result.isSuccess());
        verify(delegate, times(1)).emit(event);
    }

    @Test
    void testEmitFailsAndRetries() {
        MetricEmitter delegate = mock(MetricEmitter.class);
        when(delegate.emit(event))
                .thenThrow(new RuntimeException("fail 1"))
                .thenThrow(new RuntimeException("fail 2"))
                .thenReturn(CompletableFuture.completedFuture(EmitResult.ok()));

        RetryMetricEmitter emitter = new RetryMetricEmitter(delegate, 3, 1);
        EmitResult result = emitter.emit(event).join();

        assertTrue(result.isSuccess());
        verify(delegate, times(3)).emit(event);
    }

    @Test
    void testEmitFailsAfterMaxRetries() {
        MetricEmitter delegate = mock(MetricEmitter.class);
        when(delegate.emit(event)).thenThrow(new RuntimeException("always fails"));

        RetryMetricEmitter emitter = new RetryMetricEmitter(delegate, 2, 1);
        EmitResult result = emitter.emit(event).join();

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Failed after retries"));
        verify(delegate, times(3)).emit(event); // 1 original + 2 retries
    }

    @Test
    void testRetryInterrupted() {
        MetricEmitter delegate = mock(MetricEmitter.class);
        when(delegate.emit(event)).thenThrow(new RuntimeException("fail"));

        Thread.currentThread().interrupt(); // cause sleep to throw

        RetryMetricEmitter emitter = new RetryMetricEmitter(delegate, 2, 10);
        EmitResult result = emitter.emit(event).join();

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("interrupted"));
    }
}

