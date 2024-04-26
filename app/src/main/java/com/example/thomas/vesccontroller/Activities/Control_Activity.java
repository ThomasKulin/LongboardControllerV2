package com.example.thomas.vesccontroller.Activities;

import android.app.Activity;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.thomas.vesccontroller.Helpers.BoardProfile;
import com.example.thomas.vesccontroller.Helpers.Communications.PacketTools;
import com.example.thomas.vesccontroller.Helpers.DataLogger;
import com.example.thomas.vesccontroller.Helpers.SaveState;
import com.example.thomas.vesccontroller.R;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.thomas.vesccontroller.Activities.Board_Activity.VescSelect;
import static com.example.thomas.vesccontroller.Activities.Board_Activity.batteryLevel;
import static com.example.thomas.vesccontroller.Activities.Board_Activity.currentProfile;
import static com.example.thomas.vesccontroller.Activities.Board_Activity.values2;
//import static com.example.thomas.vesccontroller.Activities.Board_Activity.;

import io.github.controlwear.virtual.joystick.android.JoystickView;


/**
 * Created by Thomas on 2018-03-25.
 */

public class Control_Activity extends AppCompatActivity {

    boolean sendEnable = false;
    static boolean first = true;
    int throttleVal = 0;
    float sendVal = 0f;
    float commonMode = 0;
    float differential = 0;
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

    // TextViews for sensor data
    static TextView phiValue;
    static TextView phiDotValue;
    static TextView psiValue;
    static TextView psiDotValue;
    static TextView thetaValue;
    static TextView thetaDotValue;
    static TextView hdgValue;
    static TextView hdgDotValue;

    static TextView commonModeTextView;
    static TextView differentialTextView;
    static EditText cmLimitValue;
    static EditText diffLimitValue;
    private int cmLimit = 40; // Initialize with default value
    private int diffLimit = 20; // Initialize with default value


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

        commonModeTextView = (TextView) findViewById(R.id.textViewCommonMode);
        differentialTextView = (TextView) findViewById(R.id.textViewDifferential);
        cmLimitValue = (EditText) findViewById(R.id.CM_limit_value);
        diffLimitValue = (EditText) findViewById(R.id.Diff_limit_value);

        phiValue = (TextView) findViewById(R.id.phi_value);
        phiDotValue = (TextView) findViewById(R.id.phi_dot_value);
        psiValue = (TextView) findViewById(R.id.psi_value);
        psiDotValue = (TextView) findViewById(R.id.psi_dot_value);
        thetaValue = (TextView) findViewById(R.id.theta_value);
        thetaDotValue = (TextView) findViewById(R.id.theta_dot_value);
        hdgValue = (TextView) findViewById(R.id.hdg_value);
        hdgDotValue = (TextView) findViewById(R.id.hdg_dot_value);


        /**
         * THROTTLE BAR
         */
//        throttleBar = (SeekBar) findViewById(R.id.seekBar_throttle);
//        throttleBar.setProgress(throttleBar.getMax() / 2);
//        throttleBar.setOnSeekBarChangeListener(
//                new SeekBar.OnSeekBarChangeListener() {
//
//                    @Override
//                    public synchronized void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                        throttleVal = progress - (throttleBar.getMax()/2); //get throttle from -100 to 100
//                        if(throttleVal<0){
//                            throttleVal = -1* (int) (Math.pow(throttleVal, 2)/100);
//                        }
//                        else{
//                            throttleVal = (int) (Math.pow(throttleVal, 2)/100);
//                        }
//
//                        if(throttleVal>=0) {
//                            sendVal = ((float)throttleVal/100.0f)*40f;
//                            Log.d("TAG", sendVal + "");
//                        }
//                        else{
//                            sendVal = ((float)throttleVal/100.0f)*40f;
//                            Log.d("TAG", sendVal + "");
//                        }
//                    }
//
//                    @Override
//                    public void onStartTrackingTouch(SeekBar seekBar) {
//                        sendEnable = true;
//                    }
//
//                    @Override
//                    public void onStopTrackingTouch(SeekBar seekBar) {
//                        seekBar.setProgress(seekBar.getMax() / 2);
//                        sendEnable = false;
//                    }
//                });

        JoystickView joystickView = (JoystickView) findViewById(R.id.joystickView);
        joystickView.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                // Convert to radians for calculations
                double angleRadians = Math.toRadians(angle);

                // Cartesian coordinates
                double x = strength * Math.cos(angleRadians);
                double y = strength * Math.sin(angleRadians);

