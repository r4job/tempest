package com.tempest.metric.impl;

import com.tempest.metric.EmitResult;
import com.tempest.metric.MetricEvent;
import com.tempest.metric.TestMetricEvent;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AwsSnsMetricEmitterTest {

    @Test
    void testSuccessfulPublish() {
        SnsClient mockClient = mock(SnsClient.class);
        PublishResponse mockResponse = mock(PublishResponse.class);
        when(mockResponse.sdkHttpResponse()).thenReturn(PublishResponse.builder().build().sdkHttpResponse());

        when(mockClient.publish(any(PublishRequest.class))).thenReturn(mockResponse);

        AwsSnsMetricEmitter emitter = new AwsSnsMetricEmitter("arn:topic", "us-east-1");
        MetricEvent event = new TestMetricEvent("type", "id", System.currentTimeMillis(), 1);
        CompletableFuture<EmitResult> result = emitter.emit(event);

        assertTrue(result.join().isSuccess());
        verify(mockClient).publish((PublishRequest) any());
    }

    @Test
    void testUnsuccessfulResponse() {
        SnsClient mockClient = mock(SnsClient.class);
        PublishResponse mockResponse = mock(PublishResponse.class);
        when(mockResponse.sdkHttpResponse().isSuccessful()).thenReturn(false);
        when(mockResponse.sdkHttpResponse().statusText()).thenReturn(Optional.of("400 Bad Request"));

        when(mockClient.publish((PublishRequest) any())).thenReturn(mockResponse);

        AwsSnsMetricEmitter emitter = new AwsSnsMetricEmitter("arn:topic", "us-east-1");
        MetricEvent event = new TestMetricEvent("type", "id", System.currentTimeMillis(), 1);
        EmitResult result = emitter.emit(event).join();

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("400 Bad Request"));
    }

    @Test
    void testPublishThrowsException() {
        SnsClient mockClient = mock(SnsClient.class);
        when(mockClient.publish((PublishRequest) any())).thenThrow(new RuntimeException("Network error"));

        AwsSnsMetricEmitter emitter = new AwsSnsMetricEmitter("arn:topic", "us-east-1");
        MetricEvent event = new TestMetricEvent("type", "id", System.currentTimeMillis(), 1);
        EmitResult result = emitter.emit(event).join();

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Network error"));
    }
}
