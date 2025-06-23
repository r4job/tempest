package com.tempest.aggregation.strategy;

import com.tempest.aggregation.model.ConsistentHashRing;
import com.tempest.metric.MetricEvent;

import java.util.List;

public class ConsistentHashRoutingStrategy implements RoutingStrategy {
    private final ConsistentHashRing ring;

    public ConsistentHashRoutingStrategy(List<String> initialNodes) {
        this.ring = new ConsistentHashRing(initialNodes);
    }

    public void addNode(String nodeId) {
        ring.addNode(nodeId);
    }

    public void removeNode(String nodeId) {
        ring.removeNode(nodeId);
    }

    public void updateNodes(List<String> allNodes) {
        ring.setNodes(allNodes);
    }

    @Override
    public String route(MetricEvent event) {
        String key = event.getObjectType() + ":" + event.getItemId();
        return ring.getNode(key);
    }
}

