package com.tempest.aggregation.watcher;

import com.tempest.aggregation.model.ConsistentHashRing;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.GetChildrenBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

public class ZooKeeperHashRingWatcherTest {

    private CuratorFramework client;
    private ConsistentHashRing ring;
    private ZooKeeperHashRingWatcher watcher;

    @BeforeEach
    public void setup() {
        client = mock(CuratorFramework.class);
        ring = mock(ConsistentHashRing.class);
        watcher = new ZooKeeperHashRingWatcher(client, "/test/nodes", ring, 3);
    }

    @Test
    public void testStartAndUpdateNodes() throws Exception {
        List<String> mockNodes = Arrays.asList("node1", "node2");
        GetChildrenBuilder childrenBuilder = mock(GetChildrenBuilder.class);
        when(client.getChildren()).thenReturn(childrenBuilder);
        when(childrenBuilder.forPath("/test/nodes")).thenReturn(mockNodes);

        watcher.start();

        // Let the async executor run
        Thread.sleep(300);

        verify(ring, atLeastOnce()).setNodes(argThat(set ->
                set.containsAll(mockNodes) && set.size() == mockNodes.size()
        ));
    }

    @Test
    public void testStop() {
        watcher.stop();
    }
}
