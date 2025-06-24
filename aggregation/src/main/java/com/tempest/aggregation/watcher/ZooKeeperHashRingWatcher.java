package com.tempest.aggregation.watcher;

import com.tempest.aggregation.model.ConsistentHashRing;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class ZooKeeperHashRingWatcher implements Watcher {
    private static final Logger logger = LoggerFactory.getLogger(ZooKeeperHashRingWatcher.class);

    private final ZooKeeper zk;
    private final String watchPath;
    private final ConsistentHashRing hashRing;

    public ZooKeeperHashRingWatcher(String zkConnectString, String path, ConsistentHashRing hashRing) throws IOException, KeeperException, InterruptedException {
        this.zk = new ZooKeeper(zkConnectString, 3000, this);
        this.watchPath = path;
        this.hashRing = hashRing;
        init();
    }

    private void init() throws KeeperException, InterruptedException {
        if (zk.exists(watchPath, false) == null) {
            logger.error("Watch path {} does not exist", watchPath);
            throw new KeeperException.NoNodeException("Path does not exist: " + watchPath);
        }
        refreshNodes();
    }

    private void refreshNodes() throws KeeperException, InterruptedException {
        List<String> children = zk.getChildren(watchPath, true);
        List<String> nodes = new CopyOnWriteArrayList<>(children.stream()
                .map(child -> watchPath + "/" + child)
                .collect(Collectors.toList()));

        logger.info("[ZooKeeperWatcher] Updating nodes in hash ring: {}", nodes);
        hashRing.setNodes(nodes);
    }

    @Override
    public void process(WatchedEvent event) {
        if (event.getType() == Watcher.Event.EventType.NodeChildrenChanged && event.getPath().equals(watchPath)) {
            try {
                refreshNodes();
            } catch (KeeperException | InterruptedException e) {
                logger.error("Failed to refresh nodes from ZooKeeper", e);
            }
        }
    }

    public void close() throws InterruptedException {
        zk.close();
    }
}

