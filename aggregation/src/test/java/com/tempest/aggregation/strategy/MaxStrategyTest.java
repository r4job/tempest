package com.tempest.aggregation.strategy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MaxStrategyTest {

    @Test
    void returnsZeroWhenNoValuesAdded() {
        MaxStrategy strategy = new MaxStrategy();
        assertEquals(0.0, strategy.aggregate(), 1e-6, "Should return 0.0 when empty");
    }

    @Test
    void returnsSingleValueAsMax() {
        MaxStrategy strategy = new MaxStrategy();
        strategy.add(42.0);
        assertEquals(42.0, strategy.aggregate(), 1e-6);
    }

    @Test
    void returnsCorrectMaxForMultipleValues() {
        MaxStrategy strategy = new MaxStrategy();
        strategy.add(1.0);
        strategy.add(100.0);
        strategy.add(50.0);
        assertEquals(100.0, strategy.aggregate(), 1e-6);
    }

    @Test
    void handlesNegativeValues() {
        MaxStrategy strategy = new MaxStrategy();
        strategy.add(-10.0);
        strategy.add(-5.0);
        strategy.add(-20.0);
        assertEquals(-5.0, strategy.aggregate(), 1e-6);
    }

    @Test
    void handlesMixedSignValues() {
        MaxStrategy strategy = new MaxStrategy();
        strategy.add(-10.0);
        strategy.add(0.0);
        strategy.add(25.5);
        assertEquals(25.5, strategy.aggregate(), 1e-6);
    }
}
