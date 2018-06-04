package com.example.handphone.models;

public class VibrationConstants {

    //Pattern constants
    //Naming convention: Name-of-pattern_No-devices
    public static final byte SHORT_CONTINUOUS_TAG= 1;
    public static final byte LONG_CONTINUOUS_TAG= 2;
    public static final byte HIGH_PERIODIC_TAG= 3;
    public static final byte LOW_PERIODIC_TAG= 4;

    //Position constants
    public static final byte LEFT = 1;
    public static final byte RIGHT = 2;
    public static final byte AHEAD =3;
    public static final byte BACK =3;

    public static long[] getVibrationType(int id)
    {
        switch (id) {
            case LONG_CONTINUOUS_TAG:
                return PredefinedPatterns.LONG_CONTINUOUS;
            case SHORT_CONTINUOUS_TAG:
                return PredefinedPatterns.SHORT_CONTINUOUS;
            case LOW_PERIODIC_TAG:
                return PredefinedPatterns.LOW_PERIODIC;
            case HIGH_PERIODIC_TAG:
                return PredefinedPatterns.HIGH_PERIODIC;
            default:
                return null;
        }
    }

}
