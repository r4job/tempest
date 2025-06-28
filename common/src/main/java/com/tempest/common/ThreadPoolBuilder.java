package com.tempest.common;

import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class ThreadPoolBuilder {
    private static final int CORES = Runtime.getRuntime().availableProcessors();
    private static final int THREAD_LIMIT = CORES * 2;
    private static final AtomicInteger totalThreads = new AtomicInteger(0);
    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolBuilder.class);

    private static final ExecutorService GENERAL_POOL = ThreadPoolBuilder.newBuilder()
            .named("shared")
            .withCoreThreads(CORES)
            .withMaxThreads(THREAD_LIMIT)
            .withQueueCapacity(1000)
            .withKeepAlive(60_000)
            .build();

    private String name = "tempest-";
    private int corePoolSize = 1;
    private int maxPoolSize = corePoolSize;
    private long keepAliveMillis = 0;
    private int queueCapacity = 1000;
    private boolean isDaemon = true;
    private RejectedExecutionHandler rejectedHandler = new ThreadPoolExecutor.AbortPolicy();

    public static ExecutorService general() {
        return GENERAL_POOL;
    }

    public static ThreadPoolBuilder newBuilder() {
        return new ThreadPoolBuilder();
    }

    public ThreadPoolBuilder named(String tag) {
        this.name += tag;
        return this;
    }

    public ThreadPoolBuilder withCoreThreads(int core) {
        this.corePoolSize = core;
        return this;
    }

    public ThreadPoolBuilder withMaxThreads(int max) {
        this.maxPoolSize = max;
        return this;
    }

    public ThreadPoolBuilder withKeepAlive(long millis) {
        this.keepAliveMillis = millis;
        return this;
    }

    public ThreadPoolBuilder withQueueCapacity(int capacity) {
        this.queueCapacity = capacity;
        return this;
    }

    public ThreadPoolBuilder asDaemon(boolean daemon) {
        this.isDaemon = daemon;
        return this;
    }

    public ThreadPoolBuilder withRejectionPolicy(RejectedExecutionHandler handler) {
        this.rejectedHandler = handler;
        return this;
    }

    public ExecutorService build() {
        int requested = this.maxPoolSize;
        int current = totalThreads.addAndGet(requested);

        if (current > THREAD_LIMIT) {
            logger.warn("[TempestThreadPool] Thread creation exceeds safe limit: {} > {}. "
                    + "Reusing general pool.", current, THREAD_LIMIT);
            return general();
        }

        BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(queueCapacity);
        ThreadFactory factory = new NamedThreadFactory(name, isDaemon);
        return new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveMillis, TimeUnit.MILLISECONDS, queue, factory, rejectedHandler);
    }

    public ScheduledExecutorService buildScheduled() {
        return Executors.newScheduledThreadPool(corePoolSize, new NamedThreadFactory(name, isDaemon));
    }
}

