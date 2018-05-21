package com.navigation.vibration;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class NavigationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        Intent intent = getIntent();

        try {
            JSONObject json = new JSONObject(intent.getStringExtra("response"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        TextView textView = findViewById(R.id.navigation_text_area);
        textView.setText(intent.getStringExtra("response"));

    }
}
