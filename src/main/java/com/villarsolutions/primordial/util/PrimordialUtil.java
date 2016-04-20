package com.villarsolutions.primordial.util;


import java.text.DecimalFormat;

public class PrimordialUtil {

    /**
     * Wrapping the DecimalFormat in a ThreadLocal to make it thread-safe to use from a static context.
     */
    public static final ThreadLocal<DecimalFormat> DECIMAL_FORMAT = new ThreadLocal<DecimalFormat>() {
        @Override
        protected DecimalFormat initialValue() {
            return new DecimalFormat("#,##0");
        }
    };

    public static DecimalFormat getDecimalFormat() {
        return DECIMAL_FORMAT.get();
    }

    public static boolean isEven(long n) {
        return (n & 1) == 0;
    }

}
