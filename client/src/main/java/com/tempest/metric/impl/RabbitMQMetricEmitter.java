package com.tempest.metric.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.tempest.metric.MetricEmitter;
import com.tempest.metric.MetricEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RabbitMQMetricEmitter implements MetricEmitter {
    private static final Logger logger = LoggerFactory.getLogger(RabbitMQMetricEmitter.class);

    private final Connection connection;
    private final Channel channel;
    private final String exchangeName;
    private final String routingKey;
    private final ObjectMapper mapper = new ObjectMapper();

    public RabbitMQMetricEmitter(String host, int port, String exchangeName, String routingKey) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        this.connection = factory.newConnection();
        this.channel = connection.createChannel();
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
    }

    @Override
    public void emit(MetricEvent event) {
        try {
            String message = mapper.writeValueAsString(event);
            channel.basicPublish(exchangeName, routingKey, null, message.getBytes());
        } catch (Exception e) {
            logger.error("[RabbitMQMetricEmitter] Failed to emit event: {}", e.getMessage());
        }
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
