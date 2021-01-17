package com.virtuallyprivate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class Settings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        _init();
    }

    private void _init() {
        final SharedPreferences pref = getSharedPreferences(VirtuallyPrivate.NAME, 0);

        // Location
        final EditText latitudeInput = findViewById(R.id.latitude_input);
        final EditText longitudeInput = findViewById(R.id.longitude_input);
        latitudeInput.setText(pref.getString(SharedPrefs.Location.LATITUDE, ""));
        longitudeInput.setText(pref.getString(SharedPrefs.Location.LONGITUDE, ""));

        // App List
        findViewById(R.id.appListChoose).setOnClickListener(view ->
                startActivity(new Intent(Settings.this, SettingsAppChoose.class))
        );

        // Contacts List
        findViewById(R.id.contactsListChoose).setOnClickListener(view ->
                startActivity(new Intent(Settings.this, SettingsContactsChoose.class))
        );

        // Clipboard
        final EditText clipboardInput = findViewById(R.id.clipboard_input);
        clipboardInput.setText(pref.getString(SharedPrefs.CLIPBOARD, ""));

        findViewById(R.id.submitSettingsChangeButtons).setOnClickListener(view -> {
            SharedPreferences.Editor prefEditor = pref.edit();
            prefEditor.putString(SharedPrefs.Location.LATITUDE, latitudeInput.getText().toString());
            prefEditor.putString(SharedPrefs.Location.LONGITUDE, longitudeInput.getText().toString());
            prefEditor.putString(SharedPrefs.CLIPBOARD, clipboardInput.getText().toString());
            prefEditor.apply();

            Toast.makeText(Settings.this, R.string.settings_change_string, Toast.LENGTH_SHORT).show();
        });
    }
}
