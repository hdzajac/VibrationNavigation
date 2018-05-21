package com.navigation.vibration;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
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
        VibrationPattern selectedItem = vibrationPatterns.get(position);

        TextView up = findViewById(R.id.text_up);
        TextView down = findViewById(R.id.text_down);
        TextView left = findViewById(R.id.text_left);
        TextView right = findViewById(R.id.text_right);

        // actual vibration thingy here
    }

    public void goToNextActivity(View view)
    {
        Intent intent = new Intent(this, PlaceSelectionActivity.class);
        startActivity(intent);
    }

    private ArrayList<VibrationPattern>  initList(int noDevices)
    {
        ArrayList<VibrationPattern> list = new ArrayList<>();
        if (noDevices == 1) {
            list.add(new VibrationPattern("1 -Device1", 1));
            list.add(new VibrationPattern("1 - Device2", 2));
        }
        else{
            list.add(new VibrationPattern("2 - Devices1", 1));
            list.add(new VibrationPattern("2 - Devices2", 2));
      }
        return list;
    }
}
