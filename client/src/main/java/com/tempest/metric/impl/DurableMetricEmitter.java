package com.tempest.metric.impl;

import com.tempest.common.ThreadPoolBuilder;
import com.tempest.metric.EmitResult;
import com.tempest.metric.MetricEmitter;
import com.tempest.metric.MetricEvent;
import com.tempest.metric.durability.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.*;

public class DurableMetricEmitter implements MetricEmitter {
    private static final Logger logger = LoggerFactory.getLogger(DurableMetricEmitter.class);

    private final MetricEmitter delegate;
    private final MetricDurabilityStore store;
    private final ExecutorService retryExecutor;
    private final ScheduledExecutorService recoveryScheduler;

    // TODO: configurable
    private final int recoveryBatchSize = 10;
    private final int recoveryThreadCount = 2;
    private final long recoveryIntervalMs = 2000;

    public DurableMetricEmitter(MetricEmitter delegate, MetricDurabilityStore store) {
        this.delegate = delegate;
        this.store = store;
        this.retryExecutor = ThreadPoolBuilder.newBuilder().withCoreThreads(recoveryThreadCount).build();
        this.recoveryScheduler = ThreadPoolBuilder.newBuilder().buildScheduled();

        startRecoveryScheduler();
    }

    @Override
    public CompletableFuture<EmitResult> emit(MetricEvent event) {
        CompletableFuture<EmitResult> future = new CompletableFuture<>();

        delegate.emit(event).whenComplete((res, ex) -> {
            if (ex != null || !res.isSuccess()) {
                logger.warn("[DurableMetricEmitter] Emit failed, persisting: {}", ex != null ? ex.getMessage() : res.getMessage());
                try {
                    store.append(event);
                    future.complete(EmitResult.fail("Persisted to disk due to emit failure"));
                } catch (Exception e2) {
                    logger.error("[DurableMetricEmitter] Disk persist failed: {}", e2.getMessage());
                    future.complete(EmitResult.fail("Emit & persist both failed: " + e2.getMessage()));
                }
            } else {
                future.complete(res);
            }
        });

        return future;
    }

    private void startRecoveryScheduler() {
        recoveryScheduler.scheduleAtFixedRate(() -> {
            List<MetricEvent> batch = store.readNextBatch(recoveryBatchSize);
            if (batch.isEmpty()) {
                return;
            }

            for (MetricEvent event : batch) {
                retryExecutor.submit(() -> {
                    delegate.emit(event).whenComplete((res, ex) -> {
                        if (ex != null || !res.isSuccess()) {
                            logger.warn("[MetricRecovery] Retry failed for event {}: {}", event.getItemId(), ex != null ? ex.getMessage() : res.getMessage());
                        } else {
                            logger.info("[MetricRecovery] Successfully re-emitted: {}", event.getItemId());
                        }
                    });
                });
            }
        }, recoveryIntervalMs, recoveryIntervalMs, TimeUnit.MILLISECONDS);
    }

    public void shutdown() {
        retryExecutor.shutdown();
        recoveryScheduler.shutdown();
        try {
            store.close();
        } catch (Exception e) {
            logger.error("[DurableMetricEmitter] Failed to close durable writer: {}", e.getMessage());
        }
    }
}