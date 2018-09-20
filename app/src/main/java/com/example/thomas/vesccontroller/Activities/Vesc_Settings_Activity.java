package com.example.thomas.vesccontroller.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.thomas.vesccontroller.Activities.Add_Board.Add_Board_Activity;
import com.example.thomas.vesccontroller.Activities.Board_List.Board_List_Activity;
import com.example.thomas.vesccontroller.Helpers.BoardProfile;
import com.example.thomas.vesccontroller.Helpers.Communications.PacketTools;
import com.example.thomas.vesccontroller.Helpers.SaveState;
import com.example.thomas.vesccontroller.R;

import java.sql.BatchUpdateException;
import java.util.List;

/**
 * Created by Thomas on 2018-01-05.
 */

public class Vesc_Settings_Activity extends AppCompatActivity {

    ListView settingsListView;
    Button readConfig;
    Button writeConfig;
    static boolean configRead = false;
    static CustomAdapter customAdapter;

    static ListViewItem[] items;
    public static PacketTools.mc_configuration config;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vesc_settings_activity);

        /**
         * ACTION BAR
         */
        Toolbar myToolbar = (Toolbar) findViewById(R.id.vesc_settings_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("VESC Settings");

        /**
         * LIST VIEW
         */
        settingsListView = (ListView) findViewById(R.id.vesc_settings_list);
        items = new ListViewItem[17];
        int i = 0;

        String[] temp = {"BLDC", "DC", "FOC"};
        items[i++] = new ListViewItem("Motor Type",null,null,false,null,0, CustomAdapter.TYPE_TITLE);
        items[i++] = new ListViewItem(null,null,null,false,temp,1, CustomAdapter.TYPE_RADIO);

        items[i++] = new ListViewItem("Current",null,null, false,null,0, CustomAdapter.TYPE_TITLE);
        items[i++] = new ListViewItem("Motor Max",null,"Max motor current (A)",false, null,0, CustomAdapter.TYPE_EDITTEXT);
        items[i++] = new ListViewItem("Motor Brake Max",null,"Max braking current (A)",false,null,0, CustomAdapter.TYPE_EDITTEXT);
        items[i++] = new ListViewItem("Battery Max",null,"Max battery current (A)",false,null,0, CustomAdapter.TYPE_EDITTEXT);
        items[i++] = new ListViewItem("Battery Min (regen)",null,"Max regen braking current (A)",false,null,0, CustomAdapter.TYPE_EDITTEXT);

        items[i++] = new ListViewItem("Voltage",null,null,false,null,0, CustomAdapter.TYPE_TITLE);
        items[i++] = new ListViewItem("Voltage Max",null,"Max input voltage (V)",false,null,0, CustomAdapter.TYPE_EDITTEXT);
        items[i++] = new ListViewItem("Voltage Min",null,"Min input voltage (V)",false,null,0, CustomAdapter.TYPE_EDITTEXT);
        items[i++] = new ListViewItem("Battery Cutoff Start",null,"Threshold to start reducing motor current (V)",false,null,0, CustomAdapter.TYPE_EDITTEXT);
        items[i++] = new ListViewItem("Battery Cutoff End",null,"Threshold to cutoff motor current (V)",false,null,0, CustomAdapter.TYPE_EDITTEXT);

        items[i++] = new ListViewItem("Temperature",null,null,false,null,0, CustomAdapter.TYPE_TITLE);
        items[i++] = new ListViewItem("MOSFET Temp Cutoff Start",null,"Threshold to start reducing motor current (°C)",false,null,0, CustomAdapter.TYPE_EDITTEXT);
        items[i++] = new ListViewItem("MOSFET Temp Cutoff End",null,"Threshold to cutoff motor current (°C)",false,null,0, CustomAdapter.TYPE_EDITTEXT);
        items[i++] = new ListViewItem("Motor Temp Cutoff Start",null,"Threshold to start reducing motor current (°C)",false,null,0, CustomAdapter.TYPE_EDITTEXT);
        items[i++] = new ListViewItem("Motor Temp Cutoff End",null,"Threshold to cutoff motor current (°C)",false,null,0, CustomAdapter.TYPE_EDITTEXT);


        customAdapter = new CustomAdapter(this,R.id.text, items);
        settingsListView.setAdapter(customAdapter);

        /**
         * BUTTONS
         */

        readConfig = (Button) findViewById(R.id.read_config_button);
        readConfig.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                Toast.makeText(v.getContext(), "Read VESC Config", Toast.LENGTH_SHORT)
                        .show();
                PacketTools.vescUartGetValue(PacketTools.COMM_PACKET_ID.COMM_GET_MCCONF);
            }
        });
        writeConfig = (Button) findViewById(R.id.write_config_button);
        writeConfig.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button

