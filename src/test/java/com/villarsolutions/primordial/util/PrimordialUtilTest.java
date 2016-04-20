package com.villarsolutions.primordial.util;

import org.junit.Test;

import static com.villarsolutions.primordial.util.PrimordialUtil.isEven;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class PrimordialUtilTest {

    @Test
    public void testIsEven() throws Exception {
        assertFalse(isEven(1));
        assertTrue(isEven(2));
        assertFalse(isEven(89));
        assertTrue(isEven(90));
        assertFalse(isEven(-1));
        assertTrue(isEven(0));
        assertTrue(isEven(-2));
        assertTrue(isEven(-90));
    }

}