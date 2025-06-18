package com.tempest.aggregation.strategy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SumStrategyTest {

    @Test
    void returnsZeroInitially() {
        SumStrategy strategy = new SumStrategy();
        assertEquals(0.0, strategy.aggregate(), 1e-6, "Initial sum should be 0.0");
    }

    @Test
    void returnsSingleValueAsSum() {
        SumStrategy strategy = new SumStrategy();
        strategy.add(42.0);
        assertEquals(42.0, strategy.aggregate(), 1e-6);
    }

    @Test
    void returnsCorrectSumForMultipleValues() {
        SumStrategy strategy = new SumStrategy();
        strategy.add(10.0);
        strategy.add(20.0);
        strategy.add(30.0);
        assertEquals(60.0, strategy.aggregate(), 1e-6);
    }

    @Test
    void handlesNegativeValues() {
        SumStrategy strategy = new SumStrategy();
        strategy.add(-5.0);
        strategy.add(10.0);
        strategy.add(-15.0);
        assertEquals(-10.0, strategy.aggregate(), 1e-6);
    }

    @Test
    void handlesFloatingPointPrecision() {
        SumStrategy strategy = new SumStrategy();
        strategy.add(0.1);
        strategy.add(0.2);
        assertEquals(0.3, strategy.aggregate(), 1e-6);
    }
}
