package com.tempest.aggregation.strategy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MinStrategyTest {

    @Test
    void returnsZeroWhenNoValuesAdded() {
        MinStrategy strategy = new MinStrategy();
        assertEquals(0.0, strategy.aggregate(), 1e-6, "Should return 0.0 when empty");
    }

    @Test
    void returnsSingleValueAsMin() {
        MinStrategy strategy = new MinStrategy();
        strategy.add(42.0);
        assertEquals(42.0, strategy.aggregate(), 1e-6);
    }

    @Test
    void returnsCorrectMinForMultipleValues() {
        MinStrategy strategy = new MinStrategy();
        strategy.add(100.0);
        strategy.add(1.0);
        strategy.add(50.0);
        assertEquals(1.0, strategy.aggregate(), 1e-6);
    }

    @Test
    void handlesNegativeValues() {
        MinStrategy strategy = new MinStrategy();
        strategy.add(-10.0);
        strategy.add(-50.0);
        strategy.add(-5.0);
        assertEquals(-50.0, strategy.aggregate(), 1e-6);
    }

    @Test
    void handlesMixedSignValues() {
        MinStrategy strategy = new MinStrategy();
        strategy.add(0.0);
        strategy.add(-15.0);
        strategy.add(25.5);
        assertEquals(-15.0, strategy.aggregate(), 1e-6);
    }
}
