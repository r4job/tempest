package com.tempest.aggregation.strategy;

import com.tempest.aggregation.TestMetricEvent;
import com.tempest.metric.MetricEvent;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConsistentHashRoutingStrategyTest {

    @Test
    public void testRoutesToConsistentNode() {
        List<String> nodes = Arrays.asList("node-A", "node-B", "node-C");
        ConsistentHashRoutingStrategy strategy = new ConsistentHashRoutingStrategy(nodes);

        MetricEvent event1 = new TestMetricEvent("video", "item123", System.currentTimeMillis(), 1);
        MetricEvent event2 = new TestMetricEvent("video", "item123", System.currentTimeMillis(), 1);

        String target1 = strategy.route(event1);
        String target2 = strategy.route(event2);

        assertEquals(target1, target2, "Same key should consistently route to the same node");
    }

    @Test
    public void testDistributesAcrossNodes() {
        List<String> nodes = Arrays.asList("node-A", "node-B", "node-C");
        ConsistentHashRoutingStrategy strategy = new ConsistentHashRoutingStrategy(nodes);

        Set<String> visitedNodes = new HashSet<>();

        for (int i = 0; i < 100; i++) {
            MetricEvent event = new TestMetricEvent("type", "item" + i, System.currentTimeMillis(), 1);
            visitedNodes.add(strategy.route(event));
        }

        assertTrue(visitedNodes.size() > 1, "Should route to multiple different nodes");
    }

    @Test
    public void testFallbackToFirstEntryOnWraparound() {
        List<String> nodes = Collections.singletonList("node-Z");
        ConsistentHashRoutingStrategy strategy = new ConsistentHashRoutingStrategy(nodes);

        MetricEvent event = new TestMetricEvent("a", "b", System.currentTimeMillis(), 1);
        String target = strategy.route(event);

        assertEquals("node-Z", target, "With one node, all keys should route to it");
    }

    @Test
    public void testUpdateNodesReflectsInRouting() {
        List<String> initialNodes = Arrays.asList("node-1", "node-2");
        ConsistentHashRoutingStrategy strategy = new ConsistentHashRoutingStrategy(initialNodes);

        MetricEvent event = new TestMetricEvent("video", "item456", System.currentTimeMillis(), 1);
        String initialTarget = strategy.route(event);

        strategy.addNode("node-3");
        String afterAddition = strategy.route(event);

        strategy.removeNode("node-1");
        String afterRemoval = strategy.route(event);

        assertTrue(Arrays.asList("node-1", "node-2", "node-3").contains(initialTarget));
        assertTrue(Arrays.asList("node-1", "node-2", "node-3").contains(afterAddition));
        assertTrue(Arrays.asList("node-2", "node-3").contains(afterRemoval));
    }

    @Test
    public void testSetNodesResetsRing() {
        List<String> initialNodes = Arrays.asList("old-1", "old-2");
        ConsistentHashRoutingStrategy strategy = new ConsistentHashRoutingStrategy(initialNodes);

        MetricEvent event = new TestMetricEvent("audio", "item789", System.currentTimeMillis(), 1);
        String beforeReset = strategy.route(event);

        strategy.updateNodes(Arrays.asList("new-1", "new-2"));
        String afterReset = strategy.route(event);

        assertTrue(beforeReset.startsWith("old-"));
        assertTrue(afterReset.startsWith("new-"));
    }
}
