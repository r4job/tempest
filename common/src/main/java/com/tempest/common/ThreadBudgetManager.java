package com.tempest.common;

import java.util.concurrent.atomic.AtomicInteger;

public class ThreadBudgetManager {
    private static final int MAX_THREADS = Runtime.getRuntime().availableProcessors() * 2;
    private static final AtomicInteger currentThreads = new AtomicInteger(0);

    public static boolean tryReserve(int count) {
        if (count <= 0) return true;

        while (true) {
            int current = currentThreads.get();
            int updated = current + count;

            if (updated > MAX_THREADS) {
                return false;
            }
            if (currentThreads.compareAndSet(current, updated)) {
                return true;
            }
        }
    }

    public static void release(int count) {
        if (count <= 0) return;
        currentThreads.addAndGet(-count);
    }

    public static int maxThreads() {
        return MAX_THREADS;
    }

    public static int currentReserved() {
        return currentThreads.get();
    }

    public static int remaining() {
        return MAX_THREADS - currentThreads.get();
    }
}
