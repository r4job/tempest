package com.tempest.aggregation.strategy;

import com.tempest.aggregation.TestMetricEvent;
import com.tempest.metric.MetricEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ModRoutingStrategyTest {

    @Test
    public void testRoutesConsistently() {
        RoutingStrategy strategy = new ModRoutingStrategy(4);

        MetricEvent event1 = new TestMetricEvent("video", "itemA", System.currentTimeMillis(), 1);
        MetricEvent event2 = new TestMetricEvent("video", "itemA", System.currentTimeMillis(), 1);

        String node1 = strategy.route(event1);
        String node2 = strategy.route(event2);

        assertEquals(node1, node2, "Same key should route to same node consistently");
    }

    @Test
    public void testRoutesToValidNode() {
        int numNodes = 5;
        RoutingStrategy strategy = new ModRoutingStrategy(numNodes);

        MetricEvent event = new TestMetricEvent("audio", "item42", System.currentTimeMillis(), 1);
        String node = strategy.route(event);

        assertTrue(node.matches("node-\\d+"), "Node name should match pattern node-[0-9]");
        int index = Integer.parseInt(node.split("-")[1]);
        assertTrue(index >= 0 && index < numNodes, "Node index should be within range");
    }

    @Test
    public void testHandlesNegativeHashCodes() {
        RoutingStrategy strategy = new ModRoutingStrategy(3);

        MetricEvent event = new TestMetricEvent("prefix", "\uFFFF\uFFFF\uFFFF", System.currentTimeMillis(), 1);
        String node = strategy.route(event);

        assertNotNull(node);
        assertTrue(node.startsWith("node-"), "Should still resolve a node name");
    }
}
