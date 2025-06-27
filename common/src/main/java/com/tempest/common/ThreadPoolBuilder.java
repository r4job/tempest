package com.tempest.common;

import java.util.concurrent.*;

public final class ThreadPoolBuilder {
    private String name = "tempest-";
    private int corePoolSize = 1;
    private int maxPoolSize = corePoolSize;
    private long keepAliveMillis = 0;
    private int queueCapacity = 1000;
    private boolean isDaemon = true;
    private RejectedExecutionHandler rejectedHandler = new ThreadPoolExecutor.AbortPolicy();

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
        BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(queueCapacity);
        ThreadFactory factory = new NamedThreadFactory(name, isDaemon);
        return new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveMillis, TimeUnit.MILLISECONDS, queue, factory, rejectedHandler);
    }

    public ScheduledExecutorService buildScheduled() {
        return Executors.newScheduledThreadPool(corePoolSize, new NamedThreadFactory(name, isDaemon));
    }
}

