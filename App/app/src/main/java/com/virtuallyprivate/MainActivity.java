package com.virtuallyprivate;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.icu.util.ULocale;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    ListView listview;

    protected void createAvailablePermissions() {
        // if it is the first time the app ran, it saves the available permissions in the db.
        final String PREFS_NAME = "VirtuallyPrivate";
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        if (settings.getBoolean("fresh_install", true)) {
            DatabaseManager dbManager = new DatabaseManager(MainActivity.this);
            // add permissions like this
            dbManager.addPermission(new Permission( "permission_1"));
            // record the fact that the app has been started at least once
            settings.edit().putBoolean("fresh_install", false).commit();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createAvailablePermissions();
        DatabaseManager dbManager = new DatabaseManager(MainActivity.this);

        Permission p = new Permission("permission_1");

        ArrayList<String> a = dbManager.getAvailablePermissions();
        listview = (ListView) findViewById(R.id.listview);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // selected item
                String selected = ((TextView) view.findViewById(android.R.id.text1)).getText().toString();
                Toast toast = Toast.makeText(getApplicationContext(), selected, Toast.LENGTH_SHORT);
                toast.show();
            }
        });

        List<ApplicationInfo> pkgAppsList = getPackageManager().getInstalledApplications(0);

        final ArrayList<String> list = new ArrayList<String>();
        for (ApplicationInfo app: pkgAppsList) {
            list.add((String) getPackageManager().getApplicationLabel(app));
        }

        final ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1, list);
        listview.setAdapter(adapter);
    }
}