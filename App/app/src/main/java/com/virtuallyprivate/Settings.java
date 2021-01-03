package com.virtuallyprivate;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Settings extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initLocationButtons();
    }

    protected void initLocationButtons() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(VirtuallyPrivate.NAME, 0);
        EditText latitudeInput = findViewById(R.id.latitude_input);
        EditText longitudeInput = findViewById(R.id.longitude_input);
        EditText clipboardInput = findViewById(R.id.clipboard_input);
        latitudeInput.setText(pref.getString(SharedPrefs.Location.LATITUDE, ""));
        longitudeInput.setText(pref.getString(SharedPrefs.Location.LONGITUDE, ""));
        clipboardInput.setText(pref.getString(SharedPrefs.CLIPBOARD, ""));

        findViewById(R.id.submitSettingsChangeButtons).setOnClickListener(new View.OnClickListener() {
            @SuppressWarnings("deprecation")
            SharedPreferences pref = getApplicationContext().getSharedPreferences(VirtuallyPrivate.NAME, MODE_WORLD_READABLE);
            SharedPreferences.Editor prefEditor = pref.edit();
            @Override
            public void onClick(View view) {
                prefEditor.putString(SharedPrefs.Location.LATITUDE, latitudeInput.getText().toString());
                prefEditor.putString(SharedPrefs.Location.LONGITUDE, longitudeInput.getText().toString());
                prefEditor.putString(SharedPrefs.CLIPBOARD, clipboardInput.getText().toString());
                prefEditor.apply();

                Toast.makeText(Settings.this, "Settings Changed!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }
}
