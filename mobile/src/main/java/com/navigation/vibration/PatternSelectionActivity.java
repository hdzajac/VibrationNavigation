package com.navigation.vibration;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Vibrator;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.navigation.vibration.adaptors.VibrationPatternAdaptor;
import com.navigation.vibration.models.VibrationConstants;
import com.navigation.vibration.models.VibrationPattern;

import java.util.ArrayList;

public class PatternSelectionActivity extends Activity {

    public static final String VIBRATION_POSITION = "chosen_vibration";
    private ArrayList<VibrationPattern> vibrationPatterns;
    private int noDevices = 1;

    private Vibrator mVibrator;

    //choose vibrations by list position
    private int chosenVibrationID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pattern_selection);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String message = intent.getStringExtra(NoDevicesSelectionActivity.NO_DEVICES);
        try {
            noDevices = Integer.parseInt(message);
        } catch (Exception e) {
            Log.e("PatternSelection", "Bad number of devices");
        }

        ListView listView =  findViewById(R.id.pattern_list);
        vibrationPatterns = initList(noDevices);
        VibrationPatternAdaptor adapter   = new VibrationPatternAdaptor(this, vibrationPatterns);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {

                vibrationPreview(position);
            }
        });

    }


    private void vibrationPreview(int position) {
        final VibrationPattern  selectedPattern = vibrationPatterns.get(position);
        Handler handler = new Handler();
        chosenVibrationID = selectedPattern.getId();

        // actual vibration thingy here....

        Vibrator mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
         mVibrator  = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);


    }

    private void patternPreview(TextView ui, long[] pattern)
    {
        ui.setBackgroundColor(getResources().getColor(R.color.button_green));
        mVibrator.vibrate(pattern, -1);
        try {
            Thread.sleep(3000);
        } catch (Exception e) {}
        mVibrator.cancel();
        ui.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
    }


    public void goToNextActivity(View view) {
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra(VIBRATION_POSITION, chosenVibrationID);
        intent.putExtra(NoDevicesSelectionActivity.NO_DEVICES,noDevices);
        startActivity(intent);
    }

    // the list of possible patterns depending on the number of devices
    private ArrayList<VibrationPattern>  initList(int noDevices)
    {
        ArrayList<VibrationPattern> list = new ArrayList<>();

        if (noDevices == 1) {
            list.add(VibrationConstants.OneDevice1);
            list.add(VibrationConstants.OneDevice2);
        }
        else{
            list.add(VibrationConstants.TwoDevices1);
            list.add(VibrationConstants.TwoDevices2);
            list.add(VibrationConstants.TwoDevices3);
      }
        return list;
    }
}
