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

import java.util.Arrays;

public class PatternSelectionActivity extends Activity implements AdapterView.OnItemSelectedListener {

    public static final String VIBRATION_POSITION = "chosen_vibration";
    private static int MOTORS = 2;
    private static VibrationPattern vibrationPattern;
    private static int vibrationPatternId;
    private static final String TAG = "PatternSelection";
    private static Vibrator vibrator;

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
        intent.putExtra(VIBRATION_POSITION, vibrationPatternId);
        intent.putExtra(NoDevicesSelectionActivity.NO_DEVICES,MOTORS);
        startActivity(intent);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        if(MOTORS == 2) {
            vibrationPattern = VibrationConstants.getVibrationPattern(position + 1);
            vibrationPatternId = position + 1;
        }
        else {
            vibrationPattern = VibrationConstants.getVibrationPattern(position + 3);
            vibrationPatternId = position + 3;
        }
        Log.i(TAG, "selected vibrationPattern: " + parent.getItemAtPosition(position));
        Log.i(TAG, "selected vibrationPattern at position: " + (position));
        Log.i(TAG, "selected vibrationPattern vibrationID: " + (position));

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
            vibrate(vibrationPattern.getPatternAhead());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void vibrateBottom(View view) {
        vibrate(vibrationPattern.getPatternBack());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void vibrateLeft(View view) {
        vibrate(vibrationPattern.getPatternLeft());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void vibrateRight(View view) {
        vibrate(vibrationPattern.getPatternRight());
    }


    public void vibrate(long [] vibration){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
            vibrator.vibrate(VibrationEffect.createWaveform(vibration,-1));
        } else{
            vibrator.vibrate(vibration, -1);
        }
    }

}
