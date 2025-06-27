package com.tempest.common;

import java.util.concurrent.ExecutorService;

public class TempestExecutors {
    // TODO: configurable
    private static final ExecutorService GENERAL_POOL = ThreadPoolBuilder.newBuilder()
            .named("general")
            .withCoreThreads(4)
            .withMaxThreads(8)
            .withQueueCapacity(1000)
            .withKeepAlive(60_000)
            .build();

    public static ExecutorService general() {
        return GENERAL_POOL;
    }
}
