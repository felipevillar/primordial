package com.villarsolutions.primordial.util;


import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.List;
import java.util.stream.Collectors;

public class PrimordialUtil {

    /**
     * The exact maximum length of arrays is VM dependent, so we choose (Integer.MAX_VALUE - 8)
     * as a conservative number (some HotSpot VMs go up to Integer.MAX_VALUE - 5)
     * <p>
     * For reference, see the private <code>MAX_ARRAY_SIZE</code> field of <code>java.util.ArrayList</code>
     * <p>
     * Also note that even if one tries to allocate an array of length below (Integer.MAX_VALUE - 8), the JVM
     * will still throw OutOfMemoryError if there is no available heap to allocate the array.
     * <p>
     * The available heap required to allocate a boolean array of length Integer.MAX_VALUE is 2GB, so in
     * order to run certain calculators for large ceiling values, the process must be started with a large heap,
     * e.g. using the JVM argument -Xmx4G at startup.
     *
     * @see java.util.ArrayList
     */
    public static final BigInteger MAX_ARRAY_LENGTH = BigInteger.valueOf(Integer.MAX_VALUE - 8);


    public static final BigInteger TWO = BigInteger.valueOf(2);

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

    public static BigInteger bigInt(long l) {
        return BigInteger.valueOf(l);
    }

    public static boolean isOdd(BigInteger i) {
        return i.testBit(0);
    }

    public static boolean isEven(BigInteger i) {
        return !isOdd(i);
    }

    public static List<BigInteger> toBigInts(List<Integer> numbers) {
        return numbers.stream().map(BigInteger::valueOf).collect(Collectors.toList());
    }

}
