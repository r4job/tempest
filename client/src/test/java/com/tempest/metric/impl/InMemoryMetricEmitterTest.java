package com.tempest.metric.impl;

import com.tempest.metric.EmitResult;
import com.tempest.metric.MetricEvent;
import com.tempest.metric.TestMetricEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryMetricEmitterTest {

    @Test
    void testEmitAndDrain() {
        InMemoryMetricEmitter emitter = new InMemoryMetricEmitter();

        MetricEvent e1 = new TestMetricEvent("type", "id1", System.currentTimeMillis(), 10);
        MetricEvent e2 = new TestMetricEvent("type", "id2", System.currentTimeMillis(), 20);

        CompletableFuture<EmitResult> r1 = emitter.emit(e1);
        CompletableFuture<EmitResult> r2 = emitter.emit(e2);

        assertTrue(r1.join().isSuccess());
        assertTrue(r2.join().isSuccess());

        List<MetricEvent> drained = emitter.drain();
        assertEquals(2, drained.size());
        assertTrue(drained.contains(e1));
        assertTrue(drained.contains(e2));

        List<MetricEvent> drainedAgain = emitter.drain();
        assertTrue(drainedAgain.isEmpty());
    }
}

