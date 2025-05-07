package com.tempest.metric.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tempest.metric.EmitResult;
import com.tempest.metric.MetricEmitter;
import com.tempest.metric.MetricEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

import java.util.concurrent.CompletableFuture;

public class AwsSnsMetricEmitter implements MetricEmitter {

    private static final Logger logger = LoggerFactory.getLogger(AwsSnsMetricEmitter.class);

    private final SnsClient snsClient;
    private final String topicArn;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AwsSnsMetricEmitter(String topicArn, String region) {
        this.snsClient = SnsClient.builder()
                .region(Region.of(region))
                .build();
        this.topicArn = topicArn;
    }

    @Override
    public CompletableFuture<EmitResult> emit(MetricEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            PublishRequest request = PublishRequest.builder()
                    .topicArn(topicArn)
                    .message(message)
                    .build();
            PublishResponse response = snsClient.publish(request);

            if (!response.sdkHttpResponse().isSuccessful()) {
                return CompletableFuture.completedFuture(
                        EmitResult.fail("[AwsSnsMetricEmitter] Failed to publish to SNS: {}",
                        response.sdkHttpResponse().statusText().orElse("Unknown error")));
            }
        } catch (Exception e) {
            final String format = "[AwsSnsMetricEmitter] Failed to emit event: {}";
            final String errorMessage = e.getMessage();
            logger.error(format, errorMessage);
            return CompletableFuture.completedFuture(EmitResult.fail(format, errorMessage));
        }

        return CompletableFuture.completedFuture(EmitResult.ok());
    }

    public void shutdown() {
        if (snsClient != null) {
            snsClient.close();
        }
    }
}