                // Use as common mode and differential
                if(y<0){
                    commonMode = -1* (float) (Math.pow(y, 2)/10000)*cmLimit;
                    Log.d("TAG", commonMode + "");
                }
                else{
                    commonMode = (float) (Math.pow(y, 2)/10000)*cmLimit;
                    Log.d("TAG", commonMode + "");
                }
                if(x<0){
                    differential = -1* (float) (Math.pow(x, 2)/10000)*diffLimit;
                    Log.d("TAG", differential + "");
                }
                else{
                    differential = (float) (Math.pow(x, 2)/10000)*diffLimit;
                    Log.d("TAG", differential + "");
                }

                // Update the text views
                commonModeTextView.setText("Common Mode: " + commonMode);
                differentialTextView.setText("Differential: " + differential);
            }
        });

        cmLimitValue.setText(String.valueOf(cmLimit));
        diffLimitValue.setText(String.valueOf(diffLimit));

        // Setup TextWatcher for CM Limit EditText
        cmLimitValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Code here will execute before the text is changed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Code here executes as the user is typing
                // For example, validating input or enabling/disabling a button based on the input
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    cmLimit = Integer.parseInt(s.toString());
                } catch (NumberFormatException e) {
                    cmLimit = 40; // Default value or handle error
                }
            }
        });

        // Setup TextWatcher for Diff Limit EditText
        diffLimitValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Optionally handle actions before text changes
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Handle text changes in real-time
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Update variable after text changed
                try {
                    diffLimit = Integer.parseInt(s.toString());
                } catch (NumberFormatException e) {
                    diffLimit = 20; // Default value or handle error
                }
            }
        });

        /**
         * timer tasks
         */
        readValTimerTask = new TimerTask() {

            @Override
            public void run() {
                if(Board_Activity.mBluetoothConnection.isConnected()) {
                    PacketTools.vescUartGetValue(PacketTools.COMM_PACKET_ID.COMM_GET_VALUES); //Master

                    //PacketTools.vescUartGetValue(PacketTools.COMM_PACKET_ID.COMM_FORWARD_CAN); //Slave

                    runOnUiThread(new Runnable(){

                        @Override
                        public void run(){
                            // update ui here
                            updateValues(Board_Activity.values);
                            updateStates(Board_Activity.states);
                        }
                    });

                }
            }
        };
        sendValTimerTask = new TimerTask() {

            @Override
            public void run() {
                if(Board_Activity.mBluetoothConnection.isConnected()) {
                    float currentLeftMotor = commonMode + differential;
                    float currentRightMotor = commonMode - differential;

                    PacketTools.vescUartSetValue(currentRightMotor, PacketTools.COMM_PACKET_ID.COMM_SET_CURRENT); //Master
                    PacketTools.vescUartSetValue(currentLeftMotor, PacketTools.COMM_PACKET_ID.COMM_FORWARD_CAN); //Slave
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
        updateBoardStats();
        try {
            DataLogger.saveLogToFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        cancelTimers();
        updateBoardStats();
        try {
            DataLogger.saveLogToFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //-------------------- functions and stuff -----------------------------------------------------
    public static void updateValues(PacketTools.mc_values values) {
        float current_in = values.current_in*2; //This is terrible but I need to define a new COMM protocol to send params from both VESC properly
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
        if(speed > topSpeedVal && current_in > 4) {
            topSpeedVal = speed;
            topSpeed.setText(String.format("TOP SPEED:   %.1f KM/H", topSpeedVal));
        }
        if(first){
            distanceOffset = values.tachometer_abs;
        }
        distanceVal = (float) ((values.tachometer_abs-distanceOffset)*Board_Activity.currentProfile.getwheelRatio());
        distance.setText(String.format("DISTANCE:     %.1f M",distanceVal));
        currentVal = current_in;
        current.setText(String.format("CURRENT:       %.2f A", currentVal));

        if(speed > 1){ //threshold so we don't average all time where we aren't moving
            float time = (distanceVal - distancePrev)/speed;
            timeSum += time;
            avgSpeedVal = (float) (distanceVal/timeSum);
        }
        distancePrev = distanceVal;
        first = false;
    }
    public static void updateStates(PacketTools.mc_states states) {
        phiValue.setText(""+states.phi);
        phiDotValue.setText(""+states.phi_dot);
        psiValue.setText(""+states.psi);
        psiDotValue.setText(""+states.psi_dot);
        thetaValue.setText(""+states.theta);
        thetaDotValue.setText(""+states.theta_dot);
        hdgValue.setText(""+states.hdg);
        hdgDotValue.setText(""+states.hdg_dot);
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
    private void updateBoardStats(){
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
//            Control_Activity.updateBoardStats();
            Log.d("", "Control Activity: uncaught exception!");
//            android.os.Process.killProcess(android.os.Process.myPid());
//            System.exit(10);
            defaultHandler.uncaughtException(thread, exception);
        }
    }
    //----------------------------------------------------------------------------------------------
}
