package com.tempest.aggregation.strategy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AverageStrategyTest {

    @Test
    void aggregatesToZeroInitially() {
        AverageStrategy strategy = new AverageStrategy();
        assertEquals(0.0, strategy.aggregate(), 1e-6, "Initial aggregate should be 0.0");
    }

    @Test
    void aggregatesSingleValueCorrectly() {
        AverageStrategy strategy = new AverageStrategy();
        strategy.add(5.0);
        assertEquals(5.0, strategy.aggregate(), 1e-6);
    }

    @Test
    void aggregatesMultipleValuesCorrectly() {
        AverageStrategy strategy = new AverageStrategy();
        strategy.add(10.0);
        strategy.add(20.0);
        strategy.add(30.0);
        assertEquals(20.0, strategy.aggregate(), 1e-6);
    }

    @Test
    void handlesNegativeAndPositiveValues() {
        AverageStrategy strategy = new AverageStrategy();
        strategy.add(-10.0);
        strategy.add(10.0);
        assertEquals(0.0, strategy.aggregate(), 1e-6);
    }

    @Test
    void handlesFloatingPointPrecision() {
        AverageStrategy strategy = new AverageStrategy();
        strategy.add(0.1);
        strategy.add(0.2);
        assertEquals(0.15, strategy.aggregate(), 1e-6);
    }
}

