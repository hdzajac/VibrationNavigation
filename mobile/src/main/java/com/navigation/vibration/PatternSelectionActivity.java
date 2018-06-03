package com.navigation.vibration;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.navigation.vibration.adaptors.VibrationPatternAdaptor;
import com.navigation.vibration.models.VibrationConstants;
import com.navigation.vibration.models.VibrationPattern;

import java.util.ArrayList;

public class PatternSelectionActivity extends Activity {

    public static final String VIBRATION_POSITION = "chosen_vibration";
    private ArrayList<VibrationPattern> vibrationPatterns;

    //choose vibrations by list position
    private int chosenVibrationID =1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pattern_selection);


        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String message = intent.getStringExtra(NoDevicesSelectionActivity.NO_DEVICES);
        int noDevices = 2;
        try {
            noDevices = Integer.parseInt(message);
        }
        catch (Exception e)
        { }

        ArrayAdapter adapter = new ArrayAdapter<String>(this,
                R.layout.list_activity);

        ListView listView =  findViewById(R.id.pattern_list);
        listView.setAdapter(adapter);


        vibrationPatterns = initList(noDevices);
        adapter   = new VibrationPatternAdaptor(this, vibrationPatterns);

        listView =findViewById(R.id.pattern_list);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {

                vibrationPreview(position);
            }
        });

    }


    private void vibrationPreview(int position) {
        VibrationPattern selectedPattern = vibrationPatterns.get(position);
        chosenVibrationID = selectedPattern.getId();

        // actual vibration thingy here....

        Vibrator mVibrator  = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        mVibrator.vibrate(selectedPattern.getPatternAhead(), -1);

    }

    public void goToNextActivity(View view)
    {
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra(VIBRATION_POSITION, chosenVibrationID);
        startActivity(intent);
    }

    private ArrayList<VibrationPattern>  initList(int noDevices)
    {
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

            VibrationPattern pattern3 = new VibrationPattern("LONG_CONTINUOUS",  VibrationConstants.LONG_CONTINUOUS_2,
                PredefinedPatterns.LONG_CONTINUOUS,
                PredefinedPatterns.LONG_CONTINUOUS,
                PredefinedPatterns.LONG_CONTINUOUS,
                PredefinedPatterns.LONG_CONTINUOUS);

        VibrationPattern pattern4 = new VibrationPattern("SHORT_CONTINUOUS",  VibrationConstants.SHORT_CONTINUOUS_2,
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
        }
        else{
            list.add(pattern1_2);
            list.add(pattern2_2);
      }
        return list;
    }
}
