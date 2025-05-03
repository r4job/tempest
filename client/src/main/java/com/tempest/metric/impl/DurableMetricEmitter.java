package com.tempest.metric.impl;

import com.tempest.metric.MetricEmitter;
import com.tempest.metric.MetricEvent;

import java.io.*;

public class DurableMetricEmitter implements MetricEmitter {

    private final MetricEmitter delegate;
    private final File durabilityFile;

    public DurableMetricEmitter(MetricEmitter delegate, File file) {
        this.delegate = delegate;
        this.durabilityFile = file;
        loadBufferFromDisk();
    }

    @Override
    public void emit(MetricEvent event) {
        try {
            delegate.emit(event);
        } catch (Exception e) {
            System.err.println("[DurableEmitter] Emit failed, persisting: " + e.getMessage());
            persist(event);
        }
    }

    private void persist(MetricEvent event) {
        try {
            FileOutputStream fos = new FileOutputStream(durabilityFile, true);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(event);
        } catch (IOException e) {
            System.err.println("[DurableEmitter] Disk persist failed: " + e.getMessage());
        }
    }

    private void loadBufferFromDisk() {
        // TODO: restore from log
    }
}
