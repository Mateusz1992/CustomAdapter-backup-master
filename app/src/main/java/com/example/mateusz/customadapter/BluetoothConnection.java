package com.example.mateusz.customadapter;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by Mateusz on 2015-12-16.
 */
public class BluetoothConnection{

    private static final String TAG = "BluetoothConnection";
    public ConnectThread mConnectThread;
    public ConnectedThread mConnectedThread;
    public int mState;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    public static final int MESSAGE_READ = 2;
    public Handler mHandler = null;
    public BluetoothAdapter bluetoothAdapter = null;
    public BluetoothDevice connectedDevice = null;
    private Context activityContext;
    private String chosen_sensor;





    public BluetoothConnection(Handler tmpHandler, BluetoothAdapter tmpBluetoothAdapter, BluetoothDevice tmpDevice, Context tmpContext)
    {
        mHandler = tmpHandler;
        bluetoothAdapter = tmpBluetoothAdapter;

        connectedDevice = tmpDevice;
        activityContext = tmpContext;
    }

    public BluetoothConnection(Handler tmpHandler, BluetoothAdapter tmpBluetoothAdapter, BluetoothDevice tmpDevice, Context tmpContext, String chosen_sensor)
    {
        mHandler = tmpHandler;
        bluetoothAdapter = tmpBluetoothAdapter;

        connectedDevice = tmpDevice;
        activityContext = tmpContext;

        this.chosen_sensor = chosen_sensor;
    }

    /**
     * Set the current state of the chat connection
     *
     * @param state An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;

        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    /**
     * Return the current connection state.
     */
    public synchronized int getState() {
        return mState;
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    public synchronized void start() {
        Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        Log.d(TAG, "stop");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(STATE_NONE);
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TOAST, "Device connection was lost");
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        // Start the service over to restart listening mode
        BluetoothConnection.this.start();
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TOAST, "Unable to connect device");
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        // Start the service over to restart listening mode
        BluetoothConnection.this.start();
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param device The BluetoothDevice to connect
    //* @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    public synchronized void connect(BluetoothDevice device) {
        Log.d(TAG, "connect to: " + device);

        //Check if you have chosen a device
        if(chosen_sensor == null)
        {
            mHandler.obtainMessage(Constants.MESSAGE_DEVICE_NO_CHOICE).sendToTarget();
        }

        //Check if you have chosen available device
        /*if(chosen_sensor.equals(new String("LSM9DS1")))
        {
            mHandler.obtainMessage(Constants.MESSAGE_CHOSEN_DEVICE).sendToTarget();
        }
        else //else jest dla urzadzenia niedostepnego
        {

        }*/

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        if (mConnectThread.isSocketEmpty())
        {
            mHandler.obtainMessage(Constants.MESSAGE_SOCKET_ERROR).sendToTarget();
        }

