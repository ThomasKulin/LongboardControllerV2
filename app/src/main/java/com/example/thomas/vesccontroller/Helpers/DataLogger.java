package com.example.thomas.vesccontroller.Helpers;

/**
 * Created by Thomas on 2022-05-19.
 */
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.example.thomas.vesccontroller.Activities.Control_Activity;
import com.example.thomas.vesccontroller.Helpers.Communications.PacketTools;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.security.PrivilegedAction;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class DataLogger {

    public static ArrayList<String[]> log = new ArrayList<String[]>();

    public static void logData(PacketTools.mc_values values, PacketTools.mc_values values2, PacketTools.mc_states states, Control_Activity.commands command, Control_Activity.control control){
        long time = System.currentTimeMillis();
        log_values data = new log_values(time, values, values2, states, command, control);
        log.add(data.toArray());
    }
    public static void saveLogToFile(Context context) throws IOException {
        System.out.println("Saving log to file...");

        String fileSuffix = new SimpleDateFormat("yyyyMMddHHmm", Locale.getDefault()).format(new Date());
        String fileName = "LogData_" + fileSuffix + ".csv";

        // Use getExternalFilesDir for app-specific external storage, no permissions needed for API level 19 and above
        File baseDir = context.getExternalFilesDir(null);
        if (baseDir == null) {
            throw new IOException("External Storage is not mounted or not available.");
        }
        String filePath = baseDir.getAbsolutePath() + File.separator;

        File path = new File(filePath);
        if (!path.exists() && !path.mkdirs()) {
            throw new IOException("Failed to create directory path: " + filePath);
        }

        File file = new File(filePath + fileName);
        CSVWriter writer = new CSVWriter(new FileWriter(file));
        String[] header = {"timestamp", "throttle", "control enabled", "control strength", "psi limit", "theta limit", "fx", "fy", "commonMode", "differential", "currentLeftMotor", "currentRightMotor", "v_in", "temp_mos", "current_motor", "current_in", "rpm", "duty_now", "tachometer", "tachometer_abs", "v_in_2", "temp_mos_2", "current_motor_2", "current_in_2", "rpm_2", "duty_now_2", "tachometer_2", "tachometer_abs_2", "phi", "phi_dot", "psi", "psi_dot", "theta", "theta_dot", "hdg", "hdg_dot"};
        writer.writeNext(header);

        List<String[]> toWrite = new ArrayList<>();
        synchronized (log) {
            for (String[] entry : log) {
                toWrite.add(entry);
            }
        }

        // Assuming 'log' is an accessible variable containing the data
        for (String[] entry : toWrite) {
            writer.writeNext(entry);
        }
        writer.close();
        System.out.println("File saved to: " + file.getAbsolutePath());
    }

    public static class log_values{
        public String timestamp;
        public String throttle;
        public String controlEnabled;
        public String controlStrength;
        public String psiLimit;
        public String thetaLimit;
        public String fx;
        public String fy;
        public String commonMode;
        public String differential;
        public String currentLeftMotor;
        public String currentRightMotor;
        public String v_in;
        public String temp_mos;
        public String current_motor;
        public String current_in;
        public String rpm;
        public String duty_now;
        public String tachometer;
        public String tachometer_abs;
        public String v_in_2;
        public String temp_mos_2;
        public String current_motor_2;
        public String current_in_2;
        public String rpm_2;
        public String duty_now_2;
        public String tachometer_2;
        public String tachometer_abs_2;
        public String phi;
        public String phi_dot;
        public String psi;
        public String psi_dot;
        public String theta;
        public String theta_dot;
        public String hdg;
        public String hdg_dot;

        public log_values(long time, PacketTools.mc_values values, PacketTools.mc_values values2, PacketTools.mc_states state, Control_Activity.commands command, Control_Activity.control control){
            timestamp = Long.toString(time);

            if (command != null) {
                throttle = Float.toString(command.commonModeCMD);
                currentLeftMotor = Float.toString(command.currentLeftMotor);
                currentRightMotor = Float.toString(command.currentRightMotor);
            }
            else{
                throttle = "";
                currentLeftMotor = "";
                currentRightMotor = "";
            }

            if (control != null) {
                controlEnabled = Boolean.toString(control.controlEnabled);
                controlStrength = Float.toString(control.controlStrength);
                psiLimit = Float.toString(control.psiLimit);
                thetaLimit = Float.toString(control.thetaLimit);
                commonMode = Float.toString(control.commonModeCtrl);
                differential = Float.toString(control.differentialCtrl);
                fx = Float.toString(control.fx);
                fy = Float.toString(control.fy);
            }
            else{
                controlEnabled = "";
                controlStrength = "";
                psiLimit = "";
                thetaLimit = "";
                commonMode = "";
                differential = "";
                fx = "";
                fy = "";
            }

            if (values != null) {
                v_in = Float.toString(values.v_in);
                temp_mos = Float.toString(values.temp_mos);
                current_motor = Float.toString(values.current_motor);
                current_in = Float.toString(values.current_in);
                rpm = Float.toString(values.rpm);
                duty_now = Float.toString(values.duty_now);
                tachometer = Integer.toString(values.tachometer);
                tachometer_abs = Integer.toString(values.tachometer_abs);
            }
            else{
                v_in = "";
                temp_mos =  "";
                current_motor =  "";
                current_in =  "";
                rpm =  "";
                duty_now =  "";
                tachometer =  "";
                tachometer_abs =  "";
            }

            if (values2 != null) {
                v_in_2 = Float.toString(values2.v_in);
                temp_mos_2 = Float.toString(values2.temp_mos);
                current_motor_2 = Float.toString(values2.current_motor);
                current_in_2 = Float.toString(values2.current_in);
                rpm_2 = Float.toString(values2.rpm);
                duty_now_2 = Float.toString(values2.duty_now);
                tachometer_2 = Integer.toString(values2.tachometer);
                tachometer_abs_2 = Integer.toString(values2.tachometer_abs);
            }
            else{
                v_in_2 = "";
                temp_mos_2 =  "";
                current_motor_2 =  "";
                current_in_2 =  "";
                rpm_2 =  "";
                duty_now_2 =  "";
                tachometer_2 =  "";
                tachometer_abs_2 =  "";
            }

            if (state != null) {
                phi = Float.toString(state.phi);
                phi_dot = Float.toString(state.phi_dot);
                psi = Float.toString(state.psi);
                psi_dot = Float.toString(state.psi_dot);
                theta = Float.toString(state.theta);
                theta_dot = Float.toString(state.theta_dot);
                hdg = Float.toString(state.hdg);
                hdg_dot = Float.toString(state.hdg_dot);
            }
            else{
                phi = "";
                phi_dot = "";
                psi = "";
                psi_dot = "";
                theta = "";
                theta_dot = "";
                hdg = "";
                hdg_dot = "";
            }

        }

        public String[] toArray(){
            String[] arr = {timestamp, throttle, controlEnabled, controlStrength, psiLimit, thetaLimit, fx, fy, commonMode, differential, currentLeftMotor, currentRightMotor, v_in, temp_mos, current_motor, current_in, rpm, duty_now, tachometer, tachometer_abs, v_in_2, temp_mos_2, current_motor_2, current_in_2, rpm_2, duty_now_2, tachometer_2, tachometer_abs_2, phi, phi_dot, psi, psi_dot, theta, theta_dot, hdg, hdg_dot};
            return arr;
        }
    }

}
