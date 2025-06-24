package com.tempest.aggregation.model;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

public class ConsistentHashRing {
    private static final int DEFAULT_REPLICAS = 100; // TODO: configurable
    private static final String CONJ = "-replica-";

    private final int replicas;
    private final TreeMap<Long, String> ring = new TreeMap<>();

    public ConsistentHashRing(Collection<String> initialNodes) {
        this(initialNodes, DEFAULT_REPLICAS);
    }

    public ConsistentHashRing(Collection<String> initialNodes, int replicas) {
        this.replicas = replicas;
        setNodes(initialNodes);
    }

    public synchronized void setNodes(Collection<String> newNodes) {
        ring.clear();
        for (String node : newNodes) {
            addNode(node);
        }
    }

    public synchronized void addNode(String node) {
        for (int i = 0; i < replicas; i++) {
            String virtualNode = node + CONJ + i;
            ring.put(hash(virtualNode), node);
        }
    }

    public synchronized void removeNode(String node) {
        for (int i = 0; i < replicas; i++) {
            String virtualNode = node + CONJ + i;
            ring.remove(hash(virtualNode));
        }
    }

    public synchronized String getNode(String key) {
        if (ring.isEmpty()) return null;
        long hash = hash(key);
        Map.Entry<Long, String> entry = ring.ceilingEntry(hash);
        return entry != null ? entry.getValue() : ring.firstEntry().getValue();
    }

    private long hash(String key) {
        final String ALGORITHM = "SHA-256";
        final String ERROR_PREFIX = "Hash error";

        try {
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            byte[] digest = md.digest(key.getBytes(StandardCharsets.UTF_8));
            return ByteBuffer.wrap(digest).getLong(); // first 8 bytes
        } catch (Exception e) {
            throw new RuntimeException(ERROR_PREFIX, e);
        }
    }
}

