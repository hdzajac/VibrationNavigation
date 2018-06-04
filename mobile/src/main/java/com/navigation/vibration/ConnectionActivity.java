package com.navigation.vibration;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.navigation.vibration.models.VibrationConstants;
import com.navigation.vibration.service.BluetoothService;

public class ConnectionActivity extends AppCompatActivity {

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;


    private static final String TAG ="ConnectionActivity" ;

    public static final int MESSAGE_UPDATE_POSITION = 1;
    public static final int MESSAGE_CONNECTION_FAILED = 2;
    public static final int MESSAGE_CONNECTION_LOST = 3;

    private int noDevices;
    public String messageToDisplay = "Connect both devices";
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;

    private Button connLeft;
    private Button connRight;
    private Button next;

    private boolean isLeftConnected;
    private boolean isRightConnected;

    public static final String NO_DEVICES = "NO_DEVICES";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);
        connLeft = findViewById(R.id.conn_left);
        connRight = findViewById(R.id.conn_right);
        TextView editText = findViewById(R.id.textView);
        next = findViewById(R.id.next);

        //  List<Node> connectedNodes =
        //         Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await().getNodes();

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String message = intent.getStringExtra(NoDevicesSelectionActivity.NO_DEVICES);
        Log.e(TAG,message);
        noDevices = Integer.parseInt(message);

        //when there is one device work with right one
        if (noDevices == 1) {
            connLeft.setVisibility(View.GONE);
            connRight.setText(getString(R.string.connect_one));
            messageToDisplay = "Select device to connect";
            connRight.setBackgroundColor(getResources().getColor(R.color.button_red));
        }
        else{
            connLeft.setBackgroundColor(getResources().getColor(R.color.button_red));
            connRight.setBackgroundColor(getResources().getColor(R.color.button_red));
            messageToDisplay = "Select devices to connect";
        }


        editText.setText(messageToDisplay);
        next.setEnabled(false);
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
        super.onStart();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
            setup();
        }
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
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth services
        //if (BluetoothService.getInstance() != null) BluetoothService.getInstance().stop();
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    Log.v(TAG,"In connecting activity.");
                    // Get the device MAC address
                    String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    String devicePosition = data.getExtras().getString(DeviceListActivity.DEVICE_POSITION);

                    // Attempt to connect to the device
                    Log.v(TAG,"Going to service to connect device "+address.toString());

                    if(devicePosition.equals(BluetoothService.LEFT))
                        okConnectedLeft(); //todo do with handler properly
                    else
                        okConnectedRight();
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setup();
                } else {
                    // User did not enable Bluetooth or an error occured
                    Toast.makeText(this, "Bluetooth not enabled.", Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    private void setup() {
        //Make sure device is discoverable
        ensureDiscoverable();

        //was in resume
        if (BluetoothService.getInstance() != null) {
            //if (BluetoothService.getInstance().getState() == BluetoothService.getInstance().STATE_NONE) {
                BluetoothService.getInstance().start();
            //}
        }
        // Initialize the BluetoothChatService to perform bluetooth connections
        BluetoothService.getInstance().setAppName(getResources().getString(R.string.app_name));
        BluetoothService.getInstance().setHandler(mHandler);

    }

    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }


    private void okConnectedLeft() {
        connLeft.setBackgroundColor(getResources().getColor(R.color.button_green));
        connLeft.setText(getString(R.string.connected_left));
        isLeftConnected = true;
    }

    private void disconnectedLeft() {
        connLeft.setBackgroundColor(getResources().getColor(R.color.button_red));
        connLeft.setText(getString(R.string.connect_left));
        isLeftConnected = false;
    }

    private void disconnectedRight() {
        connRight.setBackgroundColor(getResources().getColor(R.color.button_red));
        if(noDevices==1)
            connRight.setText(getString(R.string.connect_one));
        else
            connRight.setText(getString(R.string.connect_right));
        isRightConnected = false;
    }


    private void okConnectedRight() {
        if(noDevices==1)
            connRight.setText(getString(R.string.connected_one));
        else
            connRight.setText(getString(R.string.connected_right));

        connRight.setBackgroundColor(getResources().getColor(R.color.button_green));
        isRightConnected = true;
        enableNext();
    }

    public void goToNextActivity(View view) {
        Intent intent = new Intent(this, PatternSelectionActivity.class);
        intent.putExtra(NO_DEVICES, noDevices);
        startActivity(intent);

    }

    private void enableNext() {
        if (noDevices == 1) {
            if (isLeftConnected || isRightConnected)
                next.setEnabled(true);
            connLeft.setEnabled(false);
            connRight.setEnabled(false);
        }

        if (noDevices == 2 && isRightConnected && isRightConnected)
            next.setEnabled(true);
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_UPDATE_POSITION:
                    int pos = msg.getData().getInt(VibrationConstants.POSITION);
                    if(pos==VibrationConstants.LEFT){
                        okConnectedLeft();
                    } else {
                        okConnectedRight();
                    }
                    break;
                case MESSAGE_CONNECTION_FAILED:
                    if(noDevices==1)
                        disconnectedRight();
                    else {
                        disconnectedLeft();
                        disconnectedRight();
                    }
                    Toast.makeText(getApplicationContext(), "Connection failed.", Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_CONNECTION_LOST:
                    if(noDevices==1)
                        disconnectedRight();
                    else {
                        disconnectedLeft();
                        disconnectedRight();
                    }
                    //todo check if this is displayed inmap fragment
                    Toast.makeText(getApplicationContext(), "Connection lost.", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    // ---------------- T E S T

    public void sendTestRight(View view) {
        Log.v(TAG,"Sending to slaves");
        byte[] sendRight = new byte[]{VibrationConstants.SHORT_CONTINUOUS_TAG};
        Log.v(TAG,"Sending to right");
        BluetoothService.getInstance().write(VibrationConstants.RIGHT,sendRight);

    }

    public void sendTestLeft(View view) {
        byte[] sendLeft= new byte[]{VibrationConstants.SHORT_CONTINUOUS_TAG};
        Log.v(TAG,"Sending to left");
        BluetoothService.getInstance().write( VibrationConstants.LEFT,sendLeft);

    }
}
