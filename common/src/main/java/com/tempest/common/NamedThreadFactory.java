package com.tempest.common;

import java.util.concurrent.ThreadFactory;

public final class NamedThreadFactory implements ThreadFactory {
    private final String prefix;
    private final boolean daemon;
    private int count = 0;

    public NamedThreadFactory(String prefix, boolean daemon) {
        this.prefix = prefix;
        this.daemon = daemon;
    }

    @Override
    public synchronized Thread newThread(Runnable r) {
        Thread t = new Thread(r, prefix + count);
        ++count;
        t.setDaemon(daemon);
        return t;
    }
}
