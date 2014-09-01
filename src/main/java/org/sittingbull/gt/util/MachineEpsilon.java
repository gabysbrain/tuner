package org.sittingbull.gt.util;

/*
 * Author: Ahmet Karahan
 */


/**
 * 
 * Compute the machine epsilon for the float and double types, the largest positive 
 * floating-point value that, when added to 1, results in a value equal to 1 due to 
 * roundoff.
 * 
 * Reference: Java Number Cruncher 
 * 			  The Java Programmer's Guide to Numerical Computing (By Ronald Mak)
 * 			  Chapter 3.9: The Machine Epsilon
 * 
 */
public final class MachineEpsilon
{
    private static final float  floatEpsilon;
    private static final double doubleEpsilon;

    static {

        // Loop to compute the float epsilon value.
        float fTemp = 0.5f;
        while (1 + fTemp > 1) fTemp /= 2;
        floatEpsilon = fTemp;

        // Loop to compute the double epsilon value.
        double dTemp = 0.5;
        while (1 + dTemp > 1) dTemp /= 2;
        doubleEpsilon = dTemp;
    };

    /**
     * Return the float epsilon value.
     * @returns the value
     */
    public static float floatValue() { return floatEpsilon; }

    /**
     * Return the double epsilon value.
     * @returns the value
     */
    public static double doubleValue() { return doubleEpsilon; }
}

