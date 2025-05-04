package com.tempest.metric.impl;

import com.tempest.grpc.auth.AuthCallCredentials;
import com.tempest.grpc.auth.TimeBasedKeyProvider;
import com.tempest.metric.MetricEmitter;
import com.tempest.metric.MetricEvent;
import com.tempest.metric.MetricServiceGrpc;
import com.tempest.metric.ReportAck;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public void emit(MetricEvent event) {
        try {
            ReportAck ack = stub.report(event);
            if (!ack.getSuccess()) {
                logger.error("[GrpcMetricEmitter] Failed to emit: {}", ack.getMessage());
            }
        } catch (Exception e) {
            logger.error("[GrpcMetricEmitter] gRPC error: {}", e.getMessage());
        }
    }
}
