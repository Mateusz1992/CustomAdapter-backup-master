package com.example.mateusz.customadapter;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

//import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";


    BluetoothAdapter bluetoothAdapter;
    List<codeLearnChapter> bluetoothDevicesList = new ArrayList<codeLearnChapter>();
    ListView codeLearnLessons;


    Button searchButton;
    Set<BluetoothDevice> pairedDevices;

    /*private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;*/
    private int mState;

    public final int REQUEST_ENABLE_BT = 1;
    private boolean turnedOn = false;
    public static final int MESSAGE_READ = 2;

    List<String> listOfSentMessages = new ArrayList<String>();

    // Constants that indicate the current connection state
   /* public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device*/

    public final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
                codeLearnChapter device_paired = new codeLearnChapter();
                device_paired.chapterName = device.getName() + "\n" + device.getAddress();
                device_paired.chapterDescription = "\nFound ";
                bluetoothDevicesList.add(device_paired);

                setListLayout();
            }
        }
    };





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();



        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                if (bluetoothAdapter == null) {
                    Toast.makeText(MainActivity.this, "There is no bluetooth in this mobile", Toast.LENGTH_LONG);
                }

                if (!bluetoothAdapter.isEnabled()) {
                    Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBluetooth, REQUEST_ENABLE_BT);
                }
                else
                {
                    turnedOn = true;
                }

                if(turnedOn == true)
                {
                    pairedDevices = bluetoothAdapter.getBondedDevices();

                    if(pairedDevices.size() > 0)
                    {
                        for (BluetoothDevice device:pairedDevices)
                        {
                            codeLearnChapter device_paired = new codeLearnChapter();
                            device_paired.chapterName = device.getName() + "\n" + device.getAddress();
                            device_paired.chapterDescription = "\nPaired ";
                            bluetoothDevicesList.add(device_paired);
                        }
                        Toast.makeText(MainActivity.this, "PairedDevices", Toast.LENGTH_LONG).show();
                        setListLayout();
                    }

                    if(!bluetoothAdapter.startDiscovery())
                    {

                    }
                    else
                    {
                        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                        registerReceiver(mReceiver, filter);
                        Toast.makeText(MainActivity.this, "Searching for devices", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

    }

    public void setListLayout() {

       final CodeLearnAdapter chapterListAdapter = new CodeLearnAdapter();
        codeLearnLessons = (ListView) findViewById(R.id.listView1);
        codeLearnLessons.setAdapter(chapterListAdapter);

        codeLearnLessons.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                BluetoothDevice tmpDevice = null;
                codeLearnChapter chapter = chapterListAdapter.getCodeLearnChapter(arg2);
                boolean found1 = false;
                //boolean found = false;


                for (BluetoothDevice device : pairedDevices) {
                    //Toast.makeText(MainActivity.this, "Znaleziono1", Toast.LENGTH_LONG).show();
                    if (chapter.chapterName.equals(device.getName() + "\n" + device.getAddress())) {
                        tmpDevice = device;
                        found1 = true;
                        Toast.makeText(MainActivity.this, "Znaleziono", Toast.LENGTH_LONG).show();
                        //connection = new BluetoothConnection(bluetoothAdapter, tmpDevice);
                        //connection = new BluetoothConnection(mHandler, bluetoothAdapter, tmpDevice, MainActivity.this);
                        break;
                    }
                }

                /*if (!found1) {
                    Toast.makeText(MainActivity.this, "False", Toast.LENGTH_LONG).show();
                }*/

                if (found1) {
                    //Toast.makeText(MainActivity.this, "Found", Toast.LENGTH_LONG).show();
                    /*if (null != connection) {
                        //Toast.makeText(MainActivity.this, "null != connection", Toast.LENGTH_LONG).show();
                        if(connection.connect(tmpDevice))
                        {

                        }
                    }*/
                    Toast.makeText(MainActivity.this, "MainActivity11", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, ChosenDevice.class);
                    //Bundle bAdapter = new Bundle();
                    //bAdapter.putParcelable("adapter", tmpDevice);
                    intent.putExtra("device", tmpDevice);
                    startActivity(intent);
                    //if (connection.mState == BluetoothConnection.STATE_CONNECTING) {
                    //  Toast.makeText(MainActivity.this, "Connecting1", Toast.LENGTH_LONG).show();
                    //}

                    //  if (connection.mState == BluetoothConnection.STATE_CONNECTED) {
                    //    Toast.makeText(MainActivity.this, "Connected1", Toast.LENGTH_LONG).show();*/
                    //Intent intent = new Intent(MainActivity.this, ChosenDevice.class);
                    //startActivity(intent);
                    //  }
                }
                // Toast.makeText(MainActivity.this, chapter.chapterName, Toast.LENGTH_LONG).show();

            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(REQUEST_ENABLE_BT == resultCode)
        {
            Toast.makeText(MainActivity.this, "Bluetooth is turning on", Toast.LENGTH_LONG);
        }
        else if (RESULT_OK == resultCode)
        {
            Toast.makeText(MainActivity.this, "Bluetooth is turned on successfully...", Toast.LENGTH_LONG);
            turnedOn = true;
        }
        else if(RESULT_CANCELED == resultCode)
        {
            Toast.makeText(MainActivity.this, "User does not turned on bluetooth", Toast.LENGTH_LONG);
            turnedOn = false;
        }
    }

    private void init() {
        searchButton = (Button)findViewById(R.id.searchButton);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = BluetoothConnection.STATE_NONE;
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class CodeLearnAdapter extends BaseAdapter
    {
        List<codeLearnChapter> codeLearnChapterList = bluetoothDevicesList;
        public int getCount() {
            return codeLearnChapterList.size();
        }


        public codeLearnChapter getCodeLearnChapter(int position)
        {
            return codeLearnChapterList.get(position);
        }

        @Override
        public Object getItem(int position) {
            return codeLearnChapterList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View rowView = inflater.inflate(R.layout.listitem, parent,false);

            if(convertView==null)
            {
                inflater = (LayoutInflater) MainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.listitem, parent,false);
            }
            TextView chapterName = (TextView)convertView.findViewById(R.id.textView1);
            TextView chapterDesc = (TextView)convertView.findViewById(R.id.textView2);

            codeLearnChapter chapter = codeLearnChapterList.get(position);

            chapterName.setText(chapter.chapterName);
            chapterDesc.setText(chapter.chapterDescription);

            return convertView;
        }


    }

    public class codeLearnChapter {
        String chapterName;
        String chapterDescription;
    }

    public List<codeLearnChapter> getDataForListView()
    {
        List<codeLearnChapter> codeLearnChaptersList = new ArrayList<codeLearnChapter>();

        for(int i=0;i<10;i++)
        {

            codeLearnChapter chapter = new codeLearnChapter();
            chapter.chapterName = "Chapter "+i;
            chapter.chapterDescription = "This is description for chapter "+i;
            codeLearnChaptersList.add(chapter);
        }

        return codeLearnChaptersList;

    }
}


