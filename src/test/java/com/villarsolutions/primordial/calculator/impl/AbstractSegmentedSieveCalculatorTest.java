package com.villarsolutions.primordial.calculator.impl;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;

import static com.villarsolutions.primordial.PrimordialFixtures.newSegment;
import static com.villarsolutions.primordial.calculator.impl.AbstractSegmentedSieveCalculator.divideIntoSegments;
import static org.junit.Assert.assertEquals;

public class AbstractSegmentedSieveCalculatorTest {

    /**
     * Min segment size (2) is higher than what the computed segment size will be (4),
     * therefore the segments sizes should be 4.
     */
    @Test
    public void whenMinSegmentSizeIsLow_thenWeUseTheCalculatedSegmentSizeInstead() throws Exception {
        List<Segment> actual = divideIntoSegments(24, 2, Integer.MAX_VALUE, 5, 19);
        // Note the first segment goes from 2 to sqrt(n)
        List<Segment> expected = Lists.newArrayList(
            newSegment(2,3),
            newSegment(5,4),
            newSegment(9,4),
            newSegment(13,4),
            newSegment(17,4),
            newSegment(21,4)
        );
        assertEquals(expected, actual);
    }

    @Test
    public void whenSegmentsAreNotDividedEvenly_thenTheFinalSegmentShouldBeSmaller() throws Exception {
        List<Segment> actual = divideIntoSegments(23, 2, Integer.MAX_VALUE, 5, 19);
        // Final segment is smaller than the others.
        List<Segment> expected = Lists.newArrayList(
            newSegment(2,3),
            newSegment(5,4),
            newSegment(9,4),
            newSegment(13,4),
            newSegment(17,4),
            newSegment(21,3)
        );
        assertEquals(expected, actual);
    }

    /**
     * Verify that the minimum segment size is respected.
     */
    @Test
    public void whenParallelismLevelIsTooHigh_thenSegmentSizeShouldBeConstrained() throws Exception {
        List<Segment> actual = divideIntoSegments(24, 6, Integer.MAX_VALUE, 1000, 19);

        // Note that the min segment size does not apply to the final segment
        List<Segment> expected = Lists.newArrayList(
            newSegment(2,3),
            newSegment(5,6),
            newSegment(11,6),
            newSegment(17,6),
            newSegment(23,2)
        );
        assertEquals(expected, actual);
    }

    /**
     * The first segment should always be "2 to sqrt(n)", but there should only be
     * one more segment, if the minSegmentSize is higher than ceiling.
     */
    @Test
    public void whenMinimumSegmentSizeIsHigherThanCeiling_thenThereShouldOnlyBeTwoSegments() throws Exception {
        List<Segment> actual = divideIntoSegments(24, 1000, Integer.MAX_VALUE, 5, 19);
        List<Segment> expected = Lists.newArrayList(
            newSegment(2,3),
            newSegment(5,20)
        );
        assertEquals(expected, actual);
    }

    @Test
    public void whenCeilingIsBelow20_thenThereIsOnlyOneSegment() throws Exception {
        List<Segment> actual = divideIntoSegments(19, 1000, Integer.MAX_VALUE, 5, 19);
        List<Segment> expected = Lists.newArrayList(
            newSegment(2,18)
        );
        assertEquals(expected, actual);
    }

}