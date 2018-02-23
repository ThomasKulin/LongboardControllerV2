package com.example.thomas.vesccontroller.Activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.example.thomas.vesccontroller.Helpers.BoardProfile;
import com.example.thomas.vesccontroller.R;

/**
 * Created by Thomas on 2018-01-05.
 */

public class Vesc_Settings_Activity extends AppCompatActivity {

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



    }
}
