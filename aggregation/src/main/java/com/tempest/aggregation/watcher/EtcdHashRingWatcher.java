package com.tempest.aggregation.watcher;

import com.tempest.aggregation.model.ConsistentHashRing;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.options.WatchOption;
import io.etcd.jetcd.watch.WatchEvent;
import io.etcd.jetcd.watch.WatchResponse;

import java.nio.charset.StandardCharsets;

public class EtcdHashRingWatcher extends AbstractHashRingWatcher {
    private final Client etcdClient;
    private final String keyPrefix;

    public EtcdHashRingWatcher(Client etcdClient, String keyPrefix, ConsistentHashRing ring, int maxRetries) {
        super(ring, maxRetries);
        this.etcdClient = etcdClient;
        this.keyPrefix = keyPrefix;
    }

    @Override
    public void start() {
        watchWithRetry();
    }

    private void watchWithRetry() {
        ByteSequence prefix = ByteSequence.from(keyPrefix, StandardCharsets.UTF_8);

        etcdClient.getWatchClient().watch(prefix, WatchOption.builder().isPrefix(true).build(),
                new Watch.Listener() {
                    @Override
                    public void onNext(WatchResponse response) {
                        for (WatchEvent event : response.getEvents()) {
                            String node = event.getKeyValue().getKey().toString(StandardCharsets.UTF_8)
                                    .replaceFirst(keyPrefix, "");
                            switch (event.getEventType()) {
                                case PUT:
                                    currentNodes.add(node);
                                    break;
                                case DELETE:
                                    currentNodes.remove(node);
                                    break;
                            }
                        }
                        ring.setNodes(currentNodes);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        logger.error("Etcd watch failed: {}", throwable.getMessage(), throwable);
                        retry(() -> watchWithRetry());
                    }

                    @Override
                    public void onCompleted() {
                        logger.info("Etcd watch completed.");
                    }
                }
        );
    }
}