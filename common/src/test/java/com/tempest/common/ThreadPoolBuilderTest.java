package com.tempest.common;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

public class ThreadPoolBuilderTest {

    private ExecutorService executor;

    @AfterEach
    public void tearDown() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    @Test
    public void testBuildBasicExecutor() throws Exception {
        executor = ThreadPoolBuilder.newBuilder()
                .named("test")
                .withCoreThreads(2)
                .withMaxThreads(4)
                .withQueueCapacity(100)
                .withKeepAlive(1000)
                .build();

        Future<Integer> result = executor.submit(() -> 42);
        assertEquals(42, result.get());
    }

    @Test
    public void testFallbackToSharedPoolWhenExceedingLimit() {
        int excessiveThreads = Runtime.getRuntime().availableProcessors() * 4;

        ExecutorService excessiveExecutor = ThreadPoolBuilder.newBuilder()
                .named("excess")
                .withCoreThreads(excessiveThreads)
                .withMaxThreads(excessiveThreads)
                .build();

        assertSame(ThreadPoolBuilder.general().getClass(), excessiveExecutor.getClass());
    }

    @Test
    public void testScheduledExecutorRunsTask() throws Exception {
        ScheduledExecutorService scheduled = ThreadPoolBuilder.newBuilder()
                .named("scheduled")
                .withCoreThreads(1)
                .buildScheduled();

        Future<String> future = scheduled.schedule(() -> "hello", 100, TimeUnit.MILLISECONDS);
        assertEquals("hello", future.get(1, TimeUnit.SECONDS));

        scheduled.shutdownNow();
    }

    @Test
    public void testShutdownReleasesThreads() {
        int before = ThreadBudgetManager.currentReserved();

        executor = ThreadPoolBuilder.newBuilder()
                .named("release")
                .withCoreThreads(1)
                .withMaxThreads(1)
                .build();

        executor.shutdownNow();
        assertEquals(before, ThreadBudgetManager.currentReserved());
    }
}
