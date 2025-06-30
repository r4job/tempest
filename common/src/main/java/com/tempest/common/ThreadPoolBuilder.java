package com.tempest.common;

import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;

import java.util.Collection;
import java.util.List;
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

        if (!ThreadBudgetManager.tryReserve(requested)) {
            logger.warn("Thread budget exceeded. Falling back to shared pool.");
            return general();
        }

        BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(queueCapacity);
        ThreadFactory factory = new NamedThreadFactory(name, isDaemon);
        ThreadPoolExecutor executor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveMillis, TimeUnit.MILLISECONDS, queue, factory, rejectedHandler);
        return wrapWithRelease(executor, requested);
    }

    public ScheduledExecutorService buildScheduled() {
        int requested = this.corePoolSize;

        if (!ThreadBudgetManager.tryReserve(requested)) {
            logger.warn("Thread budget exceeded. Falling back to shared pool.");
            return (ScheduledExecutorService) general(); // FIXME: cast is safe only if GENERAL_POOL is a ScheduledExecutorService
        }

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(corePoolSize, new NamedThreadFactory(name, isDaemon));
        return wrapScheduledWithRelease(executor, requested);
    }

    private ExecutorService wrapWithRelease(ExecutorService delegate, int reserved) {
        return new ExecutorService() {
            @Override
            public void shutdown() {
                delegate.shutdown();
                ThreadBudgetManager.release(reserved);
            }

            @Override
            public List<Runnable> shutdownNow() {
                List<Runnable> tasks = delegate.shutdownNow();
                ThreadBudgetManager.release(reserved);
                return tasks;
            }

            // Delegate all other methods
            @Override
            public boolean isShutdown() {
                return delegate.isShutdown();
            }

            @Override
            public boolean isTerminated() {
                return delegate.isTerminated();
            }

            @Override
            public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
                return delegate.awaitTermination(timeout, unit);
            }

            @Override
            public <T> Future<T> submit(Callable<T> task) {
                return delegate.submit(task);
            }

            @Override
            public <T> Future<T> submit(Runnable task, T result) {
                return delegate.submit(task, result);
            }

            @Override
            public Future<?> submit(Runnable task) {
                return delegate.submit(task);
            }

            @Override
            public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
                return delegate.invokeAll(tasks);
            }

            @Override
            public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
                    throws InterruptedException {
                return delegate.invokeAll(tasks, timeout, unit);
            }

            @Override
            public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
                return delegate.invokeAny(tasks);
            }

            @Override
            public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
                    throws InterruptedException, ExecutionException, TimeoutException {
                return delegate.invokeAny(tasks, timeout, unit);
            }

            @Override
            public void execute(Runnable command) {
                delegate.execute(command);
            }
        };
    }

    private ScheduledExecutorService wrapScheduledWithRelease(ScheduledExecutorService delegate, int reserved) {
        return new ScheduledExecutorService() {
            @Override
            public void shutdown() {
                delegate.shutdown();
                ThreadBudgetManager.release(reserved);
            }

            @Override
            public List<Runnable> shutdownNow() {
                List<Runnable> tasks = delegate.shutdownNow();
                ThreadBudgetManager.release(reserved);
                return tasks;
            }

            // Delegate all other methods
            @Override
            public boolean isShutdown() {
                return delegate.isShutdown();
            }

            @Override
            public boolean isTerminated() {
                return delegate.isTerminated();
            }

            @Override
            public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
                return delegate.awaitTermination(timeout, unit);
            }

            @Override
            public <T> Future<T> submit(Callable<T> task) {
                return delegate.submit(task);
            }

            @Override
            public <T> Future<T> submit(Runnable task, T result) {
                return delegate.submit(task, result);
            }

            @Override
            public Future<?> submit(Runnable task) {
                return delegate.submit(task);
            }

            @Override
            public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
                return delegate.invokeAll(tasks);
            }

            @Override
            public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
                    throws InterruptedException {
                return delegate.invokeAll(tasks, timeout, unit);
            }

            @Override
            public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
                return delegate.invokeAny(tasks);
            }

            @Override
            public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
                    throws InterruptedException, ExecutionException, TimeoutException {
                return delegate.invokeAny(tasks, timeout, unit);
            }

            @Override
            public void execute(Runnable command) {
                delegate.execute(command);
            }

            @Override
            public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
                return delegate.schedule(command, delay, unit);
            }

            @Override
            public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
                return delegate.schedule(callable, delay, unit);
            }

            @Override
            public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
                return delegate.scheduleAtFixedRate(command, initialDelay, period, unit);
            }

            @Override
            public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
                return delegate.scheduleWithFixedDelay(command, initialDelay, delay, unit);
            }
        };
    }
}

