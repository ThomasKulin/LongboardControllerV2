package com.example.thomas.vesccontroller.Activities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.thomas.vesccontroller.Helpers.Communications.BluetoothConnectionService;
import com.example.thomas.vesccontroller.Helpers.BoardProfile;
import com.example.thomas.vesccontroller.Activities.Board_List.Board_List_Activity;
import com.example.thomas.vesccontroller.Helpers.Communications.PacketTools;
import com.example.thomas.vesccontroller.Helpers.DataLogger;
import com.example.thomas.vesccontroller.R;
import com.example.thomas.vesccontroller.Helpers.SaveState;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import io.netopen.hotbitmapgg.library.view.RingProgressBar;

import static java.lang.Thread.sleep;


/**
 * Created by Thomas on 2017-12-28.
 */

public class Board_Activity extends AppCompatActivity {

    static BoardProfile currentProfile;
    static TextView hwVersion;
    static TextView totalDistance;
    static TextView averageSpeed;
    static TextView maxSpeed;
    TextView connectionState;
    static RingProgressBar mRingProgressBar;
    ImageView longboardButton;
    ImageView sendButton;
    private static final String TAG = "BoardActivity";

    SaveState save  = new SaveState();
    static DataLogger log = new DataLogger();
    static short batteryLevel = 0; //battery percentage (0-100)
    private static double voltages[] = new double[30];

