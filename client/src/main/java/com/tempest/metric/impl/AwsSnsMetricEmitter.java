package com.tempest.metric.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tempest.metric.MetricEmitter;
import com.tempest.metric.MetricEvent;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

public class AwsSnsMetricEmitter implements MetricEmitter {

    private final SnsClient snsClient;
    private final String topicArn;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AwsSnsMetricEmitter(String topicArn, Region region) {
        this.snsClient = SnsClient.builder()
                .region(region)
                .build();
        this.topicArn = topicArn;
    }

    @Override
    public void emit(MetricEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            PublishRequest request = PublishRequest.builder()
                    .topicArn(topicArn)
                    .message(message)
                    .build();
            PublishResponse response = snsClient.publish(request);

            if (!response.sdkHttpResponse().isSuccessful()) {
                System.err.println("[AwsSnsMetricEmitter] Failed to publish to SNS: " +
                        response.sdkHttpResponse().statusText().orElse("Unknown error"));
            }
        } catch (Exception e) {
            System.err.println("[AwsSnsMetricEmitter] Failed to emit event: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void shutdown() {
        if (snsClient != null) {
            snsClient.close();
        }
    }
}