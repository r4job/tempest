package com.tempest.metric.impl;

import com.tempest.metric.EmitResult;
import com.tempest.metric.MetricEvent;
import com.tempest.metric.TestMetricEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

class CsvMetricEmitterTest {

    private final Path tempFile = Files.createTempFile("metrics", ".csv");

    public CsvMetricEmitterTest() throws Exception {
    }

    @AfterEach
    void cleanUp() throws Exception {
        Files.deleteIfExists(tempFile);
    }

    @Test
    void testEmitWritesToCsv() throws Exception {
        CsvMetricEmitter emitter = new CsvMetricEmitter(tempFile.toString());
        MetricEvent event = new TestMetricEvent("click", "item123", 123456789L, 5);

        CompletableFuture<EmitResult> result = emitter.emit(event);

        assertTrue(result.join().isSuccess());

        try (BufferedReader reader = new BufferedReader(new FileReader(tempFile.toFile()))) {
            String line = reader.readLine();
            assertNotNull(line);
            assertEquals("click,item123,123456789,5", line);
        }
    }

    @Test
    void testEmitHandlesIOException() {
        String invalidPath = "/root/invalid.csv"; // should fail due to permission
        CsvMetricEmitter emitter = new CsvMetricEmitter(invalidPath);
        MetricEvent event = new TestMetricEvent("click", "item123", 123456789L, 5);

        CompletableFuture<EmitResult> result = emitter.emit(event);
        assertTrue(result.join().isSuccess()); // emitter always returns ok
    }
}

