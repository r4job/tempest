package com.tempest.metric.impl;

import com.tempest.metric.EmitResult;
import com.tempest.metric.MetricEmitter;
import com.tempest.metric.MetricEvent;
import com.tempest.metric.TestMetricEvent;
import com.tempest.metric.durability.MetricDurabilityStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class DurableMetricEmitterTest {

    private DurableMetricEmitter emitter;

    @AfterEach
    void tearDown() {
        if (emitter != null) emitter.shutdown();
    }

    @Test
    void testEmitSuccessSkipsDurability() throws IOException {
        MetricDurabilityStore mockStore = mock(MetricDurabilityStore.class);
        MetricEmitter mockDelegate = mock(MetricEmitter.class);

        MetricEvent event = new TestMetricEvent("type", "id", System.currentTimeMillis(), 1);
        when(mockDelegate.emit(event)).thenReturn(CompletableFuture.completedFuture(EmitResult.ok()));

        emitter = new DurableMetricEmitter(mockDelegate, mockStore);
        EmitResult result = emitter.emit(event).join();

        assertTrue(result.isSuccess());
        verify(mockStore, never()).append(any());
    }

    @Test
    void testEmitFailureTriggersDurability() throws Exception {
        MetricDurabilityStore mockStore = mock(MetricDurabilityStore.class);
        MetricEmitter mockDelegate = mock(MetricEmitter.class);

        MetricEvent event = new TestMetricEvent("type", "id", System.currentTimeMillis(), 1);
        when(mockDelegate.emit(event)).thenReturn(CompletableFuture.completedFuture(EmitResult.fail("fail")));

        emitter = new DurableMetricEmitter(mockDelegate, mockStore);
        EmitResult result = emitter.emit(event).join();

        assertFalse(result.isSuccess());
        verify(mockStore, times(1)).append(event);
    }

    @Test
    void testRecoveryFlow() throws Exception {
        MetricDurabilityStore mockStore = mock(MetricDurabilityStore.class);
        MetricEmitter mockDelegate = mock(MetricEmitter.class);

        MetricEvent event = new TestMetricEvent("type", "id", System.currentTimeMillis(), 1);
        when(mockStore.readNextBatch(anyInt())).thenReturn(List.of(event)).thenReturn(List.of());
        when(mockDelegate.emit(event)).thenReturn(CompletableFuture.completedFuture(EmitResult.ok()));

        emitter = new DurableMetricEmitter(mockDelegate, mockStore);

        // Wait a bit for the scheduler to run once
        Thread.sleep(3000);

        verify(mockDelegate, atLeastOnce()).emit(event);
    }
}
