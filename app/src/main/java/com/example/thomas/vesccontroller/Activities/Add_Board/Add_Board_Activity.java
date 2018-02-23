package com.example.thomas.vesccontroller.Activities.Add_Board;

import android.app.LauncherActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.thomas.vesccontroller.Helpers.BoardProfile;
import com.example.thomas.vesccontroller.Helpers.SaveState;
import com.example.thomas.vesccontroller.Helpers.listItem;
import com.example.thomas.vesccontroller.R;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by Thomas on 2017-12-29.
 */

public class Add_Board_Activity  extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    List<listItem> params = new ArrayList<listItem>();
    short batteryType;
    BluetoothDevice btDevice;

    BluetoothAdapter btAdapter;
    Set<BluetoothDevice> devicesArray;
    List<String> pairedDevices;
    ArrayAdapter<String> adapter;
    Spinner sItems;

    Spinner spinner;
    EditText board_name;
    EditText board_version;
    EditText motor_pulley;
    EditText wheel_pulley;
    EditText wheel_diameter;
    EditText motor_poles;
    RadioGroup batterySelector;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_board_activity);

        final BoardProfile newBoard = new BoardProfile();

        /**
         * ACTION BAR
         */
        Toolbar myToolbar = (Toolbar) findViewById(R.id.board_add_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("Add a Board");

        /**
         * SET UP SETTINGS
         */
        spinner = (Spinner) findViewById(R.id.add_board_bluetooth_spinner);
        board_name = (EditText) findViewById(R.id.add_board_name_value);
        board_version = (EditText) findViewById(R.id.add_board_version_value);
        motor_pulley = (EditText) findViewById(R.id.add_board_motor_pulley_value);
        wheel_pulley = (EditText) findViewById(R.id.add_board_wheel_pulley_value);
        wheel_diameter = (EditText) findViewById(R.id.add_board_wheel_diameter_value);
        motor_poles = (EditText) findViewById(R.id.add_board_motor_poles_value);
        batterySelector = (RadioGroup) findViewById(R.id.add_board_battery_selector);

        spinner.setOnItemSelectedListener(this);
        batterySelector.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId) {
                    case R.id.radio_6s:
                            batteryType = 6;
                        break;
                    case R.id.radio_8s:
                            batteryType = 8;
                        break;
                    case R.id.radio_10s:
                            batteryType = 10;
                        break;
                    case R.id.radio_12s:
                            batteryType = 12;
                        break;
                }
            }
        });


            /**
         * SAVE BOARD
         */
        Button saveBoard = (Button) findViewById(R.id.button_save_board);
        saveBoard.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                save();
            }
        });

        /**
         * BLUETOOTH
         */
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(btBroadcastReceiver, filter);
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        pairedDevices = new ArrayList<String>();

        if(btAdapter==null) {
            Toast.makeText(getApplicationContext(), "No Bluetooth detected", Toast.LENGTH_SHORT).show();
        }
        else{
            if(btAdapter.isEnabled()) {
                devicesArray = btAdapter.getBondedDevices();
                if (devicesArray.size() > 0) {
                    for (BluetoothDevice device : devicesArray) {
                        pairedDevices.add(device.getName());// + "\n" + device.getAddress());
                    }
                }
            }
            else{
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent, 1);
            }
        }

        adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, pairedDevices);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sItems = (Spinner) findViewById(R.id.add_board_bluetooth_spinner);
        sItems.setAdapter(adapter);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item
        String item = parent.getItemAtPosition(position).toString();
        //find bt device in set
        for (Iterator<BluetoothDevice> it = devicesArray.iterator(); it.hasNext(); ) {
            BluetoothDevice f = it.next();
            if (f.getName().equals(item))
                btDevice=f;
        }
        // Showing selected spinner item
        Toast.makeText(parent.getContext(), "Selected: " + btDevice.getName(), Toast.LENGTH_SHORT).show();
    }
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }

    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(btBroadcastReceiver);
    }

    private void refreshBT(){
        devicesArray = btAdapter.getBondedDevices();
        if (devicesArray.size() > 0) {
            for (BluetoothDevice device : devicesArray) {
                pairedDevices.add(device.getName());// + "\n" + device.getAddress());
            }
            sItems.setAdapter(adapter);
        }
    }

    private void save(){
        Context mContext = getApplicationContext();
        BoardProfile profile = new BoardProfile();
        SaveState load = SaveState.loadData(mContext);

        try{
            String name = board_name.getText().toString();
            double version = Double.parseDouble(board_version.getText().toString());

            int motorPulley = Integer.parseInt(motor_pulley.getText().toString());
            int wheelPulley = Integer.parseInt(wheel_pulley.getText().toString());
            int wheelDiameter = Integer.parseInt(wheel_diameter.getText().toString());

            double ratio = (wheelDiameter*wheelPulley)/(motorPulley*2)*0.10471976;
            int poles = Integer.parseInt(motor_poles.getText().toString());
            double minVolt = batteryType*3.5;
            double maxVolt = batteryType*4.2;

            profile.setName(name);
            profile.setVersion(version);
            profile.setWheelRatio(ratio);
            profile.setMotorPoles(poles);
            profile.setMaxVoltage(maxVolt);
            profile.setMinVoltage(minVolt);
            profile.setBtDevice(btDevice);

            load.bp.add(profile);
            SaveState.saveData(load, mContext);
            finish();
        }catch(Exception e){
            Toast.makeText(mContext, "SAVE FAIELD", Toast.LENGTH_SHORT).show();
        }
    }

    private BroadcastReceiver btBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                switch (state) {
                    case BluetoothAdapter.STATE_ON:
                        //Bluethooth is on, now you can perform your tasks
                        refreshBT();
                        break;
                }
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            }
            else if (BluetoothDevice.ACTION_FOUND.equals(action)){
            }
            else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action))
            {
            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action))
            {
            }
            else {
            }
        }
    };

}