    static BluetoothDevice btDevice;
    public static BluetoothConnectionService mBluetoothConnection;
    private static final UUID MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    IntentFilter connectionFilter;
    public static boolean connected = false;
    public static PacketTools.mc_values values = new PacketTools.mc_values(); //master
    public static PacketTools.mc_values values2 = new PacketTools.mc_values(); //slave
    public static PacketTools.mc_states states = new PacketTools.mc_states();
    public static byte VescSelect = 0; //which ESC are we getting values from currently?
    private static TimerTask refreshValTimer;
    private static Timer timer;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.board_activity);

        hwVersion = (TextView) findViewById(R.id.TV_hw_version);
        totalDistance = (TextView) findViewById(R.id.TV_total_distance);
        averageSpeed = (TextView) findViewById(R.id.TV_average_speed);
        maxSpeed = (TextView) findViewById(R.id.TV_max_speed);
        connectionState = (TextView) findViewById(R.id.TV_connection_state);

        /**
         * Bluetooth Connection
         */
        mBluetoothConnection = new BluetoothConnectionService(this);
        connectionFilter = new IntentFilter();
        connectionFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        connectionFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        connectionFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(mBluetoothReceiver,connectionFilter);

        /**
         * ACTION BAR
         */
        Toolbar myToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(myToolbar);
        /**
         * BATTERY INDICATOR
         */
        mRingProgressBar = (RingProgressBar) findViewById(R.id.progress_bar_battery);
        // Set the progress bar's progress
        mRingProgressBar.setProgress(batteryLevel);
        mRingProgressBar.setOnProgressListener(new RingProgressBar.OnProgressListener() {
            @Override
            public void progressToComplete() {
                // Progress reaches the maximum callback default Max value is 100
            }
        });

        /**
         * Longboard Connect Button Listener
         */
        connectionState.setTextColor(getResources().getColor(R.color.disconnected));
        longboardButton = (ImageView) findViewById(R.id.button_longboard);
        longboardButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                //initiateBtConnection(device)
                view.startAnimation(AnimationUtils.loadAnimation(view.getContext(), R.anim.anim_item));
                if (btDevice != null) {
                    if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                        BluetoothConnectionService.enableBtPrompt(Board_Activity.this);
                    } else {
                        if(connected) {
                            try {
                                endBtConnection();
                            }catch (Exception e){
                                Log.d(TAG, "Connection State: Connected --> Disconnected FAILED");
                                Toast.makeText(Board_Activity.this, "Failed to Disconnect", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else {
                            try {
                                initiateBtConnection();
                            }catch (Exception e){
                                Log.d(TAG, "Connection State: Disconnected --> Connected FAILED");
                                Toast.makeText(Board_Activity.this, "Failed to Connect", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                } else {
                    Toast.makeText(Board_Activity.this, "Please select a Bluetooth Device", Toast.LENGTH_SHORT).show();
                }
            }
        });
        /**
         * Temporary Send Button Listener
         */
        sendButton = (ImageView) findViewById(R.id.button_send);
        sendButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                view.startAnimation(AnimationUtils.loadAnimation(view.getContext(), R.anim.anim_item));

                Intent newActivity = new Intent(view.getContext(), Control_Activity.class);
                startActivity(newActivity);
            }
        });

        /**
         * timer task which refreshes values every X seconds
         */
        refreshValTimer = new TimerTask() {

            @Override
            public void run() {
                if(mBluetoothConnection.isConnected()) {
//                    PacketTools.vescUartGetValue(PacketTools.COMM_PACKET_ID.COMM_GET_VALUES); //Master
//                    PacketTools.vescUartGetValue(PacketTools.COMM_PACKET_ID.COMM_FORWARD_CAN);
                    if (Board_Activity.VescSelect==1){PacketTools.vescUartGetValue(PacketTools.COMM_PACKET_ID.COMM_GET_VALUES);} //Master
                    else if(Board_Activity.VescSelect==0){PacketTools.vescUartGetValue(PacketTools.COMM_PACKET_ID.COMM_FORWARD_CAN);} //Slave
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfile();
        //initializeTimer();
        registerReceiver(mBluetoothReceiver,connectionFilter);
    }

    @Override
    protected void onPause(){
        super.onPause();
        cancelTimer();
        unregisterReceiver(mBluetoothReceiver);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        cancelTimer();
        unregisterReceiver(mBluetoothReceiver);
    }

    /**
     * MENU STUFF
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.action_list_boards:
                Toast.makeText(this, "list all board profiles", Toast.LENGTH_SHORT)
                        .show();
                Intent boardListActivity = new Intent(this, Board_List_Activity.class);
                startActivity(boardListActivity);
                break;
            // action with ID action_settings was selected
            case R.id.action_settings:
                Toast.makeText(this, "VESC settings selected", Toast.LENGTH_SHORT)
                        .show();
                Intent VescActivity = new Intent(this, Vesc_Settings_Activity.class);
                startActivity(VescActivity);
                break;
            default:
                break;
        }
        return true;
    }

//------------------------------functions and stuff-------------------------------------------------

    private void loadProfile() { //find the first saved board profile
        Context mContext = getApplicationContext();
        BoardProfile profile = new BoardProfile();
        List<BoardProfile> load = save.readFromFile(mContext);

        if (load == null) { //if there is no previous save
            //SaveState.saveData(SaveState.getInstance(), mContext);
            save.saveToFile(mContext);
            //load = SaveState.loadData(mContext);
            load = save.readFromFile(mContext);
        }
        if(load.size() ==0){
            load.add(profile);
            save.bp = load;
            save.saveToFile(mContext);
        }
        profile = load.get(0);

        //update views
        currentProfile = profile;
        getSupportActionBar().setTitle(currentProfile.getName()); //sets the name displayed at the top of the app
        hwVersion.setText("HARDWARE V" + currentProfile.getVersion());
        totalDistance.setText(String.format("%.1f", (float)currentProfile.getTotalDist()/1000f));
        averageSpeed.setText(String.format("%.1f", (float)currentProfile.getAvgSpeed()));
        maxSpeed.setText(String.format("%.1f", (float)currentProfile.getMaxSpeed()));
        String deviceAddress = currentProfile.getBtDeviceAddress();
        btDevice = SaveState.getDevice(deviceAddress);
    }

    public static void updateValues(PacketTools.mc_values values) {

        byte cells = currentProfile.getCellCount();
        float vMin = (float) (currentProfile.getMinVoltage() / cells);
        float vMax = (float) (currentProfile.getMaxVoltage() / cells);
        double batteryLevelFP = (Board_Activity.values.v_in / cells);
        double temp;
        System.arraycopy(voltages, 0, voltages, 1, voltages.length - 1); //shit way of taking moving avg
        voltages[0] = batteryLevelFP;
        double filteredVoltage = smoothVoltage(voltages);
        if (filteredVoltage > vMin && filteredVoltage < vMax) {
            batteryLevel = (short) (((double) (filteredVoltage - vMin)) / ((double) (vMax - vMin)) * 100);
        } else if (filteredVoltage > vMax) {
            batteryLevel = 100;
        } else
            batteryLevel = 0;
        mRingProgressBar.setProgress(batteryLevel);

        if (values.controller_id==1) {
            Board_Activity.values = values;
            Log.d(TAG, "Updated VESC 1 Params");
            log.logData(values, null, null, Control_Activity.command);
        }
        else if (values.controller_id==2) {
            Board_Activity.values2 = values;
            Log.d(TAG, "Updated VESC 2 Params");
            log.logData(null, values2, null, Control_Activity.command);
        }

//        Log.d(TAG, "Updated motor commands");
//        log.logData(null, null, null, Control_Activity.command);
    }

    public static void updateStates(PacketTools.mc_states states) {
        Board_Activity.states = states;
        Log.d(TAG, "Updated System States");
        log.logData(null, null, states, null);
    }


    private static boolean initiateBtConnection() {
        return mBluetoothConnection.startConnectThread(btDevice, MY_UUID_INSECURE);
    }

    private static boolean endBtConnection() {
        return mBluetoothConnection.endConnection();
    }

    public static void writeBT(byte[] data) {
        mBluetoothConnection.write(data);
    }

    public static void writeBT(byte[] data, int off, int len) {
        try {
            mBluetoothConnection.write(data, off, len);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    private static void initializeTimer(){
        try {
            Log.d("TAG", "GetValues Timer Initialized");
            timer = new Timer("getValues_Timer");//create a new Timer
            timer.scheduleAtFixedRate(refreshValTimer, 100, 100);//this line starts the timer at the same time its executed
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private static void cancelTimer(){
        try {
            timer.cancel();
            timer = null;
            Log.d("TAG", "GetValues Timer Canceled");
        }catch (Exception e){
            Log.d("TAG", "Failed to Cancel getValues Timer");
            e.printStackTrace();
        }
    }
    public static double smoothVoltage(double values[]){
        double value = values[0]; // start with the first input
        double sum = 0;
        int num = 0;
        for (int i=1, len=values.length; i<len; ++i){
            sum += values[i];
            if(values[i] != 0){
                num++;
            }
        }
        sum /= num;
        return sum;
    }
    private BroadcastReceiver mBluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                //Do something if connected
                connected = true;
                connectionState.setText("CONNECTED");
                connectionState.setTextColor(getResources().getColor(R.color.connected));
                initializeTimer();
                Toast.makeText(getApplicationContext(), "BT Connected", Toast.LENGTH_SHORT).show();
            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                //Do something if disconnected
                connected = false;
                connectionState.setText("DISCONNECTED");
                connectionState.setTextColor(getResources().getColor(R.color.disconnected));
                cancelTimer();
                Toast.makeText(getApplicationContext(), "BT Disconnected", Toast.LENGTH_SHORT).show();
            }
            //else if...
        }
    };
    //----------------------------------------------------------------------------------------------
}
