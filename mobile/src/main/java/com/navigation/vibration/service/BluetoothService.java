package com.navigation.vibration.service;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.navigation.vibration.ConnectionActivity;
import com.navigation.vibration.models.VibrationConstants;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.util.UUID;

public class BluetoothService {

    private static final UUID MY_UUID = UUID.fromString("48f3cdb8-6359-11e8-adc0-fa7ae01bbebc");
    private static final String TAG = "BluetoothService";
    public static final String LEFT = "left";
    public static final String RIGHT = "right";

    private static BluetoothAdapter bluetoothAdapter;
    private AcceptThread acceptThreadRight;
    private AcceptThread acceptThreadLeft;
    private ConnectedThread connectedThreadRight;
    private ConnectedThread connectedThreadLeft;

    private Handler mHandler = null;

    public void setHandler(Handler handler) {
        mHandler = handler;
    }

    public static String APP_NAME = "";

    private int numberConnectedDevices = 0;

    //Singleton
    private static BluetoothService instance = null;

    public static BluetoothService getInstance() {
        if (instance == null) {
            instance = new BluetoothService();
            //mState = STATE_NONE;
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        return instance;
    }

    public BluetoothService() { //defeat instantiation
    }

    public static void setAppName(String appName) {
        APP_NAME = appName;
    }

    //Start the chat service. Specifically start AcceptThread to begin a
    // session in listening (server) mode. Called by the Activity onResume()
    public synchronized void start() {
        // Start the thread to listen on a BluetoothServerSocket+

        //first start thread for right
        if (acceptThreadRight == null) {
            Log.v(TAG, "Starting accepting right");
            acceptThreadRight = new AcceptThread(VibrationConstants.RIGHT);
            acceptThreadRight.start();
        }
    }

    //Start the ConnectedThread to begin managing a Bluetooth connection
    public synchronized void connected(int position, BluetoothSocket socket, BluetoothDevice device) {
        // Cancel the thread that completed the connection
        Log.v(TAG, "Called connected in service");

        // Start the thread to manage the connection and perform transmissions

        //First connect to right
        if (position == VibrationConstants.RIGHT) {

            if (acceptThreadRight != null) {
                acceptThreadRight.cancel();
                acceptThreadRight = null;
            }

            connectedThreadRight = new ConnectedThread(VibrationConstants.RIGHT, socket);
            connectedThreadRight.start();

            //starting listening for left
            if (acceptThreadLeft == null && connectedThreadLeft==null) {
                Log.v(TAG, "Starting accepting left");
                acceptThreadLeft = new AcceptThread(VibrationConstants.LEFT);
                acceptThreadLeft.start();
            }
        } else {
            connectedThreadLeft = new ConnectedThread(VibrationConstants.LEFT, socket);
            connectedThreadLeft.start();

            if (acceptThreadLeft != null) {
                acceptThreadLeft.cancel();
                acceptThreadLeft = null;
            }
            //starting listening for right
            if (acceptThreadRight == null && connectedThreadRight==null) {
                Log.v(TAG, "Starting accepting right");
                acceptThreadRight = new AcceptThread(VibrationConstants.RIGHT);
                acceptThreadRight.start();
            }
        }

    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        Log.v(TAG, "Stopping");
        if (connectedThreadRight != null) {
            connectedThreadRight.cancel();
            connectedThreadRight = null;
        }
        if (connectedThreadLeft != null) {
            connectedThreadLeft.cancel();
            connectedThreadLeft = null;
        }
        if (acceptThreadRight != null) {
            //acceptThreadRight.cancel();
            acceptThreadRight = null;
        }
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        Log.e(TAG, "Connection failed");
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(ConnectionActivity.MESSAGE_CONNECTION_FAILED);
        mHandler.sendMessage(msg);
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        Log.e(TAG, "Connection lost");
        Message msg = mHandler.obtainMessage(ConnectionActivity.MESSAGE_CONNECTION_LOST);
        mHandler.sendMessage(msg);
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(int position, byte[] out) {
        // Create temporary object
        ConnectedThread r;
        synchronized (this) {
            if (position == VibrationConstants.LEFT) {
                // Synchronize a copy of the ConnectedThread
                r = connectedThreadLeft;
                Log.v(TAG, "Writing for left");
            } else {
                r = connectedThreadRight;
                Log.v(TAG, "Writing for right");
            }
            // Perform the write unsynchronized
        }
        r.write(out);

    }


    //
    // This thread runs while listening for incoming connections. It behaves
    // like a server-side client. It runs until a connection is accepted
    // (or until cancelled).
    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;
        private int position;

        public AcceptThread(int leftRight) {
            // Use a temporary object that is later assigned to mmServerSocket
            // because mmServerSocket is final.
            Log.v(TAG, "In accept " + leftRight);
            position= leftRight;

            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code.
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME, MY_UUID);
                Log.e(TAG, "Created socket in accept thread");

            } catch (IOException e) {
                Log.e(TAG, "Socket's accept listen() method failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            setName("Accept running ");
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned.
            while (true) { //This is a blocking call and will only return on a successful connection or an exception
                try {
                    Log.e(TAG, "Accepting");
                    socket = mmServerSocket.accept();
                    Log.e(TAG, "Accepted");

                } catch (IOException e) {
                    Log.e(TAG, "Socket  's accept() method failed for ", e);
                    break;
                }
                if (socket != null) {
                    // A connection was accepted. Perform work associated with
                    // the connection in a separate thread.
                    synchronized (BluetoothService.this) {
                        connected(position, socket, socket.getRemoteDevice());
                        numberConnectedDevices++;
                    }
                }
            }
        }
//todo nu ne rotim

        // Closes the connect socket and causes the thread to finish.
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {

        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private int position;

        public ConnectedThread(int leftRight, BluetoothSocket socket) {
            mmSocket = socket;
            position = leftRight;

            Log.v(TAG, "In connected " + leftRight);
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
                Message msg = mHandler.obtainMessage(ConnectionActivity.MESSAGE_UPDATE_POSITION);
                Bundle bundle = new Bundle();
                bundle.putInt(VibrationConstants.POSITION, leftRight);
                msg.setData(bundle);
                mHandler.sendMessage(msg);

            } catch (IOException e) {
                Log.e(TAG, "Exception in connected constructror for " + position);
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.v(TAG, "Running read on connected " + position);
            byte[] buffer = new byte[1024];
            int bytes;
            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);
                } catch (IOException e) {
                    Log.e(TAG, "Exception in connected thread");
                    connectionLost();
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
            } catch (IOException e) {
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }


}
