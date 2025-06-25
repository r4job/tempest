package com.tempest.aggregation.watcher;

import com.tempest.aggregation.model.ConsistentHashRing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class AbstractHashRingWatcher implements HashRingWatcher {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected final ConsistentHashRing ring;
    protected final Set<String> currentNodes = ConcurrentHashMap.newKeySet();
    protected final ScheduledExecutorService retryExecutor = Executors.newSingleThreadScheduledExecutor();

    protected int retryCount = 0;
    protected final int maxRetries;

    public AbstractHashRingWatcher(ConsistentHashRing ring, int maxRetries) {
        this.ring = ring;
        this.maxRetries = maxRetries;
    }

    protected void retry(Runnable task) {
        if (retryCount >= maxRetries) {
            logger.error("[{}] Max retry attempts reached. Aborting...", getClass().getSimpleName());
            return;
        }
        long delay = (long) Math.pow(2, retryCount++) * 1000;
        logger.info("[{}] Retrying in {}ms...", getClass().getSimpleName(), delay);
        retryExecutor.schedule(task, delay, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        retryExecutor.shutdownNow();
    }
}
