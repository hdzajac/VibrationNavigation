package com.navigation.vibration;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


/** READ BEFORE DEVELOPMENT --------------------------------------------------
 *
 * In order for us to not to be confused about development since there will be many people working on the app
 * we will use basic naming conventions.
 *
 * each variable will start with small letter of its type. This will make us work faster and save some time.
 *
 * Example: boolean bIsFinished
 *          String sOutput
 *          Integer iNum
 *
 * does not count for itterators and other more advanced functionalities. If anyone has any other recommendations,
 * feel free to add it here, change it or delete it.
 *
 * https://developer.android.com/reference/org/w3c/dom/Document     - documentation regarding android dev.
 * https://developer.android.com/training/wearables/                - wearables documentation
 *
 */


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
