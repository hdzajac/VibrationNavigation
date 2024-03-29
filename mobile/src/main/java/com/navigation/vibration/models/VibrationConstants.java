package com.navigation.vibration.models;

import com.navigation.vibration.PredefinedPatterns;

public class VibrationConstants {

    //Pattern constants
    public static final byte SHORT_CONTINUOUS_TAG= 1;
    public static final byte LONG_CONTINUOUS_TAG= 2;
    public static final byte HIGH_PERIODIC_TAG= 3;
    public static final byte LOW_PERIODIC_TAG= 4;
    public static final byte APPLE_LEFT_TAG= 5;
    public static final byte APPLE_RIGHT_TAG= 6;
    public static final byte NONE_TAG= 7;

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

    public static VibrationPattern getVibrationPattern(int id)
    {
        switch (id) {
            case TWO_DEVICES_1:
                return TwoDevices1;
            case TWO_DEVICES_2:
                return TwoDevices2;
            case ONE_DEVICE_1:
                return OneDevice1;
            default:
                return null;
         }
    }

    public static byte pickVibrationTag(long[] pattern){
        if(pattern == PredefinedPatterns.HIGH_PERIODIC)
            return HIGH_PERIODIC_TAG;
        else if (pattern == PredefinedPatterns.LOW_PERIODIC)
            return LOW_PERIODIC_TAG;
        else if (pattern == PredefinedPatterns.LONG_CONTINUOUS)
            return LONG_CONTINUOUS_TAG;
        else if (pattern == PredefinedPatterns.SHORT_CONTINUOUS)
            return SHORT_CONTINUOUS_TAG;
        else if (pattern == PredefinedPatterns.LEFT_APPLE)
            return APPLE_LEFT_TAG;
        else if (pattern == PredefinedPatterns.RIGHT_APPLE)
            return APPLE_RIGHT_TAG;
        else if (pattern == PredefinedPatterns.NONE)
            return NONE_TAG;
        return -1;
    }

}
