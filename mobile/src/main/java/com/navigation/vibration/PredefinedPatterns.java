package com.navigation.vibration;

public class PredefinedPatterns {


    private static int dot = 200;
    private static int dotter = 700;
    private static int dash = 700;
    private static int short_gap = 200;
    private static int medium_gap = 500;
    private static int long_gap = 700;

    public static long[] SHORT_CONTINUOUS = {0, dot};
    public static long[] LONG_CONTINUOUS = {0, dash};
    public static long[] HIGH_PERIODIC =  { 0,  // Start immediately
            dot, short_gap, dot, short_gap, dot,
            dot, short_gap, dot, short_gap, dot,
            dot, short_gap, dot, short_gap, dot
    };
    public static long[] LOW_PERIODIC =
            { 0,  // Start immediately
                    dotter, long_gap, dotter, long_gap, dotter, long_gap,
                    dotter, long_gap, dotter, long_gap, dotter, long_gap,
            };
}

