package com.example.thomas.vesccontroller.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.thomas.vesccontroller.Activities.Add_Board.Add_Board_Activity;
import com.example.thomas.vesccontroller.Activities.Board_List.Board_List_Activity;
import com.example.thomas.vesccontroller.Helpers.BoardProfile;
import com.example.thomas.vesccontroller.Helpers.SaveState;
import com.example.thomas.vesccontroller.R;

import java.util.List;

/**
 * Created by Thomas on 2018-01-05.
 */

public class Vesc_Settings_Activity extends AppCompatActivity {

    ListView settingsListView;

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
        final ListViewItem[] items = new ListViewItem[16];
        int i = 0;

        String[] temp = {"BLDC", "FOC", "DC"};
        items[i++] = new ListViewItem("Motor Type",null,temp, CustomAdapter.TYPE_RADIO);

        items[i++] = new ListViewItem("Current",null, null, CustomAdapter.TYPE_TITLE);
        items[i++] = new ListViewItem("Motor Max","Max motor current (A)", null, CustomAdapter.TYPE_EDITTEXT);
        items[i++] = new ListViewItem("Motor Brake Max","Max braking current (A)",null, CustomAdapter.TYPE_EDITTEXT);
        items[i++] = new ListViewItem("Battery Max","Max battery current (A)",null, CustomAdapter.TYPE_EDITTEXT);
        items[i++] = new ListViewItem("Battery Min (regen)","Max regen braking current (A)",null, CustomAdapter.TYPE_EDITTEXT);

        items[i++] = new ListViewItem("Voltage",null,null, CustomAdapter.TYPE_TITLE);
        items[i++] = new ListViewItem("Voltage Max","Max input voltage (V)",null, CustomAdapter.TYPE_EDITTEXT);
        items[i++] = new ListViewItem("Voltage Min","Min input voltage (V)",null, CustomAdapter.TYPE_EDITTEXT);
        items[i++] = new ListViewItem("Battery Cutoff Start","Threshold to start reducing motor current (V)",null, CustomAdapter.TYPE_EDITTEXT);
        items[i++] = new ListViewItem("Battery Cutoff End","Threshold to cutoff motor current (V)",null, CustomAdapter.TYPE_EDITTEXT);

        items[i++] = new ListViewItem("Temperature",null,null, CustomAdapter.TYPE_TITLE);
        items[i++] = new ListViewItem("MOSFET Temp Cutoff Start","Threshold to start reducing motor current (°C)",null, CustomAdapter.TYPE_EDITTEXT);
        items[i++] = new ListViewItem("MOSFET Temp Cutoff End","Threshold to cutoff motor current (°C)",null, CustomAdapter.TYPE_EDITTEXT);
        items[i++] = new ListViewItem("Motor Temp Cutoff Start","Threshold to start reducing motor current (°C)",null, CustomAdapter.TYPE_EDITTEXT);
        items[i++] = new ListViewItem("Motor Temp Cutoff End","Threshold to cutoff motor current (°C)",null, CustomAdapter.TYPE_EDITTEXT);


        CustomAdapter customAdapter = new CustomAdapter(this,R.id.text, items);
        settingsListView.setAdapter(customAdapter);
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

    //--------------------------------LISTVIEW ADAPTER STUFFS---------------------------------------
    public class ListViewItem {
        private String text;
        private String hint;
        private int type;
        private String[] radios;

        public ListViewItem(String text, String hint, String[] radios, int type) {
            this.text = text;
            this.hint = hint;
            this.radios = radios;
            this.type = type;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getHint() {
            return hint;
        }

        public void setHint(String hint) {
            this.hint = hint;
        }

        public String[] getRadios() {
            return radios;
        }

        public void setRadios(String[] radios) {
            this.radios = radios;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }
    }
    public class CustomAdapter extends ArrayAdapter<ListViewItem> {

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
            ListViewItem listViewItem = objects[position];
            int listViewItemType = getItemViewType(position);


            if (convertView == null) {

                switch(listViewItemType) {
                    case TYPE_TITLE: {
                        convertView = LayoutInflater.from(getContext()).inflate(R.layout.vesc_settings_title_item, null);
                        TextView textView = (TextView) convertView.findViewById(R.id.TV_title_item);

                        viewHolder = new ViewHolder(textView);
                        convertView.setTag(viewHolder);
                        viewHolder.getText().setText(listViewItem.getText());
                        break;
                    }
                    case TYPE_EDITTEXT: {
                        convertView = LayoutInflater.from(getContext()).inflate(R.layout.vesc_settings_text_item, null);
                        TextView textView = (TextView) convertView.findViewById(R.id.TV_text_item);
                        EditText editText = (EditText) convertView.findViewById(R.id.text_item_value);

                        viewHolder = new ViewHolder(textView, editText);
                        convertView.setTag(viewHolder);
                        viewHolder.getText().setText(listViewItem.getText());
                        viewHolder.getEditText().setHint(listViewItem.getHint());
                        break;
                    }
                    case TYPE_CHECKBOX: {
                        convertView = LayoutInflater.from(getContext()).inflate(R.layout.vesc_settings_checkbox_item, null);
                        TextView textView = (TextView) convertView.findViewById(R.id.TV_checkbox_item);
                        CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.checkbox_item_checkbox);

                        viewHolder = new ViewHolder(textView, checkBox);
                        convertView.setTag(viewHolder);
                        viewHolder.getText().setText(listViewItem.getText());
                        break;
                    }
                    case TYPE_RADIO: {
                        convertView = LayoutInflater.from(getContext()).inflate(R.layout.vesc_settings_radio_item, null);
                        TextView textView = (TextView) convertView.findViewById(R.id.TV_radio_item);
                        RadioGroup radiogroup = (RadioGroup) convertView.findViewById(R.id.radio_item_radio_grouup);

                        String[] radios = objects[position].getRadios();
                        RadioButton[] radiobutton = new RadioButton[radios.length];
                        for (int i = 0; i < radios.length; i++) {
                            radiobutton[i] = new RadioButton(radiogroup.getContext());
                            radiobutton[i].setText(radios[i]);
                            radiobutton[i].setId(i);
                            radiogroup.addView(radiobutton[i]);
                        }

                        viewHolder = new ViewHolder(textView, radiogroup);
                        convertView.setTag(viewHolder);
                        viewHolder.getText().setText(listViewItem.getText());
                        break;
                    }
                }

            } else {
                convertView.setTag(viewHolder);
                viewHolder = (ViewHolder) convertView.getTag();
            }
            return convertView;
        }

    }

    public class ViewHolder {
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

        public ViewHolder(TextView text, RadioGroup radioGroup) {
            this.text = text;
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
