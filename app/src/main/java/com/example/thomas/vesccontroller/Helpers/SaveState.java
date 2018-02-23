package com.example.thomas.vesccontroller.Helpers;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Thomas on 2017-12-28.
 */

public class SaveState implements Serializable{
    static public List<BoardProfile> bp = new ArrayList<BoardProfile>();
    static SaveState instance = null;

    public static SaveState getInstance(){
        if(instance==null)
            instance = new SaveState();
        return instance;
    }

    public static void saveData(SaveState instance, Context mContext){
        ObjectOutput out;
        try {
            File outFile = new File(mContext.getFilesDir(),
                    "someRandom.data");
            out = new ObjectOutputStream(new FileOutputStream(outFile));
            out.writeObject(instance);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static SaveState loadData(Context mContext){
        ObjectInput in;
        SaveState ss=null;
        try {
            String filePath = mContext.getFilesDir() + "/" + "someRandom.data";
            File inFile = new File(filePath);
            in = new ObjectInputStream(new FileInputStream(inFile));
            ss = (SaveState) in.readObject();
            in.close();
        } catch (Exception e) {e.printStackTrace();}
        return ss;
    }
}
