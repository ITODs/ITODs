package com.example.olddriver.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.example.olddriver.R;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) this.findViewById(R.id.settings_toolbar);
        toolbar.setTitle("个人设置");
        setSupportActionBar(toolbar);
    }
}
