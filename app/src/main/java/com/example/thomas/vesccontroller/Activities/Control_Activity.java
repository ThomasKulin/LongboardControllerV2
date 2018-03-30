package com.example.thomas.vesccontroller.Activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.SeekBar;

import com.example.thomas.vesccontroller.Helpers.Communications.PacketTools;
import com.example.thomas.vesccontroller.R;

import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by Thomas on 2018-03-25.
 */

public class Control_Activity extends AppCompatActivity {

    boolean sendEnable = false;
    int throttleVal = 0;
    int sendVal = 0;
    int motorMaxCurrent = 60;
    int motorMinCurrent = -40;

    SeekBar throttleBar;
    private static TimerTask readValTimerTask;
    private static TimerTask sendValTimerTask;
    private static Timer readValTimer;
    private static Timer sendValTimer;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.control_activity);

        /**
         * ACTION BAR
         */
        Toolbar myToolbar = (Toolbar) findViewById(R.id.controller_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle(Board_Activity.currentProfile.getName()); //sets the name displayed at the top of the app

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
                            sendVal = (int)(((float)throttleVal/100.0)*60);
                        }
                        else{
                            sendVal = (int)(((float)throttleVal/100.0)*-40);
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
                if(Board_Activity.connected) {
                    PacketTools.vescUartGetValue();
                }
            }
        };
        sendValTimerTask = new TimerTask() {

            @Override
            public void run() {
                if(Board_Activity.connected) {
                    PacketTools.vescUartSetValue(sendVal, PacketTools.COMM_PACKET_ID.COMM_SET_CURRENT);
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(Board_Activity.connected)
            initializeTimers();
    }

    @Override
    protected void onPause(){
        super.onPause();
        cancelTimers();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        cancelTimers();
    }

    //-------------------- functions and stuff -----------------------------------------------------

    private static void initializeTimers(){
        try {
            sendValTimer = new Timer("setValues_Timer");
            sendValTimer.scheduleAtFixedRate(sendValTimerTask, 50, 100);
            readValTimer = new Timer("getValues_Timer");
            readValTimer.scheduleAtFixedRate(readValTimerTask, 50, 500);
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

    //----------------------------------------------------------------------------------------------
}
