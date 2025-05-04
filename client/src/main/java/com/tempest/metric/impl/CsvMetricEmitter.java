package com.tempest.metric.impl;

import com.tempest.metric.MetricEmitter;
import com.tempest.metric.MetricEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class CsvMetricEmitter implements MetricEmitter {
    private static final Logger logger = LoggerFactory.getLogger(CsvMetricEmitter.class);
    private static final String FORMAT_STRING = "%s,%s,%d,%d%n";
    private static final String ERROR_MESSAGE_PREFIX = "[CsvMetricEmitter] Failed to write metric: ";

    private final String filePath;

    public CsvMetricEmitter(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public void emit(MetricEvent event) {
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filePath, true)))) {
            out.printf(FORMAT_STRING, event.getObjectType(), event.getItemId(), event.getTimestamp(), event.getCount());
        } catch (IOException e) {
            logger.error(ERROR_MESSAGE_PREFIX + "{}", e.getMessage());
        }
    }
}
