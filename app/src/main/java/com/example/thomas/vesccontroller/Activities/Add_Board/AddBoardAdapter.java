package com.example.thomas.vesccontroller.Activities.Add_Board;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View.OnFocusChangeListener;

import com.example.thomas.vesccontroller.Helpers.listItem;
import com.example.thomas.vesccontroller.R;

import java.util.ArrayList;
import java.util.List;

public class AddBoardAdapter extends ArrayAdapter<listItem> {

    public List<listItem> myItems = new ArrayList<listItem>();

    public AddBoardAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public AddBoardAdapter(Context context, int resource, List<listItem> items) {
        super(context, resource, items);
        myItems=items;
    }

    public int getCount() {
        return myItems.size();
    }

    public listItem getItem(int position) {
        return myItems.get(position);
    }

    public long getItemId(int position) {
        return position;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            convertView = vi.inflate(R.layout.add_board_item, null);

            holder.name = (TextView) convertView.findViewById(R.id.add_board_list_setting_name);
            holder.value = (EditText) convertView.findViewById(R.id.add_board_list_setting_value);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        listItem p = getItem(position);
        if (p != null) {
                holder.name.setText(p.getName());
                holder.value.setHint(p.getHint());
                holder.value.setId(position);

        }
        //update adapter once we finish with editing
        holder.value.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                // TODO Auto-generated method stub
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub

            }
        });
//
//
//                setOnFocusChangeListener(new OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (!hasFocus){
//                    final int index = v.getId();
//                    final EditText Value = (EditText) v;
//                    myItems.get(index).setValue(Value.getText().toString());
//                }
//            }
//        });
        return convertView;
        }
class ViewHolder {
    TextView name;
    EditText value;
}
}