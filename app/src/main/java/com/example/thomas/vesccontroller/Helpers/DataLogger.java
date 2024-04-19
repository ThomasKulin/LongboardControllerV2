package com.example.thomas.vesccontroller.Helpers;

/**
 * Created by Thomas on 2022-05-19.
 */
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.example.thomas.vesccontroller.Helpers.Communications.PacketTools;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.PrivilegedAction;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class DataLogger {

    public static ArrayList<String[]> log = new ArrayList<String[]>();

    public static void logData(PacketTools.mc_values values){
        log_values data = new log_values(values);
        log.add(data.toArray());

    }
    public static void saveLogToFile() throws IOException {
        String fileSuffix = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
        String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        String fileName = "LogData_"+fileSuffix+".csv";
        String filePath = baseDir + File.separator + "VESCData" + File.separator;
        File path = new File(filePath);
        if(!path.exists()) {
            try {
                path.mkdirs();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        CSVWriter writer = new CSVWriter(new FileWriter(filePath+fileName));
        String[] header = {"v_in", "temp_mos", "current_motor", "current_in", "rpm", "duty_now", "tachometer", "tachometer_abs"};
        writer.writeNext(header);
        for (String[] i : log) {
            writer.writeNext(i);
        }
        writer.close();
    }

    public static class log_values{
        public String v_in;
        public String temp_mos;
        public String current_motor;
        public String current_in;
        public String rpm;
        public String duty_now;
        public String tachometer;
        public String tachometer_abs;

        public log_values(PacketTools.mc_values values){
            v_in = Float.toString(values.v_in);
            temp_mos = Float.toString(values.temp_mos);
            current_motor = Float.toString(values.current_motor);
            current_in = Float.toString(values.current_in);
            rpm = Float.toString(values.rpm);
            duty_now = Float.toString(values.duty_now);
            tachometer = Integer.toString(values.tachometer);
            tachometer_abs = Integer.toString(values.tachometer_abs);
        }

        public String toCsvRow() {

            return Stream.of(v_in, temp_mos, current_motor, current_in, rpm, duty_now, tachometer, tachometer_abs)
                    .map(value -> value.replaceAll("\"", "\"\""))
                    .map(value -> Stream.of("\"", ",").anyMatch(value::contains) ? "\"" + value + "\"" : value)
                    .collect(Collectors.joining(","));
        }
        public String[] toArray(){
            String[] arr = {v_in, temp_mos, current_motor, current_in, rpm, duty_now, tachometer, tachometer_abs};
            return arr;
        }
    }

}
