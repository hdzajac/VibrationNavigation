package com.navigation.vibration.models;

import android.arch.lifecycle.ViewModel;
import android.content.ClipData;

import com.here.android.mpa.common.GeoCoordinate;

public class SharedViewModel extends ViewModel {
    private GeoCoordinate targetLocation ;

    public SharedViewModel() {
        targetLocation = null;
    }

    public void select(GeoCoordinate item) {
        targetLocation = item;
    }

    public GeoCoordinate getSelected() {
        return targetLocation;
    }
}