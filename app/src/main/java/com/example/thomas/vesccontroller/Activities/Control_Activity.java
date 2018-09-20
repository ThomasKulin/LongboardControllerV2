package com.example.thomas.vesccontroller.Activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.thomas.vesccontroller.Helpers.BoardProfile;
import com.example.thomas.vesccontroller.Helpers.Communications.PacketTools;
import com.example.thomas.vesccontroller.Helpers.SaveState;
import com.example.thomas.vesccontroller.R;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.thomas.vesccontroller.Activities.Board_Activity.batteryLevel;
import static com.example.thomas.vesccontroller.Activities.Board_Activity.currentProfile;


/**
 * Created by Thomas on 2018-03-25.
 */

public class Control_Activity extends AppCompatActivity {

    boolean sendEnable = false;
    static boolean first = true;
    int throttleVal = 0;
    float sendVal = 0f;
    static float currentVal = 0f;
    int motorMaxCurrent = 60;
    int motorMinCurrent = -40;
    //monitring and indication
    static float topSpeedVal = 0;
    static float speed = 0;
    static float avgSpeedVal = 0;
    static float distanceVal = 0, distancePrev = 0, distanceOffset = 0;
    static double timeSum = 0;

    SeekBar throttleBar;
    static ImageView batteryIndicatorImage;
    static TextView speedText;
    static  TextView batteryText;
    static TextView distance;
    static TextView topSpeed;
    static TextView current;

