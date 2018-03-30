package com.example.thomas.vesccontroller.Helpers;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.example.thomas.vesccontroller.Activities.Board_Activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static android.content.ContentValues.TAG;

/**
 * Created by Thomas on 2017-12-28.
 */

public class SaveState implements Serializable {
    public static List<BoardProfile> bp = new ArrayList<BoardProfile>();

    public static void saveToFile(Context context) {
        try {
            FileOutputStream fileOutputStream = context.openFileOutput("testsave3.vsc", Context.MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(bp);
            objectOutputStream.close();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Creates an object by reading it from a file
    public static List<BoardProfile> readFromFile(Context context) {
        List<BoardProfile> boardProfileList = null;
        try {
            FileInputStream fileInputStream = context.openFileInput("testsave3.vsc");
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            boardProfileList = (List<BoardProfile>) objectInputStream.readObject();
            objectInputStream.close();
            fileInputStream.close();
        } catch (Exception e) {
            Log.e(TAG, "readFromFile: error reading from file");
        }
        if(boardProfileList!=null)
            bp=boardProfileList;
        return boardProfileList;
    }
    public static BluetoothDevice getDevice(String deviceAddress) {
        if (deviceAddress.equals("00:00:00:00:00:00")){
            return null;
        }
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if(!btAdapter.isEnabled())
            btAdapter.enable();

        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();

        for(BluetoothDevice d : pairedDevices)
            if(d.getAddress().equalsIgnoreCase(deviceAddress))
                return d;

        return null;
    }
}
