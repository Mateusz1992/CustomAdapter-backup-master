package com.example.mateusz.customadapter;

/**
 * Created by Mateusz on 2015-12-15.
 */
public interface Constants {
    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int MESSAGE_SOCKET_ERROR = 6;
    public static final int MESSAGE_CLOSE_SOCKET_ERROR = 7;
    public static final int MESSAGE_BLUETOOTH_DEVICE_UNAVAILABLE = 8;
    public static final int MESSAGE_DEVICE_CONNECTED_SUCCESSFULLY = 9;
    public static final int MESSAGE_DEVICE_NO_CHOICE = 10;
    public static final int MESSAGE_CHOSEN_DEVICE = 11;
    //public static final int MESSAGE_DEVICE_CONN = 11;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
}
