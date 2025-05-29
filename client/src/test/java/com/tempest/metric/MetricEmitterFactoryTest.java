package com.tempest.metric;

import com.tempest.config.WarmingConfig;
import com.tempest.metric.impl.*;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class MetricEmitterFactoryTest {

    @Test
    void testCreateMemoryEmitter() {
        WarmingConfig.MetricConfig config = new WarmingConfig.MetricConfig();
        config.setBackend("MEMORY");

        MetricEmitter emitter = MetricEmitterFactory.create(config);
        assertInstanceOf(InMemoryMetricEmitter.class, emitter);
    }

    @Test
    void testCreateCsvEmitter() {
        WarmingConfig.MetricConfig config = new WarmingConfig.MetricConfig();
        config.setBackend("CSV");
        config.setFilePath("test-metrics.csv");

        MetricEmitter emitter = MetricEmitterFactory.create(config);
        assertInstanceOf(CsvMetricEmitter.class, emitter);
    }

    @Test
    void testCreateAwsEmitter() {
        WarmingConfig.MetricConfig config = new WarmingConfig.MetricConfig();
        config.setBackend("AWS");

        WarmingConfig.MetricConfig.AwsConfig aws = new WarmingConfig.MetricConfig.AwsConfig();
        aws.setTopicArn("arn:aws:sns:us-west-2:123456789012:MyTopic");
        aws.setRegion("us-west-2");

        config.setAws(aws);

        MetricEmitter emitter = MetricEmitterFactory.create(config);
        assertInstanceOf(AwsSnsMetricEmitter.class, emitter);
    }

    @Test
    void testUnsupportedBackendThrows() {
        WarmingConfig.MetricConfig config = new WarmingConfig.MetricConfig();
        config.setBackend("UNSUPPORTED_BACKEND");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                MetricEmitterFactory.create(config));
        assertTrue(exception.getMessage().contains("Unsupported metric backend"));
    }

    @Test
    void testWithDurabilityAndAsync() {
        WarmingConfig.MetricConfig config = new WarmingConfig.MetricConfig();
        config.setBackend("MEMORY");
        config.setEnableDurability(true);
        config.setEnableAsync(true);
        config.setEnableBatch(false);
        config.setEnableRetry(false);
        config.setDurabilityFile(new File("durability.log"));

        MetricEmitter emitter = MetricEmitterFactory.create(config);
        // It should wrap InMemoryMetricEmitter with DurableMetricEmitter and AsyncMetricEmitter
        assertNotNull(emitter);
    }
}

