package com.tempest.metric.impl;

import com.tempest.metric.MetricEmitter;
import com.tempest.metric.pojo.MetricEvent;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class CsvMetricEmitter implements MetricEmitter {
    private static final String FORMAT_STRING = "%s,%s,%d,%d%n";
    private static final String ERROR_MESSAGE_PREFIX = "Failed to write metric: ";
    private final String filePath;

    public CsvMetricEmitter(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public void emit(MetricEvent event) {
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filePath, true)))) {
            out.printf(FORMAT_STRING, event.getObjectType(), event.getItemId(), event.getTimestamp(), event.getCount());
        } catch (IOException e) {
            System.err.println(ERROR_MESSAGE_PREFIX + e.getMessage());
        }
    }
}
