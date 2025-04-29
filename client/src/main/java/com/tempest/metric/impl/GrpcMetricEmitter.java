package com.tempest.metric.impl;


import com.tempest.metric.MetricEmitter;
import com.tempest.metric.MetricEvent;
import com.tempest.metric.MetricServiceGrpc;
import com.tempest.metric.ReportAck;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class GrpcMetricEmitter implements MetricEmitter {
    private final MetricServiceGrpc.MetricServiceBlockingStub stub;

    public GrpcMetricEmitter(String serverHost, int serverPort) {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(serverHost, serverPort)
                .usePlaintext()  // Disable TLS for now
                .build();
        stub = MetricServiceGrpc.newBlockingStub(channel);
    }

    @Override
    public void emit(MetricEvent event) {
        try {
            ReportAck ack = stub.report(event);
            if (!ack.getSuccess()) {
                System.err.println("[GrpcMetricEmitter] Failed to emit: " + ack.getMessage());
            }
        } catch (Exception e) {
            System.err.println("[GrpcMetricEmitter] gRPC error: " + e.getMessage());
        }
    }
}
