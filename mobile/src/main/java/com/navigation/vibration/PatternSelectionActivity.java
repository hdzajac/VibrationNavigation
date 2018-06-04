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
    private static VibrationPattern pattern;
    private static final String TAG = "PatternSelection";
    private static Vibrator vibrator;

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

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator == null) {
                Log.d(TAG, "API TO LOW, can't vibrate");
            }
    }


    public void goToNextActivity(View view) {
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra(VIBRATION_POSITION, chosenVibrationID);
        startActivity(intent);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.i(TAG, "selected pattern: " + parent.getItemAtPosition(position));
        MODE = position;

        if(MOTORS == 2)
            pattern = VibrationConstants.getVibrationPattern(position);
        else
            pattern = VibrationConstants.getVibrationPattern(position + 4);

        View gridView = findViewById(R.id.pattern_selection_grid_layout);
        if (gridView.getVisibility() != View.VISIBLE)
            gridView.setVisibility(View.VISIBLE);

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // do nothing, wait
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void vibrateTop(View view) {
        vibrator.vibrate(VibrationEffect.createWaveform(pattern.getPatternAhead(),-1));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void vibrateBottom(View view) {
        vibrator.vibrate(VibrationEffect.createWaveform(pattern.getPatternBack(),-1));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void vibrateLeft(View view) {
        vibrator.vibrate(VibrationEffect.createWaveform(pattern.getPatternLeft(),-1));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void vibrateRight(View view) {
        vibrator.vibrate(VibrationEffect.createWaveform(pattern.getPatternAhead(),-1));
    }

}
