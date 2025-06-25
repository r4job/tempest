package com.tempest.aggregation.watcher;

import com.tempest.aggregation.model.ConsistentHashRing;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.options.WatchOption;
import io.etcd.jetcd.watch.WatchEvent;
import io.etcd.jetcd.watch.WatchResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class EtcdHashRingWatcher {
    private static final Logger logger = LoggerFactory.getLogger(EtcdHashRingWatcher.class);
    private static final int MAX_RETRIES = 5; // TODO: configurable

    private final Client client;
    private final String keyPrefix;
    private final ConsistentHashRing ring;
    private final Set<String> currentNodes = new HashSet<>();
    private final ScheduledExecutorService retryExecutor = Executors.newSingleThreadScheduledExecutor();

    private int retryCount = 0;

    public EtcdHashRingWatcher(Client client, String keyPrefix, ConsistentHashRing ring) {
        this.client = client;
        this.keyPrefix = keyPrefix;
        this.ring = ring;
    }

    public void start() {
        ByteSequence prefix = ByteSequence.from(keyPrefix, StandardCharsets.UTF_8);
        listenerWithRetry(prefix);
    }

    private void listenerWithRetry(ByteSequence prefix) {
        client.getWatchClient().watch(prefix,
                WatchOption.builder().isPrefix(true).build(),
                new Watch.Listener() {
                    @Override
                    public void onNext(WatchResponse response) {
                        for (WatchEvent event : response.getEvents()) {
                            String nodeName = event.getKeyValue().getKey().toString(StandardCharsets.UTF_8)
                                    .replaceFirst(keyPrefix, "");
                            switch (event.getEventType()) {
                                case PUT -> currentNodes.add(nodeName);
                                case DELETE -> currentNodes.remove(nodeName);
                            }
                        }
                        ring.setNodes(currentNodes);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        logger.error("[EtcdHashRingWatcher] Watch error: {}", throwable.getMessage(), throwable);
                        if (retryCount < MAX_RETRIES) {
                            long backoffMillis = (long) Math.pow(2, retryCount) * 1000;
                            retryCount++;
                            logger.info("[EtcdHashRingWatcher] Retrying watch in {}ms (attempt {})", backoffMillis, retryCount);
                            retryExecutor.schedule(() -> listenerWithRetry(prefix), backoffMillis, TimeUnit.MILLISECONDS);
                        } else {
                            logger.error("[EtcdHashRingWatcher] Max retries reached. Giving up on etcd watch.");
                        }
                    }

                    @Override
                    public void onCompleted() {
                        logger.info("[EtcdHashRingWatcher] Watch stream completed");
                    }
                }
        );
    }
}