        mConnectThread.start();
        setState(STATE_CONNECTING);

    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice
            device, final String socketType) {
        Log.d(TAG, "connected, Socket Type:" + socketType);

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }


        // Start the thread to manage the connection and perform transmissions
        setState(STATE_CONNECTED);
        mConnectedThread = new ConnectedThread(socket);

        if(mConnectedThread != null)
        {
            mConnectedThread.start();
        }

        // Send the name of the connected device back to the UI Activity
        /*Message msg = mHandler.obtainMessage(Constants.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.DEVICE_NAME, device.getName());
        msg.setData(bundle);*/
    //    mHandler.sendMessage(msg);

        //setState(STATE_CONNECTED);
    }



    private class ConnectThread extends Thread
    {
        private BluetoothSocket mmSocket;
        private BluetoothDevice mmDevice;
        private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        private final long WAITING_TIME = 10000;
        private final long WAITING_INTERVAL = 1000;

        private MyTimer timer;

        ConnectThread(BluetoothDevice device)
        {
            BluetoothSocket temporarySocket = null;

            mmDevice = device;
            try {
                temporarySocket = mmDevice.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmSocket = temporarySocket;
        }


        public boolean isSocketEmpty()
        {
            if(mmSocket == null)
            {
                return true;
            }
            return false;
        }

        public void run()
        {
            bluetoothAdapter.cancelDiscovery();
            boolean isOk = true;

            try
            {
                //manageConnectedSocket(mmSocket);
                if (mmSocket != null)
                {
                    //TURN ON TIME COUNTER
                    //IN ORDER TO
                    //timer = new MyTimer(WAITING_TIME, WAITING_INTERVAL);
                    //timer.start();

                    //CONNECTING TO THE DEVICE
                    mmSocket.connect();
                    //Toast.makeText(activityContext, "Jestem w ConnectThread - run()", Toast.LENGTH_LONG).show();
                }
                else
                {
                   // Toast.makeText(activityContext, "Nie jestem w ConnectThread - run()", Toast.LENGTH_LONG).show();
                }
            }
            catch (IOException e) {
               // e.printStackTrace();
                try
                {
                    mmSocket.close();
                    //connectionFailed();
                    //Toast.makeText(activityContext, "Unable to connect", Toast.LENGTH_LONG);
                    mHandler.obtainMessage(Constants.MESSAGE_BLUETOOTH_DEVICE_UNAVAILABLE).sendToTarget();
                }catch (IOException e2)
                {
                    Log.e(TAG, "unable to close() " + mmSocket +
                            " socket during connection failure", e2);
                    mHandler.obtainMessage(Constants.MESSAGE_CLOSE_SOCKET_ERROR).sendToTarget();
                }
                //mHandler.obtainMessage(Constants.MESSAGE_SOCKET_ERROR).sendToTarget();
                //connectionFailed();
                isOk = false;
              //  return;
            }


            synchronized (BluetoothConnection.this)
            {
                mConnectThread = null;
            }

            if(isOk)
            {
                connected(mmSocket, mmDevice, "Dupa");
            }

        }

        /*private void manageConnectedSocket(BluetoothSocket socket) throws IOException {
            socket.connect();
        }*/

        public void cancel()
        {
            try
            {
                if (mmSocket != null)
                {
                    mmSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        class MyTimer extends CountDownTimer
        {

            public MyTimer(long millisInFuture, long countDownInterval)
            {
                super(millisInFuture, countDownInterval);


            }

            @Override
            public void onTick(long millisUntilFinished) {
             //   textView.setText("seconds remaining: " + millisUntilFinished / 1000);
                //Sprawdzamy czy BluetoothSocket nie jest pusty
                //Jesli nie jest pusty to oznacza, ze polaczylismy sie
                if(!isSocketEmpty())
                {

                }
                try {
                    mmSocket.connect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFinish() {
                //
                if(isSocketEmpty())
                {

                }
                else if(!isSocketEmpty())
                {

                }
               // textView.setText("done !");
            }
        }


    }

    public class ConnectedThread extends Thread
    {
        BluetoothSocket connectedSocket;
        InputStream inStream;
        OutputStream outStream;

        public ConnectedThread(BluetoothSocket socket)
        {
            connectedSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try
            {

                if (connectedSocket != null)
                {
                    tmpIn = connectedSocket.getInputStream();
                    tmpOut = connectedSocket.getOutputStream();
                   // Toast.makeText(activityContext, "connectedSocket != null", Toast.LENGTH_LONG);
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            inStream = tmpIn;
            outStream = tmpOut;
        }

        public void run()
        {
            byte[] buffer = new byte[1024];
            int bytes;

            String dupa;

            while (true)
            {
                try {
                    if (inStream != null)
                    {
                        bytes = inStream.available();

                        if(bytes > 0)
                        {
                            byte[] pocketBytes = new byte[bytes];
                            inStream.read(pocketBytes);
                            //Log.d(TAG, "setState() " + mState + " -> " + state);
                            System.out.println(pocketBytes.toString());
                            dupa = pocketBytes.toString();
                            System.out.println(dupa);

                            mHandler.obtainMessage(MESSAGE_READ, bytes, -1, pocketBytes).sendToTarget();
                        }
                    }
                } catch (IOException e) {
                    //e.printStackTrace();
                    //Toast.makeText(activityContext, "Co jest nie tak z odbiorem", Toast.LENGTH_LONG).show();
                    //break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                outStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                connectedSocket.close();
            } catch (IOException e) { }
        }
    }
}


