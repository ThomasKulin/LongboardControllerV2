package com.example.thomas.vesccontroller.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.thomas.vesccontroller.Helpers.BoardProfile;
import com.example.thomas.vesccontroller.Activities.Board_List.Board_List_Activity;
import com.example.thomas.vesccontroller.R;
import com.example.thomas.vesccontroller.Helpers.SaveState;

import io.netopen.hotbitmapgg.library.view.RingProgressBar;


/**
 * Created by Thomas on 2017-12-28.
 */

public class Board_Activity extends AppCompatActivity {

    short batteryLevel=50; //battery percentage (0-100)
    BoardProfile currentProfile;
    TextView totalDistance;
    TextView averageSpeed;
    TextView maxSpeed;
    RingProgressBar mRingProgressBar;
    ImageView image;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.board_activity);

        totalDistance = (TextView) findViewById(R.id.TV_total_distance);
        averageSpeed = (TextView) findViewById(R.id.TV_average_speed);
        maxSpeed = (TextView) findViewById(R.id.TV_max_speed);

        /**
         * ACTION BAR
         */
        Toolbar myToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(myToolbar);
        /**
         * BATTERY INDICATOR
         */
        mRingProgressBar = (RingProgressBar) findViewById(R.id.progress_bar_battery);
        // Set the progress bar's progress
        mRingProgressBar.setProgress(batteryLevel);
        mRingProgressBar.setOnProgressListener(new RingProgressBar.OnProgressListener()
        {
            @Override
            public void progressToComplete()
            {
                // Progress reaches the maximum callback default Max value is 100
            }
        });

        /**
         * Longboard Connect Button Listener
         */
        image = (ImageView)findViewById(R.id.button_longboard);
        image.setOnClickListener(new View.OnClickListener(){

            public void onClick(View view) {
               //initiateBtConnection(device)
                Toast.makeText(Board_Activity.this, "Initiate BlueTooth Connection", Toast.LENGTH_SHORT).show();
                view.startAnimation(AnimationUtils.loadAnimation(view.getContext(), R.anim.anim_item));
            }});
    }

    @Override
    protected void onResume(){
        super.onResume();
        loadProfile();
    }








    public void loadProfile() { //find the first saved board profile
        Context mContext = getApplicationContext();
        BoardProfile profile = new BoardProfile();
        SaveState load = SaveState.loadData(mContext);

        if(load==null){ //if there is no previous save
            SaveState.saveData(SaveState.getInstance(), mContext);
            load = SaveState.loadData(mContext);
        }

        if(load.bp.size() == 0){ //if there is no existing board profile
            load.bp.add(profile); //add default profile
            SaveState.saveData(load, mContext);
        }
        profile = load.bp.get(0);

        //update views
        currentProfile = profile;
        getSupportActionBar().setTitle(currentProfile.getName()); //sets the name displayed at the top of the app
        totalDistance.setText("" + currentProfile.getTotalDist());
        averageSpeed.setText("" + currentProfile.getAvgSpeed());
        maxSpeed.setText("" + currentProfile.getMaxSpeed());
    }

    /**
     * MENU STUFF
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.action_list_boards:
                Toast.makeText(this, "list all board profiles", Toast.LENGTH_SHORT)
                        .show();
                Intent boardListActivity = new Intent(this, Board_List_Activity.class);
                startActivity(boardListActivity);
                break;
            // action with ID action_settings was selected
            case R.id.action_settings:
                Toast.makeText(this, "VESC settings selected", Toast.LENGTH_SHORT)
                        .show();
                Intent VescActivity = new Intent(this, Vesc_Settings_Activity.class);
                startActivity(VescActivity);
                break;
            default:
                break;
        }
        return true;
    }
}
