package com.tempest.metric.impl;

import com.tempest.metric.*;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GrpcMetricEmitterTest {

    @Test
    void testEmitSuccess() {
        MetricServiceGrpc.MetricServiceBlockingStub stub = mock(MetricServiceGrpc.MetricServiceBlockingStub.class);
        ReportAck ack = ReportAck.newBuilder().setSuccess(true).build();

        MetricEvent event = new TestMetricEvent("type", "id", System.currentTimeMillis(), 1);
        when(stub.report(event)).thenReturn(ack);

        GrpcMetricEmitter emitter = new GrpcMetricEmitter(stub);
        CompletableFuture<EmitResult> result = emitter.emit(event);

        assertTrue(result.join().isSuccess());
    }

    @Test
    void testEmitFailureAck() {
        MetricServiceGrpc.MetricServiceBlockingStub stub = mock(MetricServiceGrpc.MetricServiceBlockingStub.class);
        ReportAck ack = ReportAck.newBuilder().setSuccess(false).setMessage("error").build();

        MetricEvent event = new TestMetricEvent("type", "id", System.currentTimeMillis(), 1);
        when(stub.report(event)).thenReturn(ack);

        GrpcMetricEmitter emitter = new GrpcMetricEmitter(stub);
        CompletableFuture<EmitResult> result = emitter.emit(event);

        assertFalse(result.join().isSuccess());
        assertTrue(result.join().getMessage().contains("error"));
    }

    @Test
    void testEmitThrowsException() {
        MetricServiceGrpc.MetricServiceBlockingStub stub = mock(MetricServiceGrpc.MetricServiceBlockingStub.class);
        MetricEvent event = new TestMetricEvent("type", "id", System.currentTimeMillis(), 1);
        when(stub.report(event)).thenThrow(new RuntimeException("grpc failure"));

        GrpcMetricEmitter emitter = new GrpcMetricEmitter(stub);
        CompletableFuture<EmitResult> result = emitter.emit(event);

        assertFalse(result.join().isSuccess());
        assertTrue(result.join().getMessage().contains("grpc failure"));
    }

    // Test-only constructor for injecting mocked stub
    static class GrpcMetricEmitter extends com.tempest.metric.impl.GrpcMetricEmitter {
        public GrpcMetricEmitter(MetricServiceGrpc.MetricServiceBlockingStub stub) {
            super("localhost", 0, "", 0, 0); // unused
            this.stub = stub;
        }
    }
}
