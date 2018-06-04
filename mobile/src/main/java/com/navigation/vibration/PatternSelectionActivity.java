package com.navigation.vibration;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.navigation.vibration.models.VibrationConstants;
import com.navigation.vibration.models.VibrationPattern;

import java.util.ArrayList;

public class PatternSelectionActivity extends Activity implements AdapterView.OnItemSelectedListener {

    public static final String VIBRATION_POSITION = "chosen_vibration";
    private static int MOTORS = 2;
    private static int MODE = 2;
    private static final String TAG = "PatternSelection";

    //choose vibrations by list position
    private int chosenVibrationID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pattern_selection);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        MOTORS = intent.getIntExtra(NoDevicesSelectionActivity.NO_DEVICES, 2);

        int optionsArrayRes = MOTORS == 2 ? R.array.pattern_selection_spinner_list_2 : R.array.pattern_selection_spinner_list_1;

        Spinner spinner = findViewById(R.id.pattern_selection_spinner);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                optionsArrayRes, android.R.layout.simple_spinner_item);

        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        // Set this activity as listener
        spinner.setOnItemSelectedListener(this);
    }


    public void goToNextActivity(View view) {
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra(VIBRATION_POSITION, chosenVibrationID);
        startActivity(intent);
    }


    private ArrayList<VibrationPattern> initList(int noDevices) {
        //todo
        //will identify patterns by id uniquely send the id to bluetooth device together with LEFT RIGHT AHEAD ETC

        ArrayList<VibrationPattern> list = new ArrayList<>();

        // Patters for 1 device
        VibrationPattern pattern1 = new VibrationPattern("LOW_PERIODIC", VibrationConstants.LONG_PERIODIC_1,
                PredefinedPatterns.LOW_PERIODIC,
                PredefinedPatterns.LOW_PERIODIC,
                PredefinedPatterns.LOW_PERIODIC,
                PredefinedPatterns.LOW_PERIODIC);

        VibrationPattern pattern2 = new VibrationPattern("HIGH_PERIODIC", VibrationConstants.HIGH_PERIODIC_1,
                PredefinedPatterns.HIGH_PERIODIC,
                PredefinedPatterns.HIGH_PERIODIC,
                PredefinedPatterns.HIGH_PERIODIC,
                PredefinedPatterns.HIGH_PERIODIC);

        VibrationPattern pattern3 = new VibrationPattern("LONG_CONTINUOUS", VibrationConstants.LONG_CONTINUOUS_2,
                PredefinedPatterns.LONG_CONTINUOUS,
                PredefinedPatterns.LONG_CONTINUOUS,
                PredefinedPatterns.LONG_CONTINUOUS,
                PredefinedPatterns.LONG_CONTINUOUS);

        VibrationPattern pattern4 = new VibrationPattern("SHORT_CONTINUOUS", VibrationConstants.SHORT_CONTINUOUS_2,
                PredefinedPatterns.SHORT_CONTINUOUS,
                PredefinedPatterns.SHORT_CONTINUOUS,
                PredefinedPatterns.SHORT_CONTINUOUS,
                PredefinedPatterns.SHORT_CONTINUOUS);

        //Patterns for 2 devices
        VibrationPattern pattern1_2 = new VibrationPattern("LOW_PERIODIC", VibrationConstants.LONG_PERIODIC_2,
                PredefinedPatterns.LOW_PERIODIC,
                PredefinedPatterns.LOW_PERIODIC,
                PredefinedPatterns.LOW_PERIODIC,
                PredefinedPatterns.LOW_PERIODIC);

        VibrationPattern pattern2_2 = new VibrationPattern("HIGH_PERIODIC", VibrationConstants.HIGH_PERIODIC_2,
                PredefinedPatterns.HIGH_PERIODIC,
                PredefinedPatterns.HIGH_PERIODIC,
                PredefinedPatterns.HIGH_PERIODIC,
                PredefinedPatterns.HIGH_PERIODIC);

        if (noDevices == 1) {
            list.add(pattern1);
            list.add(pattern2);
            list.add(pattern3);
            list.add(pattern4);
        } else {
            list.add(pattern1_2);
            list.add(pattern2_2);
        }
        return list;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.i(TAG, "selected pattern: " + parent.getItemAtPosition(position));
        MODE = position;

        View gridView = findViewById(R.id.pattern_selection_grid_layout);
        if (gridView.getVisibility() != View.VISIBLE)
            gridView.setVisibility(View.VISIBLE);

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // do nothing, wait
    }

    public void vibrateTop(View view) {
    }

    public void vibrateBottom(View view) {
    }

    public void vibrateLeft(View view) {
    }

    public void vibrateRight(View view) {
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void vibrate(){
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null) {
            v.vibrate(500);
        }
        else
            Log.d(TAG, "API TO LOW, can't vibrate");
    }
}
