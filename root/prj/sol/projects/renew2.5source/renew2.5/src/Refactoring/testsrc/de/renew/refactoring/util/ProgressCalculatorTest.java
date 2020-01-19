package de.renew.refactoring.util;

import static org.junit.Assert.*;
import org.junit.Test;

import de.renew.refactoring.util.ProgressCalculator;


public class ProgressCalculatorTest {
    @Test
    public void testCalculateProgress() {
        assertEquals(100, ProgressCalculator.calculateProgress(1, 1));
        assertEquals(50, ProgressCalculator.calculateProgress(1, 2));
        assertEquals(33, ProgressCalculator.calculateProgress(1, 3));

        // should not divide by 0
        assertEquals(100, ProgressCalculator.calculateProgress(0, 0));
        assertEquals(100, ProgressCalculator.calculateProgress(1, 0));
    }

    @Test
    public void testCombinedProgress() {
        assertEquals(75, ProgressCalculator.combinedProgress(100, 3, 0, 1));
        assertEquals(100, ProgressCalculator.combinedProgress(0, 0, 0, 0));
    }
}