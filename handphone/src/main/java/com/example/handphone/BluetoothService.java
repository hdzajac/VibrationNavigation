package com.example.handphone;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothService {

    private static final UUID MY_UUID = UUID.fromString("48f3cdb8-6359-11e8-adc0-fa7ae01bbebc");
    private static final String TAG = "BluetoothService";
    public static final String LEFT = "left";
    public static final String RIGHT = "right";

    private static BluetoothAdapter bluetoothAdapter;
    private ConnectThread connectThread;
    private ConnectedThread connectedThread;

    private Handler mHandler = null;

    public static String APP_NAME = "";

    //Singleton
    private static BluetoothService instance = null;

    public static BluetoothService getInstance() {
        if (instance == null) {
            instance = new BluetoothService();
            Log.v(TAG,"Started service");
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        }
        return instance;
    }

    public BluetoothService() {
       }

    public void setHandler(Handler handler){
        mHandler = handler;
    }
    public static void setAppName(String appName) {
        APP_NAME = appName;
    }


    //Start the ConnectThread to initiate a connection to a remote device.
    public synchronized void connect(BluetoothDevice device) {
        Log.v(TAG,"in connect");
        // Start the thread to connect with the given device
        connectThread = new ConnectThread(device);
        connectThread.start();

    }

    //Start the ConnectedThread to begin managing a Bluetooth connection
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {

        Log.v(TAG, "Called connected in service");
        // Cancel the thread that completed the connection
        // Start the thread to manage the connection and perform transmissions
        connectedThread = new ConnectedThread(socket);
        connectedThread.start();

    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        Log.v(TAG, "Stopping");
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }
        //setState(STATE_NONE);
    }

    public void disconnect()
    {
        if(connectedThread!=null){
            connectedThread.cancel();
            connectedThread=null;
        }
        if(connectThread!=null){
            connectThread.cancel();
            connectThread=null;
        }
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        Log.e(TAG, "Connection failed");
        Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_CONNECTION_FAILED);
        mHandler.sendMessage(msg);
    }


    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        //setState(STATE_LISTEN);
        Log.e(TAG, "Connection lost");
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_DISCONNECTED);
        mHandler.sendMessage(msg);
    }


    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;

            Log.v(TAG, "In connect thread");
            BluetoothSocket tmp = null;
            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
            }
            mmSocket = tmp;
        }

        public void run() {
            setName("ConnectThread");
            Log.v(TAG, "Running connect thread");
            // Always cancel discovery because it will slow down a connection
            bluetoothAdapter.cancelDiscovery();
            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                Log.v(TAG, "Connecting ");
                mmSocket.connect();
                Log.v(TAG, "Connected ");

            } catch (IOException e) {
                Log.e(TAG,"Exception in connect");
                connectionFailed();
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                }
                // Start the service over to restart listening mode
                return;
            }
            // Start the connected thread
            Log.v(TAG, "Going to connected thread ");
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
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

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            Log.v(TAG, "In connected");
            setName("Connected " );
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.v(TAG, "Running read on connected");
            byte[] buffer = new byte[1024];
            int bytes;
            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI Activity
                    mHandler.obtainMessage(MainActivity.MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                } catch (IOException e) {
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
            Log.v(TAG, "Running write on connected " );
            try {
                mmOutStream.write(buffer);
                // Share the sent message back to the UI Activity
                // mHandler.obtainMessage(BluetoothChat.MESSAGE_WRITE, -1, -1, buffer)
                //        .sendToTarget();
            } catch (IOException e) {
                Log.v(TAG, " FAILED write on connected");

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