    private static TimerTask readValTimerTask;
    private static TimerTask sendValTimerTask;
    private static Timer readValTimer;
    private static Timer sendValTimer;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.control_activity);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));

        /**
         * ACTION BAR
         */
        Toolbar myToolbar = (Toolbar) findViewById(R.id.controller_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle(currentProfile.getName()); //sets the name displayed at the top of the app

        /**
         * INDICATORS
         */
        batteryIndicatorImage= (ImageView) findViewById(R.id.image_view_controller_battery);
        speedText= (TextView) findViewById(R.id.TV_speed_indicator);
        batteryText = (TextView) findViewById(R.id.TV_controller_battery);
        distance = (TextView) findViewById(R.id.TV_travel_distance);
        topSpeed = (TextView) findViewById(R.id.TV_top_speed);
        current = (TextView) findViewById(R.id.TV_current);

        /**
         * THROTTLE BAR
         */
        throttleBar = (SeekBar) findViewById(R.id.seekBar_throttle);
        throttleBar.setProgress(throttleBar.getMax() / 2);
        throttleBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public synchronized void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        throttleVal = progress - (throttleBar.getMax()/2); //get throttle from -100 to 100
                        if(throttleVal>=0) {
                            sendVal = ((float)throttleVal/100.0f)*60f;
                            Log.d("TAG", sendVal + "");
                        }
                        else{
                            sendVal = ((float)throttleVal/100.0f)*40f;
                            Log.d("TAG", sendVal + "");
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        sendEnable = true;
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        seekBar.setProgress(seekBar.getMax() / 2);
                        sendEnable = false;
                    }
                });
        /**
         * timer tasks
         */
        readValTimerTask = new TimerTask() {

            @Override
            public void run() {
                if(Board_Activity.mBluetoothConnection.isConnected()) {
                    PacketTools.vescUartGetValue(PacketTools.COMM_PACKET_ID.COMM_GET_VALUES);
                    runOnUiThread(new Runnable(){

                        @Override
                        public void run(){
                            // update ui here
                            updateValues(Board_Activity.values);
                        }
                    });

                }
            }
        };
        sendValTimerTask = new TimerTask() {

            @Override
            public void run() {
                if(Board_Activity.mBluetoothConnection.isConnected()) {
                    PacketTools.vescUartSetValue(sendVal, PacketTools.COMM_PACKET_ID.COMM_SET_CURRENT);
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(Board_Activity.mBluetoothConnection.isConnected())
            initializeTimers();
    }

    @Override
    protected void onPause(){
        super.onPause();
        cancelTimers();
        saveLoggedData();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        cancelTimers();
        saveLoggedData();
    }

    //-------------------- functions and stuff -----------------------------------------------------
    public static void updateValues(PacketTools.mc_values values) {
        //battery indicator
       if(batteryLevel==100)
           batteryIndicatorImage.setImageResource(R.drawable.ic_battery_full_24dp);
        else if (batteryLevel > 90)
            batteryIndicatorImage.setImageResource(R.drawable.ic_battery_90_24dp);
       else if (batteryLevel >80)
           batteryIndicatorImage.setImageResource(R.drawable.ic_battery_80_24dp);
       else if (batteryLevel > 60)
           batteryIndicatorImage.setImageResource(R.drawable.ic_battery_60_24dp);
       else if (batteryLevel > 50)
           batteryIndicatorImage.setImageResource(R.drawable.ic_battery_50_24dp);
       else if (batteryLevel > 30)
           batteryIndicatorImage.setImageResource(R.drawable.ic_battery_30_24dp);
       else if (batteryLevel > 20)
           batteryIndicatorImage.setImageResource(R.drawable.ic_battery_20_24dp);
       else
           batteryIndicatorImage.setImageResource(R.drawable.ic_battery_alert_24dp);

       batteryText.setText(batteryLevel+"");
       //speed indicator
        speed = values.rpm/(float)Board_Activity.currentProfile.getMotorPoles(); //motor rpm
//        speed = speed * 15f/36f; //wheel rpm
//        speed = speed * 2*3.14159265354f / 60;// wheel rad/sec
//        speed = speed * 0.083f; // speed in M/S
        speed *= Board_Activity.currentProfile.getwheelRatio(); //speed in M/S
        speed = speed * 3.6f; //speed in KM/H
        speedText.setText(String.format("%.1f KM/H", speed));

        //Upper-right text indicators
        if(speed > topSpeedVal && values.current_in > 7) {
            topSpeedVal = speed;
            topSpeed.setText(String.format("TOP SPEED:   %.1f KM/H", topSpeedVal));
        }
        if(first){
            distanceOffset = values.tachometer_abs;
        }
        distanceVal = (float) ((values.tachometer_abs-distanceOffset)*Board_Activity.currentProfile.getwheelRatio());
        distance.setText(String.format("DISTANCE:     %.1f M",distanceVal));
        currentVal = values.current_in;
        current.setText(String.format("CURRENT:       %.2f A", currentVal));

        if(speed > 1){ //threshold so we don't average all time where we aren't moving
            float time = (distanceVal - distancePrev)/speed;
            timeSum += time;
            avgSpeedVal = (float) (distanceVal/timeSum);
        }
        distancePrev = distanceVal;
        first = false;
    }
    private static void initializeTimers(){
        try {
            sendValTimer = new Timer("setValues_Timer");
            sendValTimer.scheduleAtFixedRate(sendValTimerTask, 50, 100);
            readValTimer = new Timer("getValues_Timer");
            readValTimer.scheduleAtFixedRate(readValTimerTask, 50, 100);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private static void cancelTimers(){
        try {
            sendValTimer.cancel();
            readValTimer.cancel();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void saveLoggedData(){
        double totalAvgSpeed = Board_Activity.currentProfile.getAvgSpeed();
        double totalDistance = Board_Activity.currentProfile.getTotalDist();
        double totalTopSpeed = Board_Activity.currentProfile.getMaxSpeed();

        double totalTime;
        if(totalAvgSpeed!=0) {
            totalTime = totalDistance / totalAvgSpeed;
        }
        else{
            totalTime  = 0;
        }
        totalDistance += distanceVal;
        totalTime += timeSum;
        totalAvgSpeed = totalDistance/totalTime;
        if(topSpeedVal > totalTopSpeed)
            totalTopSpeed = topSpeedVal;

        BoardProfile profile = new BoardProfile();
        profile.setAvgSpeed(totalAvgSpeed);
        profile.setTotalDist(totalDistance);
        profile.setMaxSpeed(totalTopSpeed);
        updateProfile(profile);
        topSpeedVal = 0;
        speed = 0;
        avgSpeedVal = 0;
        distanceVal = 0;
        distancePrev = 0;
        distanceOffset = 0;
        first = true;
        timeSum = 0;
    }
    public void updateProfile(BoardProfile bp) {
        List<BoardProfile> load = SaveState.readFromFile(getApplicationContext());
        load.remove(currentProfile);
        currentProfile.setTotalDist(bp.getTotalDist());
        currentProfile.setMaxSpeed(bp.getMaxSpeed());
        currentProfile.setAvgSpeed(bp.getAvgSpeed());
        load.add(0, currentProfile);
        SaveState.bp = load;
        SaveState.saveToFile(getApplicationContext());
    }

        private class ExceptionHandler implements
            Thread.UncaughtExceptionHandler {
        private Activity myContext=null;
        private Thread.UncaughtExceptionHandler defaultHandler;

        public ExceptionHandler(Activity context) {
            this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
            myContext = context;
        }

        public void uncaughtException(Thread thread, Throwable exception) {
            Control_Activity.cancelTimers();
//            Control_Activity.saveLoggedData();
            Log.d("", "Control Activity: uncaught exception!");
//            android.os.Process.killProcess(android.os.Process.myPid());
//            System.exit(10);
            defaultHandler.uncaughtException(thread, exception);
        }
    }
    //----------------------------------------------------------------------------------------------
}
