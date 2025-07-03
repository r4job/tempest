package com.tempest.common;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class SharedScheduledPool {
    private static final ScheduledExecutorService SHARED = Executors.newScheduledThreadPool(
            Runtime.getRuntime().availableProcessors(),
            new NamedThreadFactory("shared-scheduled", true)
    );

    public static ScheduledExecutorService get() {
        return SHARED;
    }
}
