package com.example.handphone;

import android.app.Activity;
import android.content.Context;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.handphone.models.PredefinedPatterns;
import com.example.handphone.models.VibrationConstants;
import com.example.handphone.models.VibrationPattern;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    public static final int MESSAGE_CONNECTION_FAILED = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DISCONNECTED = 4;
    public static final int MESSAGE_TOAST = 5;

    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final String TAG = "Handphone";
    private static final UUID MY_UUID = UUID.fromString("48f3cdb8-6359-11e8-adc0-fa7ae01bbebc");

    private ArrayList<VibrationPattern> vibrationList;

    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;

    private Button connect;
    private Button disconnect;

    //private Handler mHandler; // handler that gets info from Bluetooth service

    private TextView statusTextView;

    //Testing
    private static TextView msgTestView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusTextView = findViewById(R.id.status);
        //msgTestView = findViewById(R.id.msg);

        initList();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }


    }

    @Override
    public void onStart() {
        Log.v(TAG, "In start");
        super.onStart();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            setup();
        }
    }

    private void setup() {
        //Make sure device is discoverable
        //ensureDiscoverable();
        if (BluetoothService.getInstance() != null) {
            BluetoothService.getInstance();

        }
        // Initialize the BluetoothChatService to perform bluetooth connections
        BluetoothService.getInstance().setAppName(getResources().getString(R.string.app_name));
        // set up handler here
        BluetoothService.getInstance().setHandler(mHandler);
    }

    @Override
    public synchronized void onPause() {
        super.onPause();

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public synchronized void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth services
        //if (BluetoothService.getInstance() != null) BluetoothService.getInstance().stop();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v(TAG,"On activity result");
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    Log.v(TAG, "In connecting activity.");
                    // Get the device MAC address
                    String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    // Get the BLuetoothDevice object
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    // Attempt to connect to the device
                    Log.v(TAG, "Going to service to connect device " + address.toString());
                    BluetoothService.getInstance().connect(device);

                    //todo should do properly with handler
                    statusTextView.setText(getString(R.string.connected));
                    statusTextView.setTextColor(getResources().getColor(R.color.colorConnected));
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                Log.v(TAG, "Requested enabling bluetooth");
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setup();
                } else {
                    // User did not enable Bluetooth or an error occured
                    Log.e(TAG, "Bluetooth Not enabled");
                    Toast.makeText(this, "Bluetooth not enabled.", Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    public void connect(View view) {
        Intent serverIntent = new Intent(this, DeviceListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
    }

    public void disconnect(View view) {
        BluetoothService.getInstance().disconnect();
    }

    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer

                    //test code
                    //String readMessage = new String(readBuf, 0, msg.arg1);

                    //msgTestView.setText(readMessage);
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    Log.v(TAG,"Received "+readMessage);

                    byte vibrationID;
                    if(readBuf.length>=1) {
                        //todo some validation for trash
                        vibrationID=readBuf[0];
                        VibrationPattern selectedPattern = vibrationList.get(vibrationID-1);

                        Vibrator mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

                        mVibrator.vibrate(selectedPattern.getPatternAhead(), -1);
                    }
                    break;
                case MESSAGE_DISCONNECTED:
                    statusTextView.setText(getString(R.string.disconnected));
                    statusTextView.setTextColor(getResources().getColor(R.color.colorNotConnected));
                    Toast.makeText(getApplicationContext(), "Connection lost. ", Toast.LENGTH_SHORT).show();

                    break;
                case MESSAGE_CONNECTION_FAILED:
                    statusTextView.setText(getString(R.string.disconnected));
                    statusTextView.setTextColor(getResources().getColor(R.color.colorNotConnected));

                    Toast.makeText(getApplicationContext(), "Connection failed. ", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private void initList() {
        //todo
        //will identify patterns by id uniquely send the id to bluetooth device together with LEFT RIGHT AHEAD ETC;

        vibrationList = new ArrayList<>();

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

        //Adding them according to index to et them by id directly
        vibrationList.add(pattern1);
        vibrationList.add(pattern2);
        vibrationList.add(pattern3);
        vibrationList.add(pattern4);
        vibrationList.add(pattern1_2);
        vibrationList.add(pattern2_2);

    }
}
