package com.example.thomas.vesccontroller.Activities.Board_List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.thomas.vesccontroller.Activities.Add_Board.Add_Board_Activity;
import com.example.thomas.vesccontroller.Helpers.BoardProfile;
import com.example.thomas.vesccontroller.R;
import com.example.thomas.vesccontroller.Helpers.SaveState;

import java.util.List;

/**
 * Created by Thomas on 2017-12-29.
 */

public class Board_List_Activity extends AppCompatActivity{

    List<BoardProfile> bp;
    ListView yourListView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.board_list_activity);

        /**
         * ACTION BAR
         */
        Toolbar myToolbar = (Toolbar) findViewById(R.id.board_list_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("Board Profiles");

        /**
         * LIST VIEW
         */
        yourListView = (ListView) findViewById(R.id.board_list);
        // get data from the table by the BoardListAdapter
        updateList();

        /**
         * ADD BOARD
         */
        Button addBoard = (Button) findViewById(R.id.button_add_board);
        addBoard.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "add a board", Toast.LENGTH_SHORT)
                        .show();
                Intent newActivity = new Intent(v.getContext(), Add_Board_Activity.class);
                startActivity(newActivity);
            }
        });
    }
    @Override
    protected void onResume(){
        super.onResume();
        updateList();
    }
    public void updateList(){
        bp = SaveState.loadData(this).bp;
        BoardListAdapter customAdapter = new BoardListAdapter(this, R.layout.board_list_item, bp);
        yourListView .setAdapter(customAdapter);
    }

    private class BoardListAdapter extends ArrayAdapter<BoardProfile> {

        public BoardListAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        public BoardListAdapter(Context context, int resource, List<BoardProfile> items) {
            super(context, resource, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View v = convertView;

            if (v == null) {
                LayoutInflater vi;
                vi = LayoutInflater.from(getContext());
                v = vi.inflate(R.layout.board_list_item, null);
            }

            final BoardProfile p = getItem(position);

            if (p != null) {
                TextView t1 = (TextView) v.findViewById(R.id.list_name);
                ImageButton b1 = (ImageButton) v.findViewById(R.id.list_button_delete_board);


                if (t1 != null) {
                    t1.setText(p.getName());
                    t1.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View vv)
                        {
                            // Your code that you want to execute on this button click
                            selectProfile(p);
                        }

                    });
                }

                if (b1 != null) {
                    b1.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View vv)
                        {
                            // Your code that you want to execute on this button click
                            delete(p);
                        }

                    });
                }
            }

            return v;
        }
        public void delete(BoardProfile boardProfile){
            SaveState instance = SaveState.loadData(getContext().getApplicationContext());

            try {
                instance.bp.remove(boardProfile);
                SaveState.saveData(instance, getContext().getApplicationContext());
                Toast.makeText(getContext().getApplicationContext(), "Profile Deleted", Toast.LENGTH_SHORT)
                        .show();
            }catch(Exception e){
                Toast.makeText(getContext().getApplicationContext(), "DELETE ERROR!", Toast.LENGTH_SHORT)
                        .show();
            }
            updateList();

        }
        public void selectProfile(BoardProfile bp){
            SaveState instance = SaveState.loadData(getContext().getApplicationContext());

            try {
                instance.bp.remove(bp);
                instance.bp.add(0, bp);
                SaveState.saveData(instance, getContext().getApplicationContext());
                Toast.makeText(getContext().getApplicationContext(), "Profile Selected", Toast.LENGTH_SHORT)
                        .show();
            }catch(Exception e){
                Toast.makeText(getContext().getApplicationContext(), "SELECTION ERROR!", Toast.LENGTH_SHORT)
                        .show();
            }
            updateList();
        }
    }
}
