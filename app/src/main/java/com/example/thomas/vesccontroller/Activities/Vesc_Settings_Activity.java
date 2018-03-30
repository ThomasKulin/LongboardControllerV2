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
        final ListViewItem[] items = new ListViewItem[40];

        items[0] = new ListViewItem("Title " + i,"hint", CustomAdapter.TYPE_TITLE);
        items[1] = new ListViewItem("EditText " + i,"hint", CustomAdapter.TYPE_EDITTEXT);
        items[2] = new ListViewItem("CheckBox " + i,"hint", CustomAdapter.TYPE_CHECKBOX);

        CustomAdapter customAdapter = new CustomAdapter(this, R.id.text, items);
        settingsListView.setAdapter(customAdapter);
        settingsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(getBaseContext(), items[i].getText(), Toast.LENGTH_SHORT).show();
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

    //--------------------------------LISTVIEW ADAPTER STUFFS---------------------------------------
    public class ListViewItem {
        private String text;
        private String hint;
        private int type;

        public ListViewItem(String text, String hint, int type) {
            this.text = text;
            this.hint = hint;
            this.type = type;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getHint() {
            return text;
        }

        public void setHing(String hint) {
            this.hint = hint;
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

        private ListViewItem[] objects;

        @Override
        public int getViewTypeCount() {
            return 3;
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

                if (listViewItemType == TYPE_TITLE) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.vesc_settings_title_item, null);
                    TextView textView = (TextView) convertView.findViewById(R.id.TV_title_item);
                    viewHolder = new ViewHolder(textView);
                } else if (listViewItemType == TYPE_EDITTEXT) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.vesc_settings_text_item, null);
                    TextView textView = (TextView) convertView.findViewById(R.id.TV_text_item);
                    EditText editText = (EditText) convertView.findViewById(R.id.text_item_value);
                    viewHolder = new ViewHolder(textView, editText);
                } else if (listViewItemType == TYPE_CHECKBOX) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.vesc_settings_checkbox_item, null);
                    TextView textView = (TextView) convertView.findViewById(R.id.TV_checkbox_item);
                    CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.checkbox_item_checkbox);
                    viewHolder = new ViewHolder(textView, checkBox);
                }

                convertView.setTag(viewHolder);

            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.getText().setText(listViewItem.getText());

            return convertView;
        }

    }

    public class ViewHolder {
        TextView text;
        EditText editText;
        CheckBox checkBox;

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
    }
}
