package com.tempest.metric.impl;

import com.tempest.metric.MetricEmitter;
import com.tempest.metric.pojo.Metric;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class FileMetricEmitter implements MetricEmitter {
    private final String filePath;

    public FileMetricEmitter(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public void emit(Metric metric) {
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filePath, true)))) {
            out.printf("%s,%d,%d%n", metric.getItemId(), metric.getTimestamp(), metric.getCount());
        } catch (IOException e) {
            System.err.println("Failed to write metric: " + e.getMessage());
        }
    }
}
