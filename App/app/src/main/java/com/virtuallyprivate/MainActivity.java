package com.virtuallyprivate;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;
import android.widget.SearchView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import java.lang.System;

public class MainActivity extends AppCompatActivity {

    DatabaseManager dbManager;
    ExpandableListView expandableListView;
    AppAdapter appListAdapter;

    protected void createAvailablePermissions() {
        // if it is the first time the app ran, it saves the available permissions in the db.
        final String PREFS_NAME = "VirtuallyPrivate";
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        if (settings.getBoolean("fresh_install", true)) {
            dbManager.addPermission(new Permission(Permissions.CLIPBOARD));
            dbManager.addPermission(new Permission(Permissions.APP_LIST));
            dbManager.addPermission(new Permission(Permissions.LOCATION));
            dbManager.addPermission(new Permission(Permissions.MIC));
            // record the fact that the app has been started at least once
            settings.edit().putBoolean("fresh_install", false).commit();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (System.getProperty("vxp") == null) {
            Toast.makeText(MainActivity.this, "Not running in VirtualXposed!",
                    Toast.LENGTH_LONG).show();
            //finish();
        } else {
            this.dbManager = new DatabaseManager(MainActivity.this);

            createAvailablePermissions();

            expandableListView = findViewById(R.id.listview);
            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

            LoadApps(dbManager);
        }
    }

    /*Load the applications on user's phone*/
    private void LoadApps(DatabaseManager dbManager) {
        final HashMap<AppInfo, ArrayList<String>> appsList = new HashMap<>();
        final ArrayList<AppInfo> appArrayInfo = new ArrayList<>();
        ArrayList<String> availablePermissions = dbManager.getAvailablePermissions();


        for (ApplicationInfo app: getPackageManager().getInstalledApplications(0)) {
            // Check that it is only user-installed app.
            if (!((app.flags & ApplicationInfo.FLAG_SYSTEM) != 0))
            {
                AppInfo info = new AppInfo((String) getPackageManager().getApplicationLabel(app), app);
                appsList.put(info, availablePermissions);
                appArrayInfo.add(info);
            }
        }
        appListAdapter = new AppAdapter(this, appsList,appArrayInfo, dbManager);
        expandableListView.setAdapter(appListAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.search_bar, menu);
        MenuItem menuItem = menu.findItem(R.id.search_icon);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Search..");
        searchView.setIconifiedByDefault(false);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                appListAdapter.filter(newText);
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }
}