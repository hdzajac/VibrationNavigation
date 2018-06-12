package com.example.handphone.models;

public class VibrationConstants {

    //Pattern constants
    public static final byte SHORT_CONTINUOUS_TAG= 1;
    public static final byte LONG_CONTINUOUS_TAG= 2;
    public static final byte HIGH_PERIODIC_TAG= 3;
    public static final byte LOW_PERIODIC_TAG= 4;
    public static final byte APPLE_LEFT_TAG= 5;
    public static final byte APPLE_RIGHT_TAG= 6;

    //Position constants
    public static final byte LEFT = 1;
    public static final byte RIGHT = 2;
    public static final byte AHEAD =3;
    public static final byte BACK =4;

    public static final String POSITION = "position";

    // the ids of the patterns
    public static final byte TWO_DEVICES_1= 1;
    public static final byte TWO_DEVICES_2= 2;
    public static final byte ONE_DEVICE_1= 3;


    // The actual patters - see the doc file for more info
    public static VibrationPattern TwoDevices1 = new VibrationPattern("Pattern 1 for two devices", VibrationConstants.TWO_DEVICES_1,
            PredefinedPatterns.NONE,
            PredefinedPatterns.HIGH_PERIODIC,
            PredefinedPatterns.HIGH_PERIODIC,
            PredefinedPatterns.LONG_CONTINUOUS);

    public static VibrationPattern TwoDevices2 = new VibrationPattern("Pattern 2 for two devices", VibrationConstants.TWO_DEVICES_2,
            PredefinedPatterns.SHORT_CONTINUOUS,
            PredefinedPatterns.LONG_CONTINUOUS,
            PredefinedPatterns.LONG_CONTINUOUS,
            PredefinedPatterns.HIGH_PERIODIC);


    public static VibrationPattern OneDevice1 = new VibrationPattern("Pattern 1 for one device", VibrationConstants.ONE_DEVICE_1,
            PredefinedPatterns.NONE,
            PredefinedPatterns.LEFT_APPLE,
            PredefinedPatterns.RIGHT_APPLE,
            PredefinedPatterns.NONE);



    public static long[] getVibrationType(int id) {
        switch (id) {
            case LONG_CONTINUOUS_TAG:
                return PredefinedPatterns.LONG_CONTINUOUS;
            case SHORT_CONTINUOUS_TAG:
                return PredefinedPatterns.SHORT_CONTINUOUS;
            case LOW_PERIODIC_TAG:
                return PredefinedPatterns.LOW_PERIODIC;
            case HIGH_PERIODIC_TAG:
                return PredefinedPatterns.HIGH_PERIODIC;
            case APPLE_LEFT_TAG:
                return PredefinedPatterns.LEFT_APPLE;
            case APPLE_RIGHT_TAG:
                return PredefinedPatterns.RIGHT_APPLE;
            default:
                return null;
        }
    }
}