//                if(configRead) {
                    Toast.makeText(v.getContext(), "Write VESC Config", Toast.LENGTH_SHORT)
                        .show();
                    updateConfig();
//                }
//                else{
//                    Toast.makeText(v.getContext(), "Read Config First!", Toast.LENGTH_SHORT)
//                            .show();
//                }

            }
        });
    }
    @Override
    protected void onResume(){
        super.onResume();
    }
    @Override
    protected void onPause(){
        super.onPause();
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
    }
    //--------------------------------FUNCTIONS AND SHIT--------------------------------------------

    public static void updateValues(PacketTools.mc_configuration configuration){
        config = configuration;
        items[1].setRadioId(configuration.motor_type.ordinal()); //MOTOR TYPE radio
        items[3].setValue(Float.toString(configuration.l_current_max)); //max motor current
        items[4].setValue(Float.toString(configuration.l_current_min)); //min motor current (braking)
        items[5].setValue(Float.toString(configuration.l_in_current_max)); //max battery current
        items[6].setValue(Float.toString(configuration.l_in_current_min)); //min battery current (regen)
        items[8].setValue(Float.toString(configuration.l_max_vin)); //max voltage
        items[9].setValue(Float.toString(configuration.l_min_vin)); //min voltage
        items[10].setValue(Float.toString(configuration.l_battery_cut_start)); //battery cutoff start
        items[11].setValue(Float.toString(configuration.l_battery_cut_end)); //battery cutoff end
        items[13].setValue(Float.toString(configuration.l_temp_fet_start));
        items[14].setValue(Float.toString(configuration.l_temp_fet_end));
        items[15].setValue(Float.toString(configuration.l_temp_motor_start));
        items[16].setValue(Float.toString(configuration.l_temp_motor_end));
        configRead = true;
        customAdapter.update();
    }

    public static void updateConfig(){
        config.motor_type = PacketTools.mc_motor_type.values()[items[1].getRadioId()];
        config.l_current_max = Float.parseFloat(items[3].getValue());
        config.l_current_min = Float.parseFloat(items[4].getValue());
        config.l_in_current_max = Float.parseFloat(items[5].getValue());
        config.l_in_current_min = Float.parseFloat(items[6].getValue());
        config.l_max_vin = Float.parseFloat(items[8].getValue());
        config.l_min_vin = Float.parseFloat(items[9].getValue());
        config.l_battery_cut_start = Float.parseFloat(items[10].getValue());
        config.l_battery_cut_end = Float.parseFloat(items[11].getValue());
        config.l_temp_fet_start = Float.parseFloat(items[13].getValue());
        config.l_temp_fet_end = Float.parseFloat(items[14].getValue());
        config.l_temp_motor_start = Float.parseFloat(items[15].getValue());
        config.l_temp_motor_end = Float.parseFloat(items[16].getValue());
        PacketTools.vescUartSetValue(config, PacketTools.COMM_PACKET_ID.COMM_SET_MCCONF);
    }


    //--------------------------------LISTVIEW ADAPTER STUFFS---------------------------------------
    public class ListViewItem {
        private String text;
        private String value;
        private String hint;
        private boolean checked;
        private String[] radios;
        int radioId;
        private int type;

        public ListViewItem(String text, String value, String hint, boolean checked, String[] radios,int radioId, int type) {
            this.text = text;
            this.value = value;
            this.hint = hint;
            this.checked = checked;
            this.radios = radios;
            this.radioId = radioId;
            this.type = type;
        }

        public String getText() {
            return text;
        }
        public void setText(String text) {
            this.text = text;
        }

        public String getValue() {
            return value;
        }
        public void setValue(String value) {
            this.value = value;
        }

        public String getHint() {
            return hint;
        }
        public void setHint(String hint) {
            this.hint = hint;
        }

        public boolean isChecked() {
            return checked;
        }
        public void setChecked(boolean checked) {
            this.checked = checked;
        }

        public String[] getRadios() {
            return radios;
        }
        public void setRadios(String[] radios) {
            this.radios = radios;
        }

        public int getRadioId() {
            return radioId;
        }
        public void setRadioId(int radioId) {
            this.radioId = radioId;
        }

        public int getType() {
            return type;
        }
        public void setType(int type) {
            this.type = type;
        }
    }
    public class CustomAdapter extends ArrayAdapter<ListViewItem>{

        public static final int TYPE_TITLE = 0;
        public static final int TYPE_EDITTEXT = 1;
        public static final int TYPE_CHECKBOX = 2;
        public static final int TYPE_RADIO = 3;

        private ListViewItem[] objects;

        @Override
        public int getViewTypeCount() {
            return 4;
        }

        @Override
        public int getItemViewType(int position) {
            return objects[position].getType();
        }

        public CustomAdapter(Context context, int resource, ListViewItem[] objects) {
            super(context, resource, objects);
            this.objects = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder viewHolder = null;
            final ListViewItem listViewItem = objects[position];
            int listViewItemType = getItemViewType(position);


            if (convertView == null) {
                switch (listViewItemType) {
                    case TYPE_TITLE: {
                        convertView = LayoutInflater.from(getContext()).inflate(R.layout.vesc_settings_title_item, null);
                        TextView textView = (TextView) convertView.findViewById(R.id.TV_title_item);

                        viewHolder = new ViewHolder(textView);
                        convertView.setTag(viewHolder);
                        break;
                    }
                    case TYPE_EDITTEXT: {
                        convertView = LayoutInflater.from(getContext()).inflate(R.layout.vesc_settings_text_item, null);
                        TextView textView = (TextView) convertView.findViewById(R.id.TV_text_item);
                        EditText editText = (EditText) convertView.findViewById(R.id.text_item_value);

                        viewHolder = new ViewHolder(textView, editText);
                        convertView.setTag(viewHolder);
                        break;
                    }
                    case TYPE_CHECKBOX: {
                        convertView = LayoutInflater.from(getContext()).inflate(R.layout.vesc_settings_checkbox_item, null);
                        TextView textView = (TextView) convertView.findViewById(R.id.TV_checkbox_item);
                        CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.checkbox_item_checkbox);

                        viewHolder = new ViewHolder(textView, checkBox);
                        convertView.setTag(viewHolder);
                        break;
                    }
                    case TYPE_RADIO: {
                        convertView = LayoutInflater.from(getContext()).inflate(R.layout.vesc_settings_radio_item, null);
                        RadioGroup radiogroup = (RadioGroup) convertView.findViewById(R.id.radio_item_radio_group);

                        String[] radios = objects[position].getRadios();
                        RadioButton[] radiobutton = new RadioButton[radios.length];
                        for (int i = 0; i < radios.length; i++) {
                            radiobutton[i] = new RadioButton(radiogroup.getContext());
                            radiobutton[i].setText(radios[i]);
                            radiobutton[i].setId(i);
                            radiogroup.addView(radiobutton[i]);
                        }

                        viewHolder = new ViewHolder(radiogroup);
                        convertView.setTag(viewHolder);
                        break;
                    }
                }

            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            switch (listViewItemType) {
                case TYPE_TITLE: {
                    viewHolder.getText().setText(listViewItem.getText());
                    viewHolder.getText().setId(position);
                    break;
                }
                case TYPE_EDITTEXT: {
                    viewHolder.getText().setText(listViewItem.getText());
                    viewHolder.getText().setId(position);
                    viewHolder.getEditText().setHint(listViewItem.getHint());
                    viewHolder.getEditText().setText(listViewItem.getValue());
                    viewHolder.getEditText().setId(position);

                    //we need to update adapter once we finish with editing
                    viewHolder.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
                        public void onFocusChange(View v, boolean hasFocus) {
                            if (!hasFocus) {
                                final EditText value = (EditText) v;
                                listViewItem.setValue(value.getText().toString());
                            }
                        }
                    });
                    break;
                }
                case TYPE_CHECKBOX: {
                    viewHolder.getText().setText(listViewItem.getText());
                    viewHolder.getText().setId(position);
                    viewHolder.getCheckBox().setChecked(listViewItem.isChecked());
                    viewHolder.getCheckBox().setId(position);
                    viewHolder.getCheckBox().setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            listViewItem.setChecked(isChecked);
                        }
                    });
                    break;
                }
                case TYPE_RADIO: {
                    try {
                        int radioButtonID = viewHolder.getRadioGroup().getCheckedRadioButtonId();
                        if(radioButtonID == -1) {
                            radioButtonID = 0;
                        }else if(radioButtonID == listViewItem.getRadioId()){
                            //do nothing
                        }else {
                            viewHolder.getRadioGroup().setTag(false);
                            ((RadioButton) viewHolder.getRadioGroup().getChildAt(radioButtonID)).setChecked(false);
                            ((RadioButton) viewHolder.getRadioGroup().getChildAt(listViewItem.getRadioId())).setChecked(true);
                        }
                        viewHolder.getRadioGroup().setTag(true);
                        viewHolder.getRadioGroup().setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(RadioGroup group, int checkedId) {
                                if ((boolean) group.getTag()) {
                                    group.setTag(false);
                                    ((RadioButton) group.getChildAt(listViewItem.getRadioId())).setChecked(false);
                                    listViewItem.setRadioId(checkedId);
                                    ((RadioButton) group.getChildAt(listViewItem.getRadioId())).setChecked(true);
                                    group.setTag(true);
                                }
                            }
                        });
                        viewHolder.getRadioGroup().setId(position);
                        break;
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
            return convertView;
        }

        public void update() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    customAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    public static class ViewHolder {
        TextView text;
        EditText editText;
        CheckBox checkBox;
        RadioGroup radioGroup;

        public ViewHolder(TextView text) {
            this.text = text;
        }

        public ViewHolder(TextView text, EditText editText) {
            this.text = text;
            this.editText = editText;
        }

        public ViewHolder(TextView text, CheckBox checkBox) {
            this.text = text;
            this.checkBox = checkBox;
        }

        public ViewHolder(RadioGroup radioGroup) {
            this.radioGroup = radioGroup;
        }

        public TextView getText() {
            return text;
        }
        public void setText(TextView text) {
            this.text = text;
        }

        public EditText getEditText() {
            return editText;
        }
        public void setEditText(EditText editText) {
            this.editText = editText;
        }

        public CheckBox getCheckBox() {
            return checkBox;
        }
        public void setCheckBox(CheckBox checkBox) {
            this.checkBox = checkBox;
        }

        public RadioGroup getRadioGroup() {
            return radioGroup;
        }
        public void setRadioGroup(RadioGroup radioGroup) {
            this.radioGroup = radioGroup;
        }
    }
}
