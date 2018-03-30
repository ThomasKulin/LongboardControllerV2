package com.example.thomas.vesccontroller.Activities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
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
import com.example.thomas.vesccontroller.R;
import com.example.thomas.vesccontroller.Helpers.SaveState;

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
    TextView totalDistance;
    TextView averageSpeed;
    TextView maxSpeed;
    TextView connectionState;
    static RingProgressBar mRingProgressBar;
    ImageView longboardButton;
    ImageView sendButton;
    String TAG = "Board_Activity";

    SaveState save  = new SaveState();
    static short batteryLevel = 0; //battery percentage (0-100)

    static BluetoothDevice btDevice;
    public static BluetoothConnectionService mBluetoothConnection;
    private static final UUID MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static boolean connected = false;
    private static PacketTools.mc_values values = new PacketTools.mc_values();
    private static TimerTask refreshValTimer;
    private static Timer timer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.board_activity);

        totalDistance = (TextView) findViewById(R.id.TV_total_distance);
        averageSpeed = (TextView) findViewById(R.id.TV_average_speed);
        maxSpeed = (TextView) findViewById(R.id.TV_max_speed);
        connectionState = (TextView) findViewById(R.id.TV_connection_state);

        /**
         * Bluetooth Connection
         */
        mBluetoothConnection = new BluetoothConnectionService(this);

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
                        if (connected) {
                            try {
                                endBtConnection();
                                connectionState.setText("DISCONNECTED");
                                connectionState.setTextColor(getResources().getColor(R.color.disconnected));
                                cancelTimer();
                            } catch (Exception e) {
                                Log.d(TAG, "Connection State: Connected --> Disconnected FAILED");
                                Toast.makeText(Board_Activity.this, "Failed to Disconnect", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            try {
                                initiateBtConnection();
                                connectionState.setText("CONNECTED");
                                connectionState.setTextColor(getResources().getColor(R.color.connected));
                                initializeTimer();
                            } catch (Exception e) {
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
                if(connected) {
                    PacketTools.vescUartGetValue();
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfile();
        initializeTimer();
    }

    @Override
    protected void onPause(){
        super.onPause();
        cancelTimer();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        cancelTimer();
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
        //SaveState load = SaveState.loadData(mContext);
        List<BoardProfile> load = save.readFromFile(mContext);

        if (load == null) { //if there is no previous save
            //SaveState.saveData(SaveState.getInstance(), mContext);
            save.saveToFile(mContext);
            //load = SaveState.loadData(mContext);
            load = save.readFromFile(mContext);
        }

//        if (load.bp.size() == 0) { //if there is no existing board profile
//            load.bp.add(profile); //add default profile
//            SaveState.saveData(load, mContext);
//        }
        if(load.size() ==0){
            load.add(profile);
            save.bp = load;
            save.saveToFile(mContext);
        }
        //profile = load.bp.get(0);
        profile = load.get(0);

        //update views
        currentProfile = profile;
        getSupportActionBar().setTitle(currentProfile.getName()); //sets the name displayed at the top of the app
        totalDistance.setText("" + currentProfile.getTotalDist());
        averageSpeed.setText("" + currentProfile.getAvgSpeed());
        maxSpeed.setText("" + currentProfile.getMaxSpeed());
        String deviceAddress = currentProfile.getBtDeviceAddress();
        btDevice = SaveState.getDevice(deviceAddress);
    }

    public static void updateValues(PacketTools.mc_values values) {
        byte cells = currentProfile.getCellCount();
        float vMin = (float) (currentProfile.getMinVoltage() / cells);
        float vMax = (float) (currentProfile.getMaxVoltage() / cells);
        double batteryLevelFP = (values.v_in / cells);
        if (batteryLevelFP > vMin && batteryLevelFP < vMax) {
            batteryLevel = (short) (((double)(batteryLevelFP - vMin)) / ((double)(vMax - vMin)) * 100);
        } else if (batteryLevelFP > vMax) {
            batteryLevel = 100;
        } else
            batteryLevel = 0;
        mRingProgressBar.setProgress(batteryLevel);
    }


    private static void initiateBtConnection() {
        mBluetoothConnection.startConnectThread(btDevice, MY_UUID_INSECURE);
    }

    private static void endBtConnection() {
        mBluetoothConnection.endConnection();
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
            timer.scheduleAtFixedRate(refreshValTimer, 500, 2000);//this line starts the timer at the same time its executed
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private static void cancelTimer(){
        try {
            timer.cancel();
            Log.d("TAG", "GetValues Timer Canceled");
        }catch (Exception e){
            Log.d("TAG", "Failed to Cancel getValues Timer");
            e.printStackTrace();
        }
    }
    //----------------------------------------------------------------------------------------------
}
