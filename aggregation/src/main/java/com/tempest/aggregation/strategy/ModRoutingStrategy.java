package com.tempest.aggregation.strategy;

import com.tempest.metric.MetricEvent;

public class ModRoutingStrategy implements RoutingStrategy {
    private static final String NODE_PREFIX = "node-";

    private final int numNodes;

    public ModRoutingStrategy(int numNodes) {
        this.numNodes = numNodes;
    }

    @Override
    public String route(MetricEvent event) {
        int index = Math.abs((event.getObjectType() + event.getItemId()).hashCode()) % numNodes;
        return NODE_PREFIX + index;
    }
}
