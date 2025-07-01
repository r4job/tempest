package com.tempest.common;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ThreadBudgetManagerTest {

    @AfterEach
    public void resetThreadCount() {
        int reserved = ThreadBudgetManager.currentReserved();
        ThreadBudgetManager.release(reserved);
    }

    @Test
    public void testTryReserveWithinLimit() {
        int max = ThreadBudgetManager.maxThreads();
        boolean success = ThreadBudgetManager.tryReserve(max);
        assertTrue(success);
        assertEquals(max, ThreadBudgetManager.currentReserved());
    }

    @Test
    public void testTryReserveExceedingLimit() {
        int max = ThreadBudgetManager.maxThreads();
        assertTrue(ThreadBudgetManager.tryReserve(max));
        assertFalse(ThreadBudgetManager.tryReserve(1), "Should fail to reserve over the limit");
    }

    @Test
    public void testReleaseDecreasesCount() {
        assertTrue(ThreadBudgetManager.tryReserve(5));
        ThreadBudgetManager.release(2);
        assertEquals(3, ThreadBudgetManager.currentReserved());
    }

    @Test
    public void testReleaseNegativeOrZeroHasNoEffect() {
        int initial = ThreadBudgetManager.currentReserved();
        ThreadBudgetManager.release(0);
        ThreadBudgetManager.release(-5);
        assertEquals(initial, ThreadBudgetManager.currentReserved());
    }

    @Test
    public void testRemainingCalculation() {
        ThreadBudgetManager.tryReserve(3);
        int expectedRemaining = ThreadBudgetManager.maxThreads() - 3;
        assertEquals(expectedRemaining, ThreadBudgetManager.remaining());
    }

    @Test
    public void testReserveZeroAlwaysSucceeds() {
        assertTrue(ThreadBudgetManager.tryReserve(0));
    }
}
