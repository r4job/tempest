package com.tempest.aggregation.watcher;

import com.tempest.aggregation.model.ConsistentHashRing;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ZooKeeperHashRingWatcher extends AbstractHashRingWatcher {
    private static final Logger logger = LoggerFactory.getLogger(ZooKeeperHashRingWatcher.class);

    private final CuratorFramework client;
    private final String path;
    private CuratorCache cache;
    private final ExecutorService listenerExecutor = Executors.newSingleThreadExecutor();

    public ZooKeeperHashRingWatcher(CuratorFramework client, String path, ConsistentHashRing ring, int maxRetries) {
        super(ring, maxRetries);
        this.client = client;
        this.path = path;
    }

    @Override
    public void start() {
        setupWatcherWithRetry();
    }

    private void setupWatcherWithRetry() {
        try {
            cache = CuratorCache.build(client, path);
            CuratorCacheListener listener = CuratorCacheListener.builder().forPathChildrenCache(
                    path, client, (client, event) -> updateNodes()).build();
            cache.listenable().addListener(listener, listenerExecutor);
            cache.start();
            updateNodes(); // initial sync
            logger.info("ZooKeeperHashRingWatcher started watching path: {}", path);
        } catch (Exception e) {
            logger.error("Failed to start ZooKeeper watcher: {}", e.getMessage(), e);
            retry(this::setupWatcherWithRetry);
        }
    }

    private void updateNodes() {
        try {
            List<String> children = client.getChildren().forPath(path);
            Set<String> nodes = new HashSet<>(children);
            currentNodes.clear();
            currentNodes.addAll(nodes);
            ring.setNodes(currentNodes);
            logger.info("Updated nodes from ZooKeeper: {}", currentNodes);
        } catch (Exception e) {
            logger.error("Failed to update nodes from ZooKeeper: {}", e.getMessage(), e);
        }
    }

    @Override
    public void stop() {
        super.stop();
        try {
            if (cache != null) {
                cache.close();
            }
        } catch (Exception e) {
            logger.warn("Failed to close PathChildrenCache", e);
        }
        listenerExecutor.shutdownNow();
    }
}
