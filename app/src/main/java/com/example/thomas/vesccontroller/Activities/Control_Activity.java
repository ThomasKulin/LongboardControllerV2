package com.example.thomas.vesccontroller.Activities;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.thomas.vesccontroller.Helpers.BoardProfile;
import com.example.thomas.vesccontroller.Helpers.Communications.PacketTools;
import com.example.thomas.vesccontroller.Helpers.SaveState;
import com.example.thomas.vesccontroller.R;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.thomas.vesccontroller.Activities.Board_Activity.batteryLevel;
import static com.example.thomas.vesccontroller.Activities.Board_Activity.currentProfile;
//import static com.example.thomas.vesccontroller.Activities.Board_Activity.;

import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.log10;
import static java.lang.Math.signum;
import static java.lang.Math.sin;


/**
 * Created by Thomas on 2018-03-25.
 */

public class Control_Activity extends AppCompatActivity {

    private static final String TAG = "ControlActivity";

    static boolean first = true;
    int throttleVal = 0;
    float sendVal = 0f;
    public static commands command = new commands();
    public static control control = new control();
    static float currentVal = 0f;
    int motorMaxCurrent = 40;

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
    static TextView fxValue;
    static TextView fyValue;

    static TextView commonModeTextView;
    static TextView differentialTextView;
    static TextView commonModeCtrlText;
    static TextView differentialCtrlText;
    static EditText psiLimitValue;
    static EditText thetaLimitValue;
    static EditText controlStrengthValue;
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
        commonModeCtrlText = (TextView) findViewById(R.id.textViewCommonModeCtrl);
        differentialCtrlText = (TextView) findViewById(R.id.textViewDifferentialCtrl);
        psiLimitValue = (EditText) findViewById(R.id.psi_limit_value);
        thetaLimitValue = (EditText) findViewById(R.id.theta_limit_value);
        controlStrengthValue = (EditText) findViewById(R.id.control_strength_value);

        phiValue = (TextView) findViewById(R.id.phi_value);
        phiDotValue = (TextView) findViewById(R.id.phi_dot_value);
        psiValue = (TextView) findViewById(R.id.psi_value);
        psiDotValue = (TextView) findViewById(R.id.psi_dot_value);
        thetaValue = (TextView) findViewById(R.id.theta_value);
        thetaDotValue = (TextView) findViewById(R.id.theta_dot_value);
        hdgValue = (TextView) findViewById(R.id.hdg_value);
        hdgDotValue = (TextView) findViewById(R.id.hdg_dot_value);
        fxValue = (TextView) findViewById(R.id.fx_value);
        fyValue = (TextView) findViewById(R.id.fy_value);


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
                        if(throttleVal<0){
                            throttleVal = -1* (int) (Math.pow(throttleVal, 2)/100);
                        }
                        else{
                            throttleVal = (int) (Math.pow(throttleVal, 2)/100);
                        }

                        if(throttleVal>=0) {
                            command.commonModeCMD = ((float)throttleVal/100.0f)*40f;
                            Log.d("TAG", command.commonModeCMD + "");
                        }
                        else{
                            command.commonModeCMD = ((float)throttleVal/100.0f)*40f;
                            Log.d("TAG", command.commonModeCMD + "");
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        seekBar.setProgress(seekBar.getMax() / 2);
                    }
                });

