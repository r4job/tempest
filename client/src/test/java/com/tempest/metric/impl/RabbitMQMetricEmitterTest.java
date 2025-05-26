package com.tempest.metric.impl;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.tempest.metric.EmitResult;
import com.tempest.metric.MetricEvent;
import com.tempest.metric.TestMetricEvent;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RabbitMQMetricEmitterTest {

    @Test
    void testEmitSuccess() throws Exception {
        Connection mockConnection = mock(Connection.class);
        Channel mockChannel = mock(Channel.class);

        when(mockConnection.createChannel()).thenReturn(mockChannel);

        RabbitMQMetricEmitter emitter = new RabbitMQMetricEmitter("host", 5672, "ex", "rk") {
            private Connection createConnection(String host, int port) {
                return mockConnection;
            }
        };

        MetricEvent event = new TestMetricEvent("click", "id123", System.currentTimeMillis(), 2);

        doNothing().when(mockChannel).basicPublish(any(), any(), any(), any());
        doNothing().when(mockChannel).waitForConfirmsOrDie();

        CompletableFuture<EmitResult> result = emitter.emit(event);
        assertTrue(result.join().isSuccess());
    }

    @Test
    void testEmitFailure() throws Exception {
        Connection mockConnection = mock(Connection.class);
        Channel mockChannel = mock(Channel.class);

        when(mockConnection.createChannel()).thenReturn(mockChannel);

        RabbitMQMetricEmitter emitter = new RabbitMQMetricEmitter("host", 5672, "ex", "rk") {
            private Connection createConnection(String host, int port) {
                return mockConnection;
            }
        };

        MetricEvent event = new TestMetricEvent("click", "id123", System.currentTimeMillis(), 2);

        doThrow(new RuntimeException("mock failure")).when(mockChannel).basicPublish(any(), any(), any(), any());

        CompletableFuture<EmitResult> result = emitter.emit(event);
        assertFalse(result.join().isSuccess());
        assertTrue(result.join().getMessage().contains("mock failure"));
    }
}
