package com.tempest.aggregation.watcher;


import com.tempest.aggregation.model.ConsistentHashRing;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.options.WatchOption;
import io.etcd.jetcd.watch.WatchEvent;
import io.etcd.jetcd.watch.WatchResponse;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class EtcdHashRingWatcher {

    private final Client client;
    private final String keyPrefix;
    private final ConsistentHashRing ring;
    private final Set<String> currentNodes = new HashSet<>();

    public EtcdHashRingWatcher(Client client, String keyPrefix, ConsistentHashRing ring) {
        this.client = client;
        this.keyPrefix = keyPrefix;
        this.ring = ring;
    }

    public void start() {
        Watch.Listener listener = new Watch.Listener() {
            @Override
            public void onNext(WatchResponse response) {
                for (WatchEvent event : response.getEvents()) {
                    String nodeName = event.getKeyValue().getKey().toString(StandardCharsets.UTF_8)
                            .replaceFirst(keyPrefix, "");
                    switch (event.getEventType()) {
                        case PUT:
                            currentNodes.add(nodeName);
                            break;
                        case DELETE:
                            currentNodes.remove(nodeName);
                            break;
                    }
                }
                ring.setNodes(currentNodes);
            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace(); // TODO: use retry logic
            }

            @Override
            public void onCompleted() {}
        };

        ByteSequence prefix = ByteSequence.from(keyPrefix, StandardCharsets.UTF_8);
        client.getWatchClient().watch(prefix, WatchOption.builder().isPrefix(true).build(), listener);
    }
}
