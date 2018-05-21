package com.navigation.vibration;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class NoDevicesSelectionActivity extends AppCompatActivity {

    public static final String NO_DEVICES = "NO_DEVICES";
    private String noDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_devices_selection);
    }

    public void select1(View view) {
        noDevices = "1";
        goToNextActivity();

    }

    public void select2(View view) {
        noDevices = "2";
        goToNextActivity();
    }

    private void goToNextActivity()
    {
        Intent intent = new Intent(this, ConnectionActivity.class);
        intent.putExtra(NO_DEVICES, noDevices);
        startActivity(intent);

    }
}
