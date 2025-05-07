package com.tempest.metric.impl;

import com.tempest.grpc.auth.AuthCallCredentials;
import com.tempest.grpc.auth.TimeBasedKeyProvider;
import com.tempest.metric.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class GrpcMetricEmitter implements MetricEmitter {
    private static final Logger logger = LoggerFactory.getLogger(GrpcMetricEmitter.class);

    private final MetricServiceGrpc.MetricServiceBlockingStub stub;

    public GrpcMetricEmitter(String serverHost, int serverPort, String secretSeed, long rotationIntervalMillis, long tokenTTLMillis) {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(serverHost, serverPort)
                .useTransportSecurity()
                .build();
        TimeBasedKeyProvider provider = new TimeBasedKeyProvider(secretSeed, rotationIntervalMillis);
        stub = MetricServiceGrpc
                .newBlockingStub(channel)
                .withCallCredentials(new AuthCallCredentials(provider, tokenTTLMillis));
    }

    @Override
    public CompletableFuture<EmitResult> emit(MetricEvent event) {
        try {
            ReportAck ack = stub.report(event);
            if (!ack.getSuccess()) {
                final String format = "[GrpcMetricEmitter] Failed to emit: {}";
                logger.error(format, ack.getMessage());
                return CompletableFuture.completedFuture(EmitResult.fail(format, ack.getMessage()));
            }
        } catch (Exception e) {
            final String format = "[GrpcMetricEmitter] gRPC error: {}";
            logger.error(format, e.getMessage());
            return CompletableFuture.completedFuture(EmitResult.fail(format, e.getMessage()));
        }

        return CompletableFuture.completedFuture(EmitResult.ok());
    }
}