//        JoystickView joystickView = (JoystickView) findViewById(R.id.joystickView);
//        joystickView.setOnMoveListener(new JoystickView.OnMoveListener() {
//            @Override
//            public void onMove(int angle, int strength) {
//                // Convert to radians for calculations
//                double angleRadians = Math.toRadians(angle);
//
//                // Cartesian coordinates
//                double x = strength * cos(angleRadians);
//                double y = strength * sin(angleRadians);
//
//                // Use as common mode and differential
//                if(y<0){
//                    command.commonModeCMD = -1* (float) (Math.pow(y, 2)/10000)*cmLimit;
//                    Log.d("TAG", command.commonModeCMD + "");
//                }
//                else{
//                    command.commonModeCMD = (float) (Math.pow(y, 2)/10000)*cmLimit;
//                    Log.d("TAG", command.commonModeCMD + "");
//                }
//                if(x<0){
//                    command.differentialCMD = -1* (float) (Math.pow(x, 2)/10000)*diffLimit;
//                    Log.d("TAG", command.differentialCMD + "");
//                }
//                else{
//                    command.differentialCMD = (float) (Math.pow(x, 2)/10000)*diffLimit;
//                    Log.d("TAG", command.differentialCMD + "");
//                }
//
//                // Update the text views
//                commonModeTextView.setText("Common Mode: " + command.commonModeCMD);
//                differentialTextView.setText("Differential: " + command.differentialCMD);
//            }
//        });

        /**
         * TUNING PARAMETERS
         */
        psiLimitValue.setText(String.valueOf(control.psiLimit));
        thetaLimitValue.setText(String.valueOf(control.thetaLimit));
        controlStrengthValue.setText(String.valueOf(control.controlStrength));

        psiLimitValue.addTextChangedListener(new TextWatcher() {
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
                    control.psiLimit = Integer.parseInt(s.toString());
                } catch (NumberFormatException e) {
                    control.psiLimit = 20; // Default value or handle error
                }
            }
        });

        thetaLimitValue.addTextChangedListener(new TextWatcher() {
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
                    control.thetaLimit = Integer.parseInt(s.toString());
                } catch (NumberFormatException e) {
                    control.thetaLimit = 20; // Default value or handle error
                }
            }
        });

       controlStrengthValue.addTextChangedListener(new TextWatcher() {
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
                    control.controlStrength = Integer.parseInt(s.toString());
                } catch (NumberFormatException e) {
                    control.controlStrength = 0; // Default value or handle error
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
                    if (Board_Activity.VescSelect==1){PacketTools.vescUartGetValue(PacketTools.COMM_PACKET_ID.COMM_GET_VALUES);} //Master
                    else if(Board_Activity.VescSelect==0){PacketTools.vescUartGetValue(PacketTools.COMM_PACKET_ID.COMM_FORWARD_CAN);} //Slave

//                    PacketTools.vescUartGetValue(PacketTools.COMM_PACKET_ID.COMM_GET_VALUES);
//                    PacketTools.vescUartGetValue(PacketTools.COMM_PACKET_ID.COMM_FORWARD_CAN);
                    runOnUiThread(new Runnable(){

                        @Override
                        public void run(){
                            // update ui here
                            updateValues(Board_Activity.values);
                            updateStates(Board_Activity.states);
                            calculateControl(Board_Activity.values, Board_Activity.states);
                        }
                    });

                }
            }
        };
        sendValTimerTask = new TimerTask() {

            @Override
            public void run() {
                if(Board_Activity.mBluetoothConnection.isConnected()) {
                    command.currentLeftMotor = command.commonModeCMD;
                    command.currentRightMotor = command.commonModeCMD;

                    if (control.controlEnabled){
                        command.currentLeftMotor += control.commonModeCtrl + control.differentialCtrl;
                        command.currentRightMotor += control.commonModeCtrl - control.differentialCtrl;
                    }
                    if(command.currentLeftMotor > 40){command.currentLeftMotor = 40;}
                    if(command.currentRightMotor > 40){command.currentRightMotor = 40;}
                    if(command.currentLeftMotor < -40){command.currentLeftMotor = -40;}
                    if(command.currentRightMotor < -40){command.currentRightMotor = -40;}

                    PacketTools.vescUartSetValue(command.currentRightMotor, PacketTools.COMM_PACKET_ID.COMM_SET_CURRENT); //Master
                    PacketTools.vescUartSetValue(command.currentLeftMotor, PacketTools.COMM_PACKET_ID.COMM_FORWARD_CAN); //Slave

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
            Board_Activity.log.saveLogToFile(this);
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
            Board_Activity.log.saveLogToFile(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            // Handle the event for the volume up button being pressed
            control.controlEnabled = true;
            return true;  // Indicate that you've handled the event
        }
        return super.onKeyDown(keyCode, event);  // Let the system handle all other key events
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            // Handle the event for the volume up button being released
            control.controlEnabled = false;
            return true;  // Indicate that you've handled the event
        }
        return super.onKeyUp(keyCode, event);  // Let the system handle all other key events
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

        // Set speed text and color
        if (control.controlEnabled) {
            commonModeCtrlText.setTextColor(Color.GREEN);
            differentialCtrlText.setTextColor(Color.GREEN);
        } else {
            commonModeCtrlText.setTextColor(Color.RED);
            differentialCtrlText.setTextColor(Color.RED);
        }


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

    public static void calculateControl(PacketTools.mc_values values, PacketTools.mc_states states){
        double x = 0;
        double x_dot = 0;
        double phi = states.phi * 0.01745;  // Convert to radians
        double phi_dot = states.phi_dot * 0.01745;
        double psi = states.psi * 0.01745;
        double psi_dot = states.psi_dot * 0.01745;
        double theta = states.theta * 0.01745;
        double theta_dot = states.theta_dot * 0.01745;

        phi = (double) constrainAbsVal((float) phi, 45* (float)0.01745);
        phi_dot = (double) constrainAbsVal((float) phi_dot, 90* (float)0.01745);
        psi = (double) constrainAbsVal((float) psi, control.psiLimit* (float)0.01745);
        psi_dot = (double) constrainAbsVal((float) psi_dot, 90* (float)0.01745);
        theta = (double) constrainAbsVal((float) theta, control.thetaLimit* (float)0.01745);
        theta_dot = (double) constrainAbsVal((float) theta_dot, 90* (float)0.01745);


        double fx = (-0.5 * (10 * ((36.624520729487024 * x + 0.27861843412132048 * phi + 16510.451050942986 * theta - 3.2004724026257469 * psi + 133.76411335652477 * x_dot - 0.20022883155884946 * phi_dot + 1090.6635460300026 * theta_dot - 0.56388233721281655 * psi_dot) * ((-1 * cos(theta)) / (0.90000000000000002 * (4 + 90 * Math.pow(sin(theta), 2))))) + ((10 * (32.433095947025691 * x + 0.031999224782588702 * phi + 1752.320146257649 * theta - 2.1895584321499855 * psi + 86.359313073578377 * x_dot + 0.12290772267221448 * phi_dot + 133.76411335652477 * theta_dot - 2.6298100506676696 * psi_dot)) / (4 + 90 * Math.pow(sin(theta), 2)))));
        fx = fx/40;
        double fy = (-0.5 * (10 * (( - 0.21704087080691076 * x + 106.78915861262998 * phi - 0.3699874871493381 * theta + 61662.985856405969 * psi - 2.6298100506676696 * x_dot + 8.0362227522587357 * phi_dot - 0.56388233721281655 * theta_dot + 8355.1423483685739 * psi_dot) * (cos(psi) / (72.900000000000006 * Math.pow(cos(theta), 2)))) + ((32.433095947025691 * x + 0.031999224782588702 * phi + 1752.320146257649 * theta - 2.1895584321499855 * psi + 86.359313073578377 * x_dot + 0.12290772267221448 * phi_dot + 133.76411335652477 * theta_dot - 2.6298100506676696 * psi_dot) * ((0 / (4 + 90 * Math.pow(sin(theta), 2))) + 10 * ((sin(theta) * sin(psi) * cos(theta)) / (0.90000000000000002 * (4 + 90 * Math.pow(sin(theta), 2)))))) + 10 * ((36.624520729487024 * x + 0.27861843412132048 * phi + 16510.451050942986 * theta - 3.2004724026257469 * psi + 133.76411335652477 * x_dot - 0.20022883155884946 * phi_dot + 1090.6635460300026 * theta_dot - 0.56388233721281655 * psi_dot) * ((-94 * sin(theta) * sin(psi)) / (72.900000000000006 * (4 + 90 * Math.pow(sin(theta), 2)))))));
        fxValue.setText(""+String.format("%.2f", fx));
        fyValue.setText(""+String.format("%.2f", fy));

        // Board Parameters
        double Nw = 37;  // Wheel pulley teeth
        double Nm = 14;  // Motor pulley teeth
        double rw = 48e-3;  // Wheel radius [m]

        // BLDC Motor Parameters
        double P = 14;  // Number of motor poles
        double Rs = 0.0502;  // Stator resistance per phase           [Ohm]
        double Ls = 47.56e-6;  // Stator inductance      [H]
        double Ke = 1/0.17;  // Motor constant      [volts/1000RPM]
        double psi_m = 60*Ke/(Math.sqrt(3)*3.1415*P*1000);  // Maximum permanent magnet flux linkage [Wb]
        double J = 0.0005;  // Rotor inertia                         [Kg*m^2]
        double Vdc = 48;  // DC link voltage

        // Calculate Motor Commands
        double Tw = fx * rw / 2;  // Wheel torque (dual motor so /2)
        double Te = Nm/Nw*Tw;  // Motor torque from wheel torque
        double Iq = Te/(0.75*P*psi_m);  // Common mode motor current required to achieve fx
        control.commonModeCtrl = (float) Iq * control.controlStrength/100f;

        x_dot = values.rpm/Board_Activity.currentProfile.getMotorPoles() * Board_Activity.currentProfile.getwheelRatio(); //speed in M/S
        double Spd_Kmph = x_dot * 3.6f;
        double turningRateDeg = fy/90f * x_dot;
        control.differentialCtrl = (float) (signum(turningRateDeg) * 180f/Spd_Kmph * log10(abs(turningRateDeg)+1)) * control.controlStrength/100f;

        control.fx = (float) fx;
        control.fy = (float) fy;

        commonModeCtrlText.setText("X Current: " + control.commonModeCtrl);
        differentialCtrlText.setText("Y Current: " + control.differentialCtrl);
        
    }

    static float constrainAbsVal(float value, float constraint){
        float temp = value;
        if(value > constraint){temp = constraint;}
        if(value < -constraint){temp = -constraint;}
        return temp;
    }

    private static void initializeTimers(){
        try {
            sendValTimer = new Timer("setValues_Timer");
            sendValTimer.scheduleAtFixedRate(sendValTimerTask, 0, 50);
            readValTimer = new Timer("getValues_Timer");
            readValTimer.scheduleAtFixedRate(readValTimerTask, 50, 50);
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
    public static class commands {
        public float commonModeCMD;
        public float differentialCMD;
        public float currentLeftMotor;
        public float currentRightMotor;
        public commands(){
            commonModeCMD = 0;
            differentialCMD = 0;
            currentLeftMotor = 0;
            currentRightMotor = 0;
        }
    }
    public static class control {
        public boolean controlEnabled;
        public float controlStrength;
        public float psiLimit;
        public float thetaLimit;

        public float commonModeCtrl;
        public float differentialCtrl;
        public float fx;
        public float fy;
        public control(){
            controlEnabled = false;
            controlStrength = 30;
            psiLimit = 20;
            thetaLimit = 20;
            commonModeCtrl = 0;
            differentialCtrl = 0;
            fx = 0;
            fy = 0;
        }
    }
}
