package com.tempest.metric.impl;

import com.tempest.metric.EmitResult;
import com.tempest.metric.MetricEvent;
import com.tempest.metric.TestMetricEvent;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class KafkaMetricEmitterTest {

    @Test
    void testEmitSuccess() {
        KafkaProducer<String, String> mockProducer = mock(KafkaProducer.class);
        KafkaMetricEmitter emitter = new KafkaMetricEmitter("localhost:9092", "metrics") {
            private KafkaProducer<String, String> createProducer(String bootstrapServers) {
                return mockProducer;
            }
        };

        MetricEvent event = new TestMetricEvent("click", "id123", System.currentTimeMillis(), 2);

        doAnswer(invocation -> {
            Callback callback = invocation.getArgument(1);
            callback.onCompletion(mock(RecordMetadata.class), null);
            return null;
        }).when(mockProducer).send(any(ProducerRecord.class), any());

        CompletableFuture<EmitResult> result = emitter.emit(event);
        assertTrue(result.join().isSuccess());
    }

    @Test
    void testEmitFailure() {
        KafkaProducer<String, String> mockProducer = mock(KafkaProducer.class);
        KafkaMetricEmitter emitter = new KafkaMetricEmitter("localhost:9092", "metrics") {
            private KafkaProducer<String, String> createProducer(String bootstrapServers) {
                return mockProducer;
            }
        };

        MetricEvent event = new TestMetricEvent("click", "id123", System.currentTimeMillis(), 2);

        doAnswer(invocation -> {
            Callback callback = invocation.getArgument(1);
            callback.onCompletion(null, new RuntimeException("Kafka error"));
            return null;
        }).when(mockProducer).send(any(ProducerRecord.class), any());

        CompletableFuture<EmitResult> result = emitter.emit(event);
        assertFalse(result.join().isSuccess());
        assertTrue(result.join().getMessage().contains("Kafka error"));
    }
}

