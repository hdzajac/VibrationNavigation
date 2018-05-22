package com.navigation.vibration;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.navigation.vibration.adaptors.VibrationPatternAdaptor;
import com.navigation.vibration.models.VibrationPattern;

import java.util.ArrayList;

public class PatternSelectionActivity extends Activity {


    private ArrayList<VibrationPattern> vibrationPatterns;
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
        VibrationPattern selectedPattern= vibrationPatterns.get(position);
        // actual vibration thingy here....

        Vibrator mVibrator  = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        mVibrator.vibrate(selectedPattern.getPatternAhead(), -1);

    }

    public void goToNextActivity(View view)
    {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    private ArrayList<VibrationPattern>  initList(int noDevices)
    {
        ArrayList<VibrationPattern> list = new ArrayList<>();
        VibrationPattern pattern1 = new VibrationPattern("LOW_PERIODIC", 1,
                PredefinedPatterns.LOW_PERIODIC,
                PredefinedPatterns.LOW_PERIODIC,
                PredefinedPatterns.LOW_PERIODIC,
                PredefinedPatterns.LOW_PERIODIC);

        VibrationPattern pattern2 = new VibrationPattern("HIGH_PERIODIC", 1,
                PredefinedPatterns.HIGH_PERIODIC,
                PredefinedPatterns.HIGH_PERIODIC,
                PredefinedPatterns.HIGH_PERIODIC,
                PredefinedPatterns.HIGH_PERIODIC);

        VibrationPattern pattern3 = new VibrationPattern("LONG_CONTINUOUS", 1,
                PredefinedPatterns.LONG_CONTINUOUS,
                PredefinedPatterns.LONG_CONTINUOUS,
                PredefinedPatterns.LONG_CONTINUOUS,
                PredefinedPatterns.LONG_CONTINUOUS);

        VibrationPattern pattern4 = new VibrationPattern("SHORT_CONTINUOUS", 1,
                PredefinedPatterns.SHORT_CONTINUOUS,
                PredefinedPatterns.SHORT_CONTINUOUS,
                PredefinedPatterns.SHORT_CONTINUOUS,
                PredefinedPatterns.SHORT_CONTINUOUS);


        if (noDevices == 1) {
            list.add(pattern1);
            list.add(pattern2);
            list.add(pattern3);
            list.add(pattern4);
        }
        else{
            list.add(pattern1);
            list.add(pattern2);
      }
        return list;
    }
}
