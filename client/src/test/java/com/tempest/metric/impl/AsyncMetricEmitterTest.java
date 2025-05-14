package com.tempest.metric.impl;

import com.tempest.metric.EmitResult;
import com.tempest.metric.MetricEmitter;
import com.tempest.metric.MetricEvent;
import com.tempest.metric.TestMetricEvent;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AsyncMetricEmitterTest {

    @Test
    void testSuccessfulEmit() throws ExecutionException, InterruptedException {
        MetricEmitter mockDelegate = mock(MetricEmitter.class);
        MetricEvent event = new TestMetricEvent("type", "id", System.currentTimeMillis(), 1);
        EmitResult result = EmitResult.ok();

        when(mockDelegate.emit(event)).thenReturn(CompletableFuture.completedFuture(result));

        AsyncMetricEmitter emitter = new AsyncMetricEmitter(mockDelegate);
        CompletableFuture<EmitResult> future = emitter.emit(event);

        assertTrue(future.get().isSuccess());
        verify(mockDelegate, times(1)).emit(event);
    }

    @Test
    void testEmitWithFailureResult() throws ExecutionException, InterruptedException {
        MetricEmitter mockDelegate = mock(MetricEmitter.class);
        MetricEvent event = new TestMetricEvent("type", "id", System.currentTimeMillis(), 1);
        EmitResult failResult = EmitResult.fail("failed reason");

        when(mockDelegate.emit(event)).thenReturn(CompletableFuture.completedFuture(failResult));

        AsyncMetricEmitter emitter = new AsyncMetricEmitter(mockDelegate);
        CompletableFuture<EmitResult> future = emitter.emit(event);

        assertFalse(future.get().isSuccess());
        verify(mockDelegate).emit(event);
    }

    @Test
    void testEmitWithException() throws ExecutionException, InterruptedException {
        MetricEmitter mockDelegate = mock(MetricEmitter.class);
        MetricEvent event = new TestMetricEvent("type", "id", System.currentTimeMillis(), 1);
        CompletableFuture<EmitResult> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("emit failed"));

        when(mockDelegate.emit(event)).thenReturn(failedFuture);

        AsyncMetricEmitter emitter = new AsyncMetricEmitter(mockDelegate);
        CompletableFuture<EmitResult> future = emitter.emit(event);

        assertThrows(ExecutionException.class, future::get);
        verify(mockDelegate).emit(event);
    }
}