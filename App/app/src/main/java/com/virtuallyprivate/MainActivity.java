package com.virtuallyprivate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.lang.System;

public class MainActivity extends AppCompatActivity {
    private DatabaseHelper m_dbHelper;
    private ExpandableListView m_appList;
    private AppAdapter m_appListAdapter;

    private void createAvailablePermissions() {
        // if it's the first time the app ran, it saves the available permissions in the db.
        SharedPreferences settings = getSharedPreferences(VirtuallyPrivate.NAME, 0);
        if (settings.getBoolean("fresh_install", true)) {
            m_dbHelper.addPermission(new Permission(Permissions.CLIPBOARD));
            m_dbHelper.addPermission(new Permission(Permissions.APP_LIST));
            m_dbHelper.addPermission(new Permission(Permissions.CAMERA));
            m_dbHelper.addPermission(new Permission(Permissions.MICROPHONE));
            m_dbHelper.addPermission(new Permission(Permissions.CONTACTS_LIST));
            m_dbHelper.addPermission(new Permission(Permissions.CALL_LOG));
            m_dbHelper.addPermission(new Permission(Permissions.LOCATION));
            // record the fact that the app has been started at least once
            settings.edit().putBoolean("fresh_install", false).commit();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        if (System.getProperty("vxp") == null) {
            Toast.makeText(MainActivity.this, "Not running in VirtualXposed!",
                    Toast.LENGTH_LONG).show();
            finish();
        }
        if (ContextCompat.checkSelfPermission(
                this,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
           _start();
        }
    }

    private void _start() {
        this.m_dbHelper = new DatabaseHelper(MainActivity.this);
        this.m_appList = findViewById(R.id.listview);

        createAvailablePermissions();
        _loadApps();
    }

    /*
    * Waiting until the permission is granted to load
    * */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            if (!Arrays.asList(grantResults).contains(PackageManager.PERMISSION_DENIED)) {
               _start();
            }
        }
    }

    /* Load the applications on user's phone */
    private void _loadApps() {
        final HashMap<AppInfo, ArrayList<String>> appsList = new HashMap<>();
        final ArrayList<AppInfo> appArrayInfo = new ArrayList<>();
        final ArrayList<String> availablePermissions = m_dbHelper.getAvailablePermissions();

        for (ApplicationInfo app: Utils.getUserInstallerApplications(this)) {
            final AppInfo info = new AppInfo(Utils.getAppLabel(this, app), app);
            appsList.put(info, availablePermissions);
            appArrayInfo.add(info);
        }
        this.m_appListAdapter = new AppAdapter(this, appsList,appArrayInfo, m_dbHelper);
        this.m_appList.setAdapter(this.m_appListAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case R.id.settings_button:
                startActivity(new Intent(this, Settings.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Utils.inflateOptionsMenu(menu, getMenuInflater(), true, m_appListAdapter);
        return super.onCreateOptionsMenu(menu);
    }
}