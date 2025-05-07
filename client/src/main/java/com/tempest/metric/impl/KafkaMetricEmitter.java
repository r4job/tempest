package com.tempest.metric.impl;

import com.tempest.metric.EmitResult;
import com.tempest.metric.MetricEmitter;
import com.tempest.metric.MetricEvent;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.CompletableFuture;

public class KafkaMetricEmitter implements MetricEmitter {
    private static final Logger logger = LoggerFactory.getLogger(KafkaMetricEmitter.class);
    private static final String STRING_SERIALIZER = "org.apache.kafka.common.serialization.StringSerializer";
    private static final String LEADER_ONLY = "1";
    private static final String FORMAT_STRING = "%s,%s,%d,%d";
    private final KafkaProducer<String, String> producer;
    private final String topic;

    public KafkaMetricEmitter(String bootstrapServers, String topic) {
        this.topic = topic;

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.ACKS_CONFIG, LEADER_ONLY);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, STRING_SERIALIZER);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, STRING_SERIALIZER);

        this.producer = new KafkaProducer<>(props);
    }

    @Override
    public CompletableFuture<EmitResult> emit(MetricEvent event) {
        CompletableFuture<EmitResult> resultFuture = new CompletableFuture<>();

        String value = String.format(
                FORMAT_STRING,
                event.getObjectType(),
                event.getItemId(),
                event.getTimestamp(),
                event.getCount()
        );

        ProducerRecord<String, String> record = new ProducerRecord<>(topic, event.getItemId(), value);

        producer.send(record, (metadata, exception) -> {
            if (exception != null) {
                String errorMessage = String.format("Kafka emit failed: topic=%s, key=%s, due to exception: %s",
                        topic, event.getItemId(), exception.getMessage());
                logger.error("[KafkaMetricEmitter] {}", errorMessage);
                resultFuture.complete(EmitResult.fail(errorMessage));
            } else {
                resultFuture.complete(EmitResult.ok());
            }
        });

        return resultFuture;
    }

    public void shutdown() {
        producer.flush();
        producer.close();
    }
}
