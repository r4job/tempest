package com.tempest.metric.impl;

import com.tempest.metric.EmitResult;
import com.tempest.metric.MetricEmitter;
import com.tempest.metric.MetricEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;

public class DurableMetricEmitter implements MetricEmitter {
    private static final Logger logger = LoggerFactory.getLogger(DurableMetricEmitter.class);

    private final MetricEmitter delegate;
    private final File durabilityFile;
    private final BlockingQueue<MetricEvent> recoveryQueue;

    public DurableMetricEmitter(MetricEmitter delegate, File file) {
        this.delegate = delegate;
        this.durabilityFile = file;
        this.recoveryQueue = new LinkedBlockingQueue<>(10_000); // TODO: configurable
        loadBufferFromDisk();
        startRecoveryWorker();
    }

    @Override
    public CompletableFuture<EmitResult> emit(MetricEvent event) {
        try {
            delegate.emit(event);
        } catch (Exception e) {
            logger.error("[DurableMetricEmitter] Emit failed, persisting: {}", e.getMessage());
            if (!persist(event)) {
                return CompletableFuture.completedFuture(EmitResult.fail("[DurableMetricEmitter] Disk persist failed"));
            }
        }

        return CompletableFuture.completedFuture(EmitResult.ok());
    }

    private boolean persist(MetricEvent event) {
        try {
            FileOutputStream fos = new FileOutputStream(durabilityFile, true);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(event);
        } catch (IOException e) {
            logger.error("[DurableMetricEmitter] Disk persist failed: {}", e.getMessage());
            return false;
        }
        return true;
    }

    private void loadBufferFromDisk() {
        if (!durabilityFile.exists()) {
            return;
        }

        try {
            FileInputStream fis = new FileInputStream(durabilityFile);
            while (fis.available() > 0) {
                try (ObjectInputStream ois = new ObjectInputStream(fis)) {
                    Object obj = ois.readObject();
                    if (obj instanceof MetricEvent) {
                        recoveryQueue.offer((MetricEvent) obj);
                    }
                } catch (Exception e) {
                    logger.error("[DurableMetricEmitter] Failed to load event from disk: {}", e.getMessage());
                }
            }
        } catch (IOException e) {
            logger.error("[DurableMetricEmitter] Error reading durability file: {}", e.getMessage());
        }
    }

    private void startRecoveryWorker() {
        Thread worker = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    MetricEvent event = recoveryQueue.take();
                    try {
                        delegate.emit(event);
                        // TODO: track success and delete durability file later
                    } catch (Exception e) {
                        logger.error("[DurableMetricEmitter] Retry failed: {}", e.getMessage());
                        recoveryQueue.offer(event);
                        Thread.sleep(500); // avoid tight loop
                    }
                }
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }, "durable-metric-recovery-worker");
        worker.start();
    }
}
