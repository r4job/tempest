package com.tempest.aggregation.strategy;

import com.tempest.metric.MetricEvent;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ConsistentHashRoutingStrategy implements RoutingStrategy {
    private static final int VIRTUAL_NODE_REPLICAS = 100; // TODO: configurable

    private final TreeMap<Long, String> hashRing = new TreeMap<>();
    private final List<String> nodes;

    public ConsistentHashRoutingStrategy(List<String> nodeIds) {
        this.nodes = new ArrayList<>(nodeIds);
        buildHashRing();
    }

    private void buildHashRing() {
        for (String node : nodes) {
            for (int i = 0; i < VIRTUAL_NODE_REPLICAS; i++) {
                String virtualNodeId = node + "-replica-" + i;
                long hash = hash(virtualNodeId);
                hashRing.put(hash, node);
            }
        }
    }

    @Override
    public String route(MetricEvent event) {
        String key = event.getObjectType() + ":" + event.getItemId();
        long hash = hash(key);
        Map.Entry<Long, String> entry = hashRing.ceilingEntry(hash);
        return entry != null ? entry.getValue() : hashRing.firstEntry().getValue();
    }

    private long hash(String key) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(key.getBytes(StandardCharsets.UTF_8));
            return ByteBuffer.wrap(digest).getLong(); // only use first 8 bytes
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash key", e);
        }
    }
}

