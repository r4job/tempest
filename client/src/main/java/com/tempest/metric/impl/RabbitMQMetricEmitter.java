package com.tempest.metric.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.tempest.metric.EmitResult;
import com.tempest.metric.MetricEmitter;
import com.tempest.metric.MetricEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

public class RabbitMQMetricEmitter implements MetricEmitter {
    private static final Logger logger = LoggerFactory.getLogger(RabbitMQMetricEmitter.class);

    private final Connection connection;
    private final Channel channel;
    private final String exchangeName;
    private final String routingKey;
    private final ObjectMapper mapper = new ObjectMapper();

    public RabbitMQMetricEmitter(String host, int port, String exchangeName, String routingKey) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);

        try {
            this.connection = factory.newConnection();
            this.channel = connection.createChannel();
        } catch (IOException | TimeoutException e) {
            logger.error("[RabbitMQMetricEmitter]", e);
            throw new RuntimeException(e);
        }

        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
    }

    @Override
    public CompletableFuture<EmitResult> emit(MetricEvent event) {
        try {
            String message = mapper.writeValueAsString(event);
            channel.basicPublish(exchangeName, routingKey, null, message.getBytes());
            channel.waitForConfirmsOrDie();  // throws IOException or TimeoutException on failure

        } catch (Exception e) {
            final String format = "[RabbitMQMetricEmitter] RabbitMQ emit failed due to exception: {}";
            logger.error(format, e.getMessage());
            return CompletableFuture.completedFuture(EmitResult.fail(format, e.getMessage()));
        }

        return CompletableFuture.completedFuture(EmitResult.ok());
    }

    public void close() {
        try {
            channel.close();
            connection.close();
        } catch (Exception e) {
            logger.error("[RabbitMQMetricEmitter] Failed to close RabbitMQ resources: {}", e.getMessage());
        }
    }
}